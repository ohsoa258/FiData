package com.fisk.common.core.utils.sftp;

import com.fisk.common.core.enums.sftp.SftpAuthTypeEnum;
import com.fisk.common.core.enums.sftp.SortTypeEnum;
import com.fisk.common.core.enums.sftp.SortTypeNameEnum;
import com.fisk.common.core.enums.sftp.SourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.Dto.sftp.FilePropertySortDTO;
import com.fisk.common.core.utils.Dto.sftp.FileTreeSortDTO;
import com.fisk.common.core.utils.Dto.sftp.SftpExcelTreeDTO;
import com.fisk.common.core.utils.FileBinaryUtils;
import com.fisk.common.framework.exception.FkException;
import com.jcraft.jsch.*;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Slf4j
public class SftpUtils {

    private static final String ROOT_PATH = "/";

   /* @Test
    public void test(){
        ChannelSftp sftp = connect("192.168.21.21",
                22,
                "sftp",
                "password01!",
                "");
        ExcelTreeDTO file = getFile(sftp, "/upload", "xlsx");
        String test = "";

    }*/

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
            if (!StringUtils.isEmpty(secretKeyPath)) {
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
     * 关闭连接
     *
     * @param sftp
     */
    public static void disconnect(ChannelSftp sftp) {
        try {
            if (sftp != null) {
                if (sftp.getSession().isConnected()) {
                    sftp.getSession().disconnect();
                }
            }
        } catch (Exception e) {
            log.error("关闭与sftp服务器会话连接异常", e);
        }
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
     * 解析全路径
     *
     * @param textFullPath
     * @return
     */
    public static List<String> encapsulationExcelParam(String textFullPath) {
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

    /**
     * 获取sftp文件夹和指定文件
     *
     * @param sftp
     * @param path
     * @param fileType
     * @return
     */
    /*public static ExcelTreeDTO getFile(ChannelSftp sftp, String path, String fileType) {
        ExcelTreeDTO list = new ExcelTreeDTO();
        try {
            // 文件
            List<ExcelPropertyDTO> fileList = new ArrayList<>();
            // 文件夹
            List<ExcelPropertyDTO> directoryList = new ArrayList<>();
            fileType = "." + fileType;
            Vector vector = sftp.ls(path);
            Iterator iterator = vector.iterator();
            while (iterator.hasNext()) {
                ChannelSftp.LsEntry file = (ChannelSftp.LsEntry) iterator.next();
                String fileName = file.getFilename();
                //获取指定文件
                if (fileName.contains(fileType)) {
                    ExcelPropertyDTO dto = new ExcelPropertyDTO();
                    dto.fileName = fileName;
                    dto.fileFullName = path + fileName;
                    fileList.add(dto);
                } else {
                    if (filterFileName(fileName)) {
                        continue;
                    }
                    ExcelPropertyDTO dto = new ExcelPropertyDTO();
                    dto.fileName = fileName;
                    dto.fileFullName = path + fileName + ROOT_PATH;
                    directoryList.add(dto);
                }
            }
            list.fileList = fileList;
            list.directoryList = directoryList;
        } catch (SftpException e) {
            log.error("sftp获取文件失败,{}", e);
            return null;
        } finally {
            disconnect(sftp);
        }
        return list;
    }*/

    /**
     * 递归获取sftp文件和文件夹
     *
     * @param sftp
     * @param path
     * @param fileType
     * @return
     */
    public static SftpExcelTreeDTO getFile(ChannelSftp sftp, String path, String fileType) {
        log.info("参数信息，目录路径-[{}], 文件类型[{}]", path, fileType);
        // 校验目录格式
        if (path.length() != 1 && !path.endsWith("/")) {
            throw new FkException(ResultEnum.SFTP_DIR_PATH_ERROR);
        }

        // 初始化顶级节点
        SftpExcelTreeDTO root = new SftpExcelTreeDTO();
        String fileName = null;
        if (!path.equals("/") && path.endsWith("/")) {
            // 去掉开始、结尾字符
            fileName = path.substring(1);
            fileName = fileName.substring(0, fileName.length() - 1);
        } else {
            fileName = path;
        }
        root.setFileName(fileName);
        root.setFileFullName(path);
        root.setDirFlag(true);

        // 处理文件类型
        fileType = "." + fileType;
        try {
            root.getChildren().addAll(recurFile(sftp, path, fileType));
        } catch (SftpException e) {
            log.info("sftp读取文件失败,{}", e);
            return null;
        } finally {
            disconnect(sftp);
        }
        return root;
    }

    /**
     * 递归获取文件及目录树结构
     *
     * @param sftp     sftp连接
     * @param path     目录路径
     * @param fileType 文件类型，不包含"."符号
     * @return
     * @throws SftpException
     */
    private static List<SftpExcelTreeDTO> recurFile(ChannelSftp sftp, String path, String fileType) throws SftpException {
        List<SftpExcelTreeDTO> tree = new ArrayList<>();
        Vector<ChannelSftp.LsEntry> vectors = sftp.ls(path);
        if (vectors.size() != 0) {
            for (ChannelSftp.LsEntry entry : vectors) {
                SftpExcelTreeDTO dto = new SftpExcelTreeDTO();
                dto.setFileName(entry.getFilename());
                dto.setDirFlag(entry.getAttrs().isDir());
                if (entry.getAttrs().isDir()) {
                    // 过滤错误文件夹
                    if (filterFileName(entry.getFilename())) {
                        continue;
                    }
                    dto.setFileFullName(path + entry.getFilename() + ROOT_PATH);
                    if (!entry.getFilename().equals(".ssh")) {
                        dto.getChildren().addAll(recurFile(sftp, dto.getFileFullName(), fileType));
                    }
                } else {
                    // 过滤文件类型
                    if (!entry.getFilename().contains(fileType)) {
                        continue;
                    }
                    dto.setFileFullName(path + entry.getFilename());
                }
                tree.add(dto);
            }
        }
        return tree;
    }

    /**
     * 过滤错误文件夹
     *
     * @param fileName
     * @return
     */
    public static boolean filterFileName(String fileName) {
        if (".".equals(fileName)) {
            return true;
        } else if ("..".equals(fileName)) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 按照时间或文件名进行顺序/倒序查询文件列表
     *
     * @param sftp         源服务器sftp连接
     * @param sortTypeName 排序类型（1：文件名，2：时间）
     * @param sortType     排序顺序类型（1：正序，2：默认，3：倒序）
     * @param path         需要查询的文件目录路径
     * @return
     */
    public static FileTreeSortDTO getSortFile(ChannelSftp sftp, Integer sortTypeName, Integer sortType, String path, String wildcardText) {
        log.info("排序获取文件参数：排序方式-[{}],顺序-[{}],路径-[{}]", sortTypeName, sortType, path);
        FileTreeSortDTO list = new FileTreeSortDTO();
        try {
            String wildcard = getWildcard(wildcardText);
            // 文件
            List<FilePropertySortDTO> fileList = new ArrayList<>();
            Vector vector = sftp.ls(path);
            Iterator iterator = vector.iterator();
            while (iterator.hasNext()) {
                ChannelSftp.LsEntry file = (ChannelSftp.LsEntry) iterator.next();
                Integer modifyTime = file.getAttrs().getMTime();
                String fileName = file.getFilename();
                // 排除文件夹
                if (fileName.equals(".") || fileName.equals("..") || !fileName.contains(".")) {
                    continue;
                }
                boolean isAdd = true;
                if (StringUtils.isNotEmpty(wildcard)) {
                    Pattern r = Pattern.compile(wildcard);
                    String t_fileName = fileName.substring(0, fileName.lastIndexOf(".")).toLowerCase();
                    Matcher matches = r.matcher(t_fileName);
                    if (!matches.find()) {
                        isAdd = false;
                    }
                }
                if (isAdd) {
                    // 获取指定文件
                    FilePropertySortDTO dto = new FilePropertySortDTO();
                    dto.fileName = fileName;
                    dto.fileFullName = path + fileName;
                    dto.setModifyTime(modifyTime);
                    fileList.add(dto);
                }
            }
            // 是否进行排序
            if (!fileList.isEmpty() && !sortType.equals(SortTypeEnum.DEFAULT_SORT.getValue())) {
                fileList = sortFile(fileList, sortTypeName, sortType, SourceTypeEnum.SFTP);
            }
            list.fileList = fileList;
        } catch (SftpException e) {
            log.error("sftp获取文件失败,{}", e);
            return null;
        }
        return list;
    }

    /**
     * 对文件进行排序
     *
     * @param fileList     待排序文件列表
     * @param sortTypeName 排序类型（1：文件名，2：时间）
     * @param sortType     排序顺序类型（1：正序，2：默认，3：倒序）
     * @return
     */
    private static List<FilePropertySortDTO> sortFile(List<FilePropertySortDTO> fileList, Integer sortTypeName, Integer sortType, SourceTypeEnum sourceType) {
        if (sortTypeName.equals(SortTypeNameEnum.TIME_SORT.getValue())) {
            if (sourceType == SourceTypeEnum.SFTP) {
                if (sortType.equals(SortTypeEnum.POSITIVE_SORT.getValue())) {
                    // 正序排序
                    fileList = fileList.stream().sorted(Comparator.comparing(FilePropertySortDTO::getModifyTime))
                            .collect(Collectors.toList());
                } else {
                    // 倒序排序
                    fileList = fileList.stream().sorted(Comparator.comparing(FilePropertySortDTO::getModifyTime).reversed())
                            .collect(Collectors.toList());
                }
            } else if (sourceType == SourceTypeEnum.WINDOWS) {
                if (sortType.equals(SortTypeEnum.POSITIVE_SORT.getValue())) {
                    // 正序排序
                    fileList = fileList.stream().sorted(Comparator.comparing(FilePropertySortDTO::getLastModified))
                            .collect(Collectors.toList());
                } else {
                    // 倒序排序
                    fileList = fileList.stream().sorted(Comparator.comparing(FilePropertySortDTO::getLastModified).reversed())
                            .collect(Collectors.toList());
                }
            }
        }

        // 文件名排序
        if (sortTypeName.equals(SortTypeNameEnum.FILENAME_SORT.getValue())) {
            if (sortType.equals(SortTypeEnum.POSITIVE_SORT.getValue())) {
                fileList = fileList.stream().sorted(Comparator.comparing(FilePropertySortDTO::getFileName))
                        .collect(Collectors.toList());
            } else {
                fileList = fileList.stream().sorted(Comparator.comparing(FilePropertySortDTO::getFileName).reversed())
                        .collect(Collectors.toList());
            }
        }
        log.info("排序后的文件名:{}", fileList);
        return fileList;
    }

    /**
     * 对文件进行重新排序，并根据指定的索引获取对应文件的InputStream流
     *
     * @param sftp         源服务器sftp
     * @param sortTypeName 排序类型（1：文件名，2：时间）
     * @param sortType     排序顺序类型（1：正序，2：默认，3：倒序）
     * @param index        需要复制的文件位置索引
     * @param dir          文件所在目录
     * @return
     * @throws SftpException
     */
    public static InputStream getFileInputStream(ChannelSftp sftp, Integer sortTypeName, Integer sortType,
                                                 Integer index, String dir, String wildcardText) throws SftpException {
        // 获取排序后的文件列表
        FileTreeSortDTO sortFileDto = getSortFile(sftp, sortTypeName, sortType, dir, wildcardText);
        if (sortFileDto == null || sortFileDto.getFileList().isEmpty()) {
            throw new FkException(ResultEnum.SFTP_FILE_IS_NULL);
        }
        List<FilePropertySortDTO> fileList = sortFileDto.getFileList();

        // 文件索引校验
        if (index <= 0 || index > fileList.size()) {
            throw new FkException(ResultEnum.SFTP_FILE_INDEX_ERROR);
        }
        // 根据索引查询目标文件DTO
        FilePropertySortDTO filePropertySortDTO = fileList.get(index - 1);
        String path = filePropertySortDTO.getFileFullName();

        // 文件路径及名称处理
        String[] splits = path.split("/");
        String fileName = splits[splits.length - 1];
        if (StringUtils.isNotEmpty(dir)) {
            sftp.cd(dir);
        }

        return sftp.get(fileName);
    }

    /**
     * 指定文件名，并以文件流的形式传输到目标服务器中的指定目录下
     *
     * @param sftp     目标服务器sftp链接
     * @param ins      文件输入流
     * @param dir      文件上传后所在的目录名
     * @param fileName 文件上传后的新文件名称
     */
    public static boolean uploadFile(ChannelSftp sftp, InputStream ins, SmbFileInputStream smbIns, String dir, String fileName) throws IOException {
        boolean flag = false;
        try {
            // 进入到当前目录并写入文件
            sftp.cd(dir);
            if (smbIns != null) {
                sftp.put(smbIns, fileName);
            } else {
                sftp.put(ins, fileName);
            }
            flag = true;
        } catch (SftpException e) {
            log.error("sftp上传文件失败，{}", e);
            throw new FkException(ResultEnum.UPLOAD_ERROR);
        }
        return flag;
    }

    /**
     * 上传二进制密钥字符串文件到服务器指定目录下
     *
     * @param fileBinary 二进制密钥文件字符串
     * @param linuxPath  服务器存储路径
     * @param fileName   文件名
     * @param userName   账号
     * @param pw         密码
     * @param rsaPath
     * @param host       主机地址
     * @param port       端口
     * @return
     * @throws IOException
     */
    public static boolean uploadRsaFile(String fileBinary, String linuxPath, String fileName,
                                        String userName, String pw, String rsaPath, String host, Integer port) {
        InputStream inputStream = null;
        try {
            inputStream = FileBinaryUtils.getInputStream(fileBinary);
            ChannelSftp root = SftpUtils.getSftpConnect(SftpAuthTypeEnum.USERNAME_PW_AUTH.getValue(),
                    userName, pw, rsaPath, host, port);
            return SftpUtils.uploadFile(root, inputStream, null, linuxPath, fileName);
        } catch (Exception e) {
            log.error("上传二进制文件失败{}", e);
            return false;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭流
     *
     * @param ins
     */
    public static void closeInputStream(InputStream ins) {
        try {
            if (ins != null) {
                ins.close();
            }
        } catch (Exception e) {
            log.error("关闭InputStream流异常", e);
        }
    }

    /**
     * 将源sftp服务器中的指定文件复制到目标sftp服务器中
     *
     * @param rHost          源sftp地址
     * @param rPort          源sftp端口
     * @param rUserName      源sftp用户名
     * @param rPw            源sftp密码
     * @param rKey           源sftp-rsa文件路径
     * @param rAuthType      源sftp认证类型
     * @param tHost          目标sftp端口
     * @param tPort          目标sftp端口
     * @param tUserName      目标sftp用户名
     * @param tPw            目标sftp密码
     * @param tKey           目标sftp-rsa文件路径
     * @param tAuthType      目标sftp认证类型
     * @param sortTypeName   排序类型（1：文件名，2：时间）
     * @param sortType       排序顺序类型（1：正序，2：默认，3：倒序）
     * @param index          需要复制的第几个文件
     * @param currDir        需要复制的文件所在目录
     * @param targetDir      复制后文件所在目录
     * @param targetFileName 复制后的新文件名称
     * @throws SftpException
     * @throws IOException
     */
    public static void copyFile(String rHost, Integer rPort, String rUserName, String rPw, String rKey, Integer rAuthType,
                                String tHost, Integer tPort, String tUserName, String tPw, String tKey, Integer tAuthType,
                                Integer sortTypeName, Integer sortType, Integer index, String currDir,
                                String targetDir, String targetFileName, String wildcardText) {
        InputStream ins = null;
        ChannelSftp currSftp = null;
        ChannelSftp targetSftp = null;
        try {
            // 初始化sftp连接
            log.info("开始连接数据源");
            currSftp = getSftpConnect(rAuthType, rUserName, rPw, rKey, rHost, rPort);

            log.info("数据源连接成功,开始连接目标数据");
            targetSftp = getSftpConnect(tAuthType, tUserName, tPw, tKey, tHost, tPort);

            log.info("目标数据源连接成功,开始获取字节流");
            // 获取文件字节流
            ins = getFileInputStream(currSftp, sortTypeName, sortType, index, currDir, wildcardText);
            log.info("字节流获取完成,开始上传文件");
            // 上传文件
            uploadFile(targetSftp, ins, null, targetDir, targetFileName);
            log.info("文件上传成功");
        } catch (Exception e) {
            log.error("sftp文件复制失败，{}", e);
            throw new FkException(ResultEnum.SFTP_FILE_COPY_FAIL);
        } finally {
            closeInputStream(ins);
            disconnect(currSftp);
            disconnect(targetSftp);
        }
    }

    public static ChannelSftp getSftpConnect(Integer authType, String userName, String pw, String rsaPath,
                                             String host, Integer port) {
        ChannelSftp sftp = null;
        if (authType == SftpAuthTypeEnum.RSA_AUTH.getValue()) {
            // 密钥认证
            if (StringUtils.isEmpty(rsaPath)) {
                throw new FkException(ResultEnum.SFTP_RSA_IS_NULL);
            }
            sftp = connect(host, port, userName, pw, rsaPath);
        } else {
            // 密码认证
            if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(pw)) {
                throw new FkException(ResultEnum.SFTP_ACCOUNT_IS_NULL);
            }
            sftp = connect(host, port, userName, pw, null);
        }
        return sftp;
    }

    /**
     * @return void
     * @description 读取Windows共享目录下的文件并上传到SFTP
     * @author dick
     * @date 2023/2/14 19:05
     * @version v1.0
     * @params smbUserName Windows账号
     * @params smbPassWord Windows密码
     * @params smbIp Windows IP
     * @params smbPath Windows共享文件夹路径
     * @params wildcardText 带文本的通配符
     * @params targetDir 复制后文件所在目录
     * @params targetFileName 复制后的新文件名称
     * @params sortTypeName 排序类型（1：文件名，2：时间）
     * @params sortType 排序顺序类型（1：正序，2：默认，3：倒序）
     * @params index 需要复制的第几个文件
     * @params sftpAuthType 目标sftp认证类型
     * @params sftpUserName 目标sftp用户名
     * @params sftpPassWord 目标sftp密码
     * @params rsaPath 目标sftp-rsa文件路径
     * @params sftpHost 目标sftp主机
     * @params sftpPort 目标sftp端口
     */
    public static void copySmbFilesToSFTP(String smbUserName, String smbPassWord, String smbIp, String smbPath,
                                          String wildcardText, String targetDir, String targetFileName,
                                          Integer sortTypeName, Integer sortType, Integer index,
                                          Integer sftpAuthType, String sftpUserName, String sftpPassWord,
                                          String rsaPath, String sftpHost, Integer sftpPort) {
        if (StringUtils.isEmpty(smbUserName) || StringUtils.isEmpty(smbPassWord) || StringUtils.isEmpty(smbIp)
                || StringUtils.isEmpty(smbPath) || StringUtils.isEmpty(targetDir) || StringUtils.isEmpty(targetFileName)) {
            throw new FkException(ResultEnum.PARAMTER_NOTNULL);
        }
        log.info(String.format("【copySmbFilesToSFTP】请求参数：" +
                        "smbUserName：%s，" +
                        "smbPassWord：%s，" +
                        "smbIp：%s，" +
                        "smbPath：%s，" +
                        "wildcardText：%s，" +
                        "targetDir：%s，" +
                        "targetFileName：%s，" +
                        "sortTypeName：%s，" +
                        "sortType：%s，" +
                        "index：%s，" +
                        "sftpAuthType：%s，" +
                        "sftpUserName：%s，" +
                        "sftpPassWord：%s，" +
                        "rsaPath：%s，" +
                        "sftpHost：%s，" +
                        "sftpPort：%s", smbUserName, smbPassWord, smbIp, smbPath, wildcardText, targetDir,
                targetFileName, sortTypeName, sortType, index, sftpAuthType, sftpUserName, sftpPassWord,
                rsaPath, sftpHost, sftpPort)
        );
        ChannelSftp targetSftp = null;
        SmbFileInputStream smbFileInputStream = null;
        try {
            List<FilePropertySortDTO> fileList = new ArrayList<>();

            // 第一步：读取Windows共享目录下的文件
            String wildcard = getWildcard(wildcardText);
            String remoteUrl = String.format("smb://%s:%s@%s%s", smbUserName, smbPassWord, smbIp, smbPath);
            SmbFile remoteFile = new SmbFile(remoteUrl);
            remoteFile.connect();
            if (remoteFile.exists()) {
                SmbFile[] smbFiles = remoteFile.listFiles();
                for (SmbFile smbFile : smbFiles) {
                    FilePropertySortDTO dto = new FilePropertySortDTO();
                    if (!smbFile.isFile()) {
                        continue;
                    }
                    boolean isAdd = true;
                    if (StringUtils.isNotEmpty(wildcard)) {
                        Pattern r = Pattern.compile(wildcard);
                        String fileName = smbFile.getName().substring(0, smbFile.getName().lastIndexOf(".")).toLowerCase();
                        Matcher matches = r.matcher(fileName);
                        if (!matches.find()) {
                            isAdd = false;
                        }
                    }
                    if (isAdd) {
                        dto.setFileName(smbFile.getName());
                        dto.setFileFullName(smbFile.getCanonicalPath());
                        Date date = new Date(smbFile.lastModified());
                        dto.setLastModified(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                        dto.setSmbFile(smbFile);
                        fileList.add(dto);
                    }
                }
            }
            if (CollectionUtils.isEmpty(fileList)) {
                log.info(String.format("【copySmbFilesToSFTP】未读取到匹配的文件。文本通配符：%s，正则表达式通配符：%s", wildcardText, wildcard));
                return;
            }

            // 第二步：按照配置对文件排序并取出指定下标的文件
            fileList = sortFile(fileList, sortTypeName, sortType, SourceTypeEnum.WINDOWS);
            if (index <= 0 || index > fileList.size()) {
                throw new FkException(ResultEnum.SFTP_FILE_INDEX_ERROR);
            }
            FilePropertySortDTO file = fileList.get(index - 1);
            smbFileInputStream = new SmbFileInputStream(file.getSmbFile());

            // 第三步：文件保存到SFTP服务器，存在则覆盖
            targetSftp = getSftpConnect(sftpAuthType, sftpUserName, sftpPassWord, rsaPath, sftpHost, sftpPort);
            boolean b = uploadFile(targetSftp, null, smbFileInputStream, targetDir, targetFileName);
            if (b) {
                log.info("copySmbFilesToSFTP-Success");
            }
        } catch (Exception ex) {
            log.error("【windows】sftp文件复制失败，{}", ex);
            throw new FkException(ResultEnum.SFTP_FILE_COPY_FAIL);
        } finally {
            closeInputStream(smbFileInputStream);
            disconnect(targetSftp);
        }
    }

    /**
     * @return java.lang.String
     * @description 获取正则表达式通配符
     * @author dick
     * @date 2023/2/14 18:00
     * @version v1.0
     * @params wildcardText
     */
    public static String getWildcard(String wildcardText) {
        String wildcard = "";
        if (StringUtils.isEmpty(wildcardText)) {
            return wildcard;
        }
        wildcard = wildcardText.replace("%", "");
        if (StringUtils.isEmpty(wildcard)) {
            return wildcard;
        }
        if (wildcard.length() <= 1) {
            return wildcard;
        }
        if (wildcardText.startsWith("%") && wildcardText.endsWith("%")) {
            wildcard = wildcardText.substring(1);
            wildcard = wildcard.substring(0, wildcard.length() - 1);
            wildcard = String.format(".*%s.*", wildcard);
        } else if (wildcardText.endsWith("%")) {
            wildcard = wildcardText.substring(0, wildcardText.length() - 1);
            wildcard = String.format("^%s", wildcard);
        } else if (wildcardText.startsWith("%")) {
            wildcard = wildcardText.substring(1);
            wildcard = String.format("%s$", wildcard);
        }
        return wildcard.toLowerCase();
    }

    public static void main(String[] args) throws IOException, SftpException {
        // 上传二进制文件测试
//        File file = new File("C:\\test\\id_rsa_npw");
//        byte[] fileBinary = new byte[(int) file.length()];
//        FileInputStream fis = new FileInputStream(file);
//        fis.read(fileBinary);
//        SftpUtils.uploadRsaFile(fileBinary.toString(), "/upload/test/", "rsa", "sftp", "password01!",
//                null, "192.168.21.21", 22);
//        fis.close();
        getSftpConnect(0, "sftp", "password01!", "", "192.168.21.21", 22);
        copyFile("192.168.21.21", 22,  "sftp", "password01!", "", 0,
                "192.168.21.21", 22,  "sftp", "password01!", "", 0,
                1, 3, 4, "/upload/test/",
                "/upload/test/测试/", "测试测试.xlsx", "");
    }

}