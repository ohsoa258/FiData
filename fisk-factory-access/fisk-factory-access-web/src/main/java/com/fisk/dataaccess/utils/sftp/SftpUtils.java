package com.fisk.dataaccess.utils.sftp;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.ftp.ExcelPropertyDTO;
import com.fisk.dataaccess.dto.ftp.ExcelPropertySortDTO;
import com.fisk.dataaccess.dto.ftp.ExcelTreeDTO;
import com.fisk.dataaccess.dto.ftp.ExcelTreeSortDTO;
import com.fisk.dataaccess.enums.SortTypeEnum;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

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
            session.setTimeout(60000);
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            //解决获取sftp文件中有中文名导致乱码问题
            Class cl = ChannelSftp.class;
            Field field = cl.getDeclaredField("server_version");
            field.setAccessible(true);
            field.set(sftp, 2);
            sftp.setFilenameEncoding("GBK");

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
    public static ExcelTreeDTO getFile(ChannelSftp sftp, String path, String fileType) {
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
    }

    /**
     * 过滤错误文件夹
     *
     * @param fileName
     * @return
     */
    public boolean filterFileName(String fileName) {
        if (".".equals(fileName)) {
            return true;
        } else if ("..".equals(fileName)) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 按照时间查询指定文件
     * @param sftp sftp连接
     * @param sortType 排序类型（1：顺序，2：倒序）
     * @param path 需要查询的文件目录
     * @param fileType  文件类型
     * @return
     */
    public ExcelTreeSortDTO getSortFile(ChannelSftp sftp, Integer sortType, String path, String fileType) {
        ExcelTreeSortDTO list = new ExcelTreeSortDTO();
        try {
            // 文件
            List<ExcelPropertySortDTO> fileList = new ArrayList<>();
            // 文件夹
            List<ExcelPropertyDTO> directoryList = new ArrayList<>();
            fileType = "." + fileType;
            Vector vector = sftp.ls(path);
            Iterator iterator = vector.iterator();
            while (iterator.hasNext()) {
                ChannelSftp.LsEntry file = (ChannelSftp.LsEntry) iterator.next();
                Integer modifyTime = file.getAttrs().getMTime();
                String fileName = file.getFilename();
                //获取指定文件
                if (fileName.contains(fileType)) {
                    ExcelPropertySortDTO dto = new ExcelPropertySortDTO();
                    dto.fileName = fileName;
                    dto.fileFullName = path + fileName;
                    dto.setModifyTime(modifyTime);
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
            fileList = sortFile(fileList, sortType);
            list.fileList = fileList;
            list.directoryList = directoryList;
        } catch (SftpException e) {
            log.error("sftp获取文件失败,{}", e);
            return null;
        } finally {
            disconnect(sftp);
        }
        return list;
    }

    /**
     * 按照排序类型对文件列表进行排序
     *
     * @param fileList 文件列表
     * @param sortType 排序类型（1：正序，2：倒序）
     * @return
     */
    public static List<ExcelPropertySortDTO> sortFile(List<ExcelPropertySortDTO> fileList, Integer sortType){
        if (sortType.equals(SortTypeEnum.POSITIVE_SORT.getValue())){
            // 正序排序
            fileList.sort(new Comparator<ExcelPropertySortDTO>() {
                @Override
                public int compare(ExcelPropertySortDTO o1, ExcelPropertySortDTO o2) {
                    if(o1.modifyTime < o2.modifyTime){
                        return -1;
                    }else if(o1.modifyTime.equals(o2.modifyTime)){
                        return 0;
                    }else {
                        return 1;
                    }
                }
            });
        }
        if (sortType.equals(SortTypeEnum.REVERSE_SORT.getValue())){
            // 倒序排序
            fileList.sort(new Comparator<ExcelPropertySortDTO>() {
                @Override
                public int compare(ExcelPropertySortDTO o1, ExcelPropertySortDTO o2) {
                    if(o1.modifyTime < o2.modifyTime){
                        return 1;
                    }else if(o1.modifyTime.equals(o2.modifyTime)){
                        return 0;
                    }else {
                        return -1;
                    }
                }
            });
        }
        return fileList;
    }

    /**
     * 下载指定目录下的文件
     *
     * @param sftp sftp连接
     * @param dir 文件目录
     * @param fileName 文件名
     * @return InputStream
     * @throws SftpException
     */
    public static InputStream downloadStream(ChannelSftp sftp, String dir, String fileName) throws SftpException {
        if (StringUtils.isNotEmpty(dir)) {
            sftp.cd(dir);
        }
        return sftp.get(fileName);
    }

    /**
     * 指定文件名，并以文件流的形式传输到目标服务器中的指定目录下
     *
     * @param sftp sftp链接
     * @param ins 文件输入流
     * @param dir 文件上传后所在的目录名
     * @param fileName 文件上传后的名称
     */
    public static boolean uploadFile(ChannelSftp sftp, InputStream ins, String dir, String fileName) throws IOException {
        boolean flag = false;
        try{
            // 进入到当前目录并写入文件
            sftp.cd(dir);
            sftp.put(ins, fileName);
            flag = false;
        } catch (SftpException e) {
            log.error("sftp上传文件失败，{}", e);
            throw new FkException(ResultEnum.UPLOAD_ERROR);
        }finally {
            if(ins != null){
                ins.close();
            }
        }
        return flag;
    }

    /**
     * sftp连接将指定文件复制到目标sftp上
     *
     * @param currSftp 当前源sftp
     * @param targetSftp 目标源sftp
     * @param currDir 当前源文件所在目录
     * @param currFileName 当前源文件名称
     * @param targetDir 复制后的文件所在路径
     * @param targetFileName 复制后的文件名称
     * @return
     */
    public static void copyFile(ChannelSftp currSftp, ChannelSftp targetSftp, String currDir, String currFileName,
                                   String targetDir, String targetFileName) throws SftpException, IOException {
        // 加载文件流
        InputStream ins = downloadStream(currSftp, currDir, currFileName);

        // 上传文件
        uploadFile(targetSftp, ins, targetDir, targetFileName);
    }

}
