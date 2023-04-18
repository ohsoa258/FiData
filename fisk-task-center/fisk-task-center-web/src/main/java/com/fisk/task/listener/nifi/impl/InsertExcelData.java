package com.fisk.task.listener.nifi.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.TableNameGenerateUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.daconfig.DataSourceConfig;
import com.fisk.task.dto.daconfig.FtpConfig;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.nifi.ISftpDataUploadListener;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.service.nifi.impl.TableNifiSettingServiceImpl;
import com.fisk.task.utils.StackTraceHelper;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

import static com.fisk.common.core.constants.ExcelConstants.EXCEL2003_SUFFIX_NAME;

/**
 * @author cfk
 */
@Component
@Slf4j
public class InsertExcelData implements ISftpDataUploadListener {
    @Resource
    DataAccessClient client;
    @Resource
    UserClient userClient;
    @Value("${fiData-data-ods-source}")
    private String dataSourceOdsId;
    @Resource
    TableNifiSettingServiceImpl tableNifiSettingService;

    @Override
    public ResultEnum buildSftpDataUploadListener(String data) {
        log.info("sftp或ftp-Java代码同步参数:{}", data);
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            KafkaReceiveDTO kafkaReceive = JSON.parseObject(data, KafkaReceiveDTO.class);
            String topic = kafkaReceive.topic;
            String[] topicParameter = topic.split("\\.");
            String appId = "";
            String tableId = "";
            if (Objects.equals(topicParameter.length, 6)) {
                appId = topicParameter[4];
                tableId = topicParameter[5];
            } else if (Objects.equals(topicParameter.length, 7)) {
                appId = topicParameter[5];
                tableId = topicParameter[6];
            }
            ResultEntity<DataAccessConfigDTO> dataAccessConfig = client.dataAccessConfig(Long.parseLong(tableId), Long.parseLong(appId));
            if (dataAccessConfig.code == ResultEnum.SUCCESS.getCode()) {
                DataAccessConfigDTO config = dataAccessConfig.data;
                DataSourceConfig targetDsConfig = config.targetDsConfig;
                TableNifiSettingPO one = tableNifiSettingService.query().eq("table_access_id", tableId).eq("app_id", appId).eq("type", OlapTableEnum.PHYSICS.getValue()).one();
                targetDsConfig.targetTableName = one.tableName;
                List<TableFieldsDTO> tableFieldsList = targetDsConfig.tableFieldsList;
                Integer columnCount = tableFieldsList.size();
                FtpConfig ftpConfig = config.ftpConfig;
                InputStream fileInputStream = null;
                if (ftpConfig.whetherSftpl) {
                    ChannelSftp connect = connect(ftpConfig.hostname, Integer.valueOf(ftpConfig.port), ftpConfig.username, ftpConfig.password, StringUtils.isEmpty(ftpConfig.fileName) ? null : (ftpConfig.linuxPath + ftpConfig.fileName));
                    fileInputStream = getSftpFileInputStream(connect, ftpConfig.remotePath + "/" + ftpConfig.fileFilterRegex);
                } else {
                    FTPClient ftpClient = getFtpClient(ftpConfig.hostname, ftpConfig.port, ftpConfig.username, ftpConfig.password);
                    fileInputStream = getInputStreamByName(ftpClient, ftpConfig.remotePath, ftpConfig.fileFilterRegex);
                }

                String[] split = ftpConfig.fileFilterRegex.split("\\.");
                List<List<Object>> lists = readExcelFromInputStream(fileInputStream, split[split.length - 1], ftpConfig.startLine, config);
                //第一个list是行,第二个list是列,里面每个object是格
                // 构造 SQL 语句，? 代表需要填充的数据

                List<String> stgAndTableName = TableNameGenerateUtils.getStgAndTableName(targetDsConfig.targetTableName);
                StringBuilder sqlBuilder = new StringBuilder("INSERT INTO " + stgAndTableName.get(0) + " (fidata_flow_batch_code,");
                for (int i = 0; i < columnCount; i++) {
                    if (i > 0) {
                        sqlBuilder.append(", ");
                    }
                    sqlBuilder.append("[" + tableFieldsList.get(i).fieldName + "]");
                }
                sqlBuilder.append(") VALUES ('',");
                for (int i = 0; i < columnCount; i++) {
                    if (i > 0) {
                        sqlBuilder.append(", ");
                    }
                    sqlBuilder.append("?");
                }
                sqlBuilder.append(")");

                ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceOdsId));
                if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
                    DataSourceDTO dataSource = fiDataDataSource.data;
                    // 创建连接
                    conn = DriverManager.getConnection(dataSource.conStr, dataSource.conAccount, dataSource.conPassword);
                    // 预编译 SQL 语句
                    pstmt = conn.prepareStatement(sqlBuilder.toString());
                    // 循环插入数据
                    int batchSize = 1000; // 每批次插入的数据条数
                    int count = 0;
                    assert lists != null;
                    for (List list : lists) {
                        for (int i = 0; i < list.size(); i++) {
                            Object object = list.get(i);
                            if (object != null) {
                                pstmt.setString(i + 1, object.toString());
                            } else {
                                pstmt.setString(i + 1, null);
                            }
                        }
                        pstmt.addBatch();
                        count++;
                        if (count % batchSize == 0) {
                            pstmt.executeBatch();
                        }
                    }
                    pstmt.executeBatch(); // 执行剩余的数据

                } else {
                    log.error("userclient无法查询到ods库的连接信息");
                    throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
                }
            }
        } catch (Exception e) {
            log.error("sftp或ftp-Java代码同步" + StackTraceHelper.getStackTraceInfo(e));
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                log.error(StackTraceHelper.getStackTraceInfo(e));
            }
        }
        return ResultEnum.SUCCESS;

    }

    /**
     * @return java.util.List<com.fisk.dataaccess.dto.ftp.ExcelDTO>
     * @description 读取excel内容
     * @author Lock
     * @date 2021/12/28 10:29
     * @version v1.0
     * @params inputStream Excel文件流
     * @params ext 文件后缀名
     */
    public static List<List<Object>> readExcelFromInputStream(InputStream inputStream, String ext, Integer startRow, DataAccessConfigDTO config) {

        try {
            Workbook workbook = readFromInputStream(inputStream, ext);
            if (workbook == null) {
                return null;
            }
            // 读取Excel内容，返回list，每一行存放一个list
            List<List<Object>> lists = readExcelContentList(workbook, config.ftpConfig.sheetName, startRow, config);
            return lists;
        } catch (Exception e) {
            throw new FkException(ResultEnum.READ_EXCEL_CONTENT_ERROR);
        }
    }

    /**
     * @return java.util.List<java.util.List < java.lang.String>>
     * @description 读取Excel内容，返回list，每一行存放一个list
     * @author Lock
     * @date 2021/12/28 10:19
     * @version v1.0
     * @params wb 工作簿对象
     * @params index sheet页
     */
    private static List<List<Object>> readExcelContentList(Workbook wb, String sheetName, int startRow, DataAccessConfigDTO config) {
        if (wb != null) {
            try {
                List<List<Object>> content = new ArrayList<>();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Sheet sheet = wb.getSheet(sheetName);
                // 获取行数
                int getRow = 0;
                short lastCellNum = (short) config.targetDsConfig.tableFieldsList.size();
                //解决最大行数一直变的问题,拿第一次得到的行数
                int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();
                for (int i = 0; i <= physicalNumberOfRows; i++) {
                    if (getRow < startRow + 1) {
                        getRow++;
                        continue;
                    }
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        lastCellNum = row.getLastCellNum() > lastCellNum ? row.getLastCellNum() : lastCellNum;
                    } else {
                        row = sheet.createRow(i);
                    }
                    List<Object> col = new ArrayList<>();
                    for (int j = 0; j < lastCellNum; j++) {
                        //System.out.println("坐标:"+i+","+j);
                        Object obj = getCellFormatValue(Objects.nonNull(row.getCell(j)) ? row.getCell(j) : row.createCell(j));
                        obj = (obj instanceof Date) ? simpleDateFormat.format((Date) obj) : obj;
                        col.add(obj);
                    }
                    long count = col.stream().count();
                    Optional.of(col).filter(x -> count > 0).ifPresent(content::add);
                    getRow++;
                }
                return content;
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return null;
    }

    /**
     * @return java.lang.Object
     * @description 根据Cell类型设置数据
     * @author Lock
     * @date 2021/12/28 10:25
     * @version v1.0
     * @params cell excel单元格对象
     */
    private static Object getCellFormatValue(Cell cell) {
        Object cellvalue = "";
        if (cell != null) {
            switch (cell.getCellType()) {
                case STRING:
                    cellvalue = cell.getStringCellValue();
                    break;
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        cellvalue = cell.getDateCellValue();
                    } else {
                        cellvalue = cell.getNumericCellValue();
                    }
                    break;
                case BOOLEAN:
                    cellvalue = cell.getBooleanCellValue();
                    break;
                case FORMULA:
                    switch (cell.getCachedFormulaResultType()) {
                        case STRING:
                            cellvalue = cell.getStringCellValue();
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                cellvalue = cell.getDateCellValue();
                            } else {
                                cellvalue = cell.getNumericCellValue();
                            }
                            break;
                        case BOOLEAN:
                            cellvalue = cell.getBooleanCellValue();
                            break;
                        default:
                            break;
                    }
                    break;
                case BLANK:
                    break;
                case ERROR:
                    break;
                // 处理其他类型的值
                default:
                    break;
            }

        }
        return cellvalue;
    }

    /**
     * @return org.apache.poi.ss.usermodel.Workbook excel工作簿对象
     * @description 从流中读取，上传文件可以直接获取文件流，无需暂存到服务器上
     * @author Lock
     * @date 2021/12/28 10:15
     * @version v1.0
     * @params inputStream 文件输入流
     * @params ext 文件后缀名
     */
    private static Workbook readFromInputStream(InputStream inputStream, String ext) {
        try {
            if (EXCEL2003_SUFFIX_NAME.equals(ext)) {
                // Excel 2003
                return new HSSFWorkbook(inputStream);
            } else {
                // Excel 2007
                return new XSSFWorkbook(inputStream);
            }
        } catch (Exception e) {
            log.error("从流中读取excel文件失败，【readFromInputStream】方法报错，", e);
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            // 创建连接
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/mydatabase", "root", "password");
            PreparedStatement pstmt = null;

            // 打开 Excel 文件
            File file = new File("data.xlsx");
            FileInputStream fis = new FileInputStream(file);

            // 创建工作簿对象
            XSSFWorkbook workbook = new XSSFWorkbook(fis);

            // 读取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);

            // 获取表头
            Row headerRow = sheet.getRow(0);
            int columnCount = headerRow.getLastCellNum();

            // 构造 SQL 语句，? 代表需要填充的数据
            StringBuilder sqlBuilder = new StringBuilder("INSERT INTO my_table (");
            for (int i = 0; i < columnCount; i++) {
                if (i > 0) {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append(headerRow.getCell(i).getStringCellValue());
            }
            sqlBuilder.append(") VALUES (");
            for (int i = 0; i < columnCount; i++) {
                if (i > 0) {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append("?");
            }
            sqlBuilder.append(")");

            // 预编译 SQL 语句
            pstmt = conn.prepareStatement(sqlBuilder.toString());

            // 循环插入数据
            int batchSize = 1000; // 每批次插入的数据条数
            int count = 0;
            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next(); // 跳过表头行
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                for (int i = 0; i < columnCount; i++) {
                    Cell cell = row.getCell(i);
                    if (cell != null) {
                        pstmt.setString(i + 1, cell.toString());
                    } else {
                        pstmt.setString(i + 1, null);
                    }
                }
                pstmt.addBatch();
                count++;
                if (count % batchSize == 0) {
                    pstmt.executeBatch();
                }
            }
            pstmt.executeBatch(); // 执行剩余的数据
            pstmt.close();
            conn.close();
            fis.close();
            System.out.println("数据插入成功");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * sftp连接
     *
     * @param hostName
     * @param port
     * @param userName
     * @param password
     * @param secretKeyPath
     * @return
     */
    public static ChannelSftp connect(String hostName, Integer port, String userName, String password, String secretKeyPath) {
        JSch jSch = new JSch();
        Session session = null;
        ChannelSftp sftp = null;
        Channel channel = null;
        try {
            session = jSch.getSession(userName, hostName, port);
            if (StringUtils.isEmpty(password)) {
                jSch.addIdentity(secretKeyPath);
            } else {
                session.setPassword(password);
            }
            session.setConfig(getSshConfig());
            //设置timeout时间
            session.setTimeout(100000);
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            //解决获取sftp文件中有中文名导致乱码问题
            /*Class cl = ChannelSftp.class;
            Field field = cl.getDeclaredField("server_version");
            field.setAccessible(true);
            field.set(sftp, 2);
            sftp.setFilenameEncoding("GBK");*/

            //获取服务版本，判断是否连接成功
            sftp.getServerVersion();
        } catch (Exception e) {
            log.error("SSH方式连接FTP服务器时有JSchException异常!", e);
            throw new FkException(ResultEnum.SFTP_CONNECTION_ERROR);
        }
        return sftp;
    }

    /**
     * 获取服务配置
     *
     * @return
     */
    private static Properties getSshConfig() {
        Properties sshConfig = new Properties();
        sshConfig.put("StrictHostKeyChecking", "no");
        //sshConfig.put("PreferredAuthentications","publickey,gssapi-with-mic,keyboard-interactive,password");
        return sshConfig;
    }

    /**
     * 预览sftp文件
     *
     * @param sftp
     * @param path
     * @return
     */
    public static InputStream getSftpFileInputStream(ChannelSftp sftp, String path) {
        try {
            InputStream inputStream = sftp.get(path);
            return inputStream;
        } catch (SftpException e) {
            log.error("sftp获取文件流失败,{}", e);
            throw new FkException(ResultEnum.SFTP_PREVIEW_ERROR);
        }
    }

    /**
     * 根据应用id连接ftp数据源, 获取ftp客户端
     *
     * @return org.apache.commons.net.ftp.FTPClient
     * @author Lock
     * @date 2021/12/31 10:24
     */
    private FTPClient getFtpClient(String host, String port, String connectAccount, String connectPwd) {
        FTPClient ftpClient = new FTPClient();
        try {
            // 设置文件传输的编码
            ftpClient.setControlEncoding("utf-8");

            /*
              连接 FTP 服务器
              如果连接失败，则此时抛出异常，如ftp服务器服务关闭时，抛出异常：
              java.net.ConnectException: Connection refused: connect
              */
            ftpClient.connect(host, Integer.parseInt(port));
            /*
              登录 FTP 服务器
              1）如果传入的账号为空，则使用匿名登录，此时账号使用 "Anonymous"，密码为空即可
              */
            if (StringUtils.isBlank(connectAccount)) {
                ftpClient.login("Anonymous", "");
            } else {
                ftpClient.login(connectAccount, connectPwd);
            }

            /*
            设置传输的文件类型
              BINARY_FILE_TYPE：二进制文件类型
              ASCII_FILE_TYPE：ASCII传输方式，这是默认的方式
              ....
             */
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            /*
              确认应答状态码是否正确完成响应
              凡是 2开头的 isPositiveCompletion 都会返回 true，因为它底层判断是：
              return (reply >= 200 && reply < 300);
             */
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                /*
                  如果 FTP 服务器响应错误 中断传输、断开连接
                  abort：中断文件正在进行的文件传输，成功时返回 true,否则返回 false
                  disconnect：断开与服务器的连接，并恢复默认参数值
                 */
                ftpClient.abort();
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            log.error(">>>>>FTP服务器连接登录失败，请检查连接参数是否正确，或者网络是否通畅*********");
            throw new FkException(ResultEnum.FTP_CONNECTION_ERROR);
        }
        return ftpClient;
    }

    /**
     * 根据名称获取文件，以输入流返回
     *
     * @param ftpPath  FTP服务器上文件相对路径，例如：test/123
     * @param fileName 文件名，例如：test.txt
     * @return InputStream 输入流对象
     */
    public static InputStream getInputStreamByName(FTPClient ftpClient, String ftpPath, String fileName) {
        // 登录
        InputStream input = null;
        if (ftpClient != null) {
            try {
                // 判断是否存在该目录,true则切换到该目录
                if (!ftpClient.changeWorkingDirectory(ftpPath)) {
                    System.out.println(ftpPath + "该目录不存在");
                    return input;
                }
                // 设置被动模式，开通一个端口来传输数据
                ftpClient.enterLocalPassiveMode();
                String[] fs = ftpClient.listNames();
                // 判断该目录下是否有文件
                if (fs == null || fs.length == 0) {
                    return input;
                }
                for (String ff : fs) {
                    String ftpName = new String(ff.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                    if (ftpName.equals(fileName)) {
                        input = ftpClient.retrieveFileStream(ff);
                        break;
                    }
                }
            } catch (IOException e) {
                log.error("获取文件失败,{}", e.getMessage());
            } finally {
                closeFtpConnect(ftpClient);
                System.out.println("将excel文件转换成输入流,关闭ftp连接");
                log.info("将excel文件转换成输入流,关闭ftp连接");
            }
        }
        return input;
    }

    /**
     * 使用完毕，应该及时关闭连接
     * 终止 ftp 传输
     * 断开 ftp 连接
     *
     * @param ftpClient ftpClient
     * @return 执行结果
     */
    public static FTPClient closeFtpConnect(FTPClient ftpClient) {
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            log.error("关闭ftp连接失败,{}", e.getMessage());
        }
        return ftpClient;
    }
}

