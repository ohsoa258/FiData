package com.fisk.dataaccess.service.impl;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.app.DbConnectionDTO;
import com.fisk.dataaccess.dto.ftp.ExcelDTO;
import com.fisk.dataaccess.dto.ftp.ExcelTreeDTO;
import com.fisk.dataaccess.dto.ftp.FtpPathDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsQueryDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.dataaccess.enums.FtpFileTypeEnum;
import com.fisk.dataaccess.mapper.AppRegistrationMapper;
import com.fisk.dataaccess.mapper.TableAccessMapper;
import com.fisk.dataaccess.service.IFtp;
import com.fisk.dataaccess.utils.ftp.ExcelUtils;
import com.fisk.dataaccess.utils.ftp.FtpUtils;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.fisk.dataaccess.utils.ftp.FtpUtils.getInputStreamByName;

/**
 * @author Lock
 * @version 1.0
 * @description ftp数据源实现类
 * @date 2021/12/28 10:50
 */
@Slf4j
@Service
public class FtpImpl implements IFtp {

    @Resource
    AppDataSourceImpl dataSourceImpl;

    @Resource
    TableAccessMapper tableAccessMapper;

    @Resource
    AppRegistrationMapper appRegistrationMapper;

    @Override
    public ResultEntity<Object> connectFtp(DbConnectionDTO dto) {
        FTPClient ftpClient = null;
        try {
            if (DataSourceTypeEnum.FTP.getName().equals(dto.driveType)) {
                ftpClient = FtpUtils.connectFtpServer(dto.host, Integer.parseInt(dto.port), dto.connectAccount, dto.connectPwd, "utf-8");
                return ftpClient.isConnected() ? ResultEntityBuild.build(ResultEnum.SUCCESS) : ResultEntityBuild.build(ResultEnum.FTP_CONNECTION_ERROR);
            }
        } finally {
            // 关闭连接
            FtpUtils.closeFtpConnect(ftpClient);
        }
        return null;
    }

    @Override
    public List<ExcelDTO> previewContent(OdsQueryDTO query) {
        FTPClient ftpClient = getFtpClient(query.appId);

        if (StringUtils.isBlank(query.querySql) || FtpFileTypeEnum.judgeSuffixList(query.querySql)) {
            throw new FkException(ResultEnum.FILE_NOT_SELECTED);
        }

        // 重新封装excel参数
        List<String> excelParam = encapsulationExcelParam(query.querySql);

        // 获取文件输入流
        InputStream inputStream = getInputStreamByName(ftpClient, excelParam.get(0), excelParam.get(1));

        switch (query.fileTypeEnum) {
            // 获取excel内容
            case XLS_FILE:
            case XLSX_FILE:
                return ExcelUtils.readExcelFromInputStream(inputStream, excelParam.get(2));
            case CSV_FILE:
                return ExcelUtils.readCsvFromInputStream(inputStream, excelParam.get(3));
            default:
                return null;
        }
    }

    @Override
    public ExcelTreeDTO loadFtpFileSystem(FtpPathDTO dto) {

        // 查询ftp数据源配置信息
        FTPClient ftpClient = getFtpClient(dto.appId);

        AppDataSourcePO dataSourcePo = dataSourceImpl.query().eq("app_id", dto.appId).one();

        // 数据源配置不同的文件后缀名,展示相对应的文件系统
        FtpFileTypeEnum fileTypeEnum = FtpFileTypeEnum.getValue(dataSourcePo.fileSuffix);
        return FtpUtils.listFilesAndDirectorys(ftpClient, dto.fullPath, fileTypeEnum.getName());
    }

    @Override
    public ResultEnum copyFtpFile(int tableAccessId) {
        log.info("【copyFtpFile】请求参数：" + tableAccessId);
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        FTPClient ftpClient = null;
        try {
            if (tableAccessId == 0) {
                return ResultEnum.PARAMTER_NOTNULL;
            }
            TableAccessPO tableAccessPO = tableAccessMapper.selectById(tableAccessId);
            if (tableAccessPO == null || StringUtils.isEmpty(tableAccessPO.getSqlScript())) {
                log.info("【copyFtpFile】tableAccess为空");
                return ResultEnum.DATA_NOTEXISTS;
            }
            AppRegistrationPO appRegistrationPO = appRegistrationMapper.selectById(tableAccessPO.getAppId());
            if (appRegistrationPO == null) {
                log.info("【copyFtpFile】appRegistration为空");
                return ResultEnum.DATA_NOTEXISTS;
            }
            ftpClient = getFtpClient(appRegistrationPO.getId());
            if (ftpClient == null) {
                log.info("【copyFtpFile】ftpClient建立连接失败");
                return ResultEnum.FTP_CONNECTION_ERROR;
            }
            // 判断根目录是否存在Archive文件夹，不存在则创建
            boolean isExist = ftpClient.changeWorkingDirectory("/Archive");
            if (!isExist) {
                log.info("【copyFtpFile】文件目录不存在，创建Archive目录");
                ftpClient.makeDirectory("/Archive");
            }
            // 将源文件复制到Archive文件夹下
            log.info("【copyFtpFile】文件信息：" + tableAccessPO.getSqlScript());
            String[] filePathList = tableAccessPO.getSqlScript().split("/");
            String fileNameValue = filePathList[filePathList.length - 1];
            String[] split = fileNameValue.split("\\.");
            List<String> list = Arrays.asList(split);
            List arrList = new ArrayList(list);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = simpleDateFormat.parse(DateTimeUtils.getNow());
            String format = String.valueOf(date.getTime());

            String suffixName = "." + arrList.get(arrList.size() - 1);
            arrList.remove(arrList.size() - 1);
            String fileName = Joiner.on(".").join(arrList) + "_" + format + suffixName;
            String toPath = "/Archive/" + fileName;
            ftpClient.rename(tableAccessPO.getSqlScript(), toPath);
        } catch (Exception ex) {
            log.error("【copyFtpFile】触发系统异常：" + ex);
            resultEnum = ResultEnum.ERROR;
        } finally {
            if (ftpClient != null) {
                FtpUtils.closeFtpConnect(ftpClient);
            }
        }
        return resultEnum;
    }

    /**
     * 根据应用id连接ftp数据源, 获取ftp客户端
     *
     * @param appId 应用id
     * @return org.apache.commons.net.ftp.FTPClient
     * @author Lock
     * @date 2021/12/31 10:24
     */
    private FTPClient getFtpClient(long appId) {
        // 查询ftp数据源配置信息
        AppDataSourcePO dataSourcePo = dataSourceImpl.query().eq("app_id", appId).one();
        // 参数校验
        if (dataSourcePo == null) {
            throw new FkException(ResultEnum.FTP_CONNECTION_INVALID);
        }
        return FtpUtils.connectFtpServer(
                dataSourcePo.host,
                Integer.parseInt(dataSourcePo.port),
                dataSourcePo.connectAccount,
                dataSourcePo.connectPwd,
                "utf-8");
    }

    /**
     * 封装读取excel文件内容所需参数
     *
     * @param textFullPath 文件全路径
     * @return java.util.List<java.lang.String>
     * @author Lock
     * @date 2021/12/29 11:01
     */
    public List<String> encapsulationExcelParam(String textFullPath) {
        List<String> param = new ArrayList<>();
        // ["/Windows/二级/tb_app_registration", "xlsx"]
        String[] split = textFullPath.split("\\.");
        // .xlsx
        String suffixName = "." + split[1];

        String[] split1 = split[0].split("/");
        // tb_app_registration
        String fileName = split1[split1.length - 1];
        // tb_app_registration.xlsx
        String fileFullName = fileName + suffixName;
        // /Windows/二级/
        String path = textFullPath.replace(fileFullName, "");

        // 全目录路径
        param.add(path);
        // 文件全名
        param.add(fileFullName);
        // 文件后缀名
        param.add(suffixName);
        param.add(fileName);
        return param;
    }
}
