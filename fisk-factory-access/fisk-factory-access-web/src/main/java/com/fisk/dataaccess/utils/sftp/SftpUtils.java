package com.fisk.dataaccess.utils.sftp;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.ftp.ExcelPropertyDTO;
import com.fisk.dataaccess.dto.ftp.ExcelTreeDTO;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author JianWenYang
 */
@Slf4j
public class SftpUtils {

    private static final String ROOT_PATH = "/";
    private static final String secreKeyPath1 = "D:\\WeChatData\\WeChat Files\\wxid_mpbfv3jjxgjd12\\FileStorage\\File\\2022-11\\id_rsa_sftp2";

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
     * @return
     */
    public static ChannelSftp connect(String hostName, Integer port, String userName, String password, String secretKeyPath) {
        JSch jSch = new JSch();
        Session session = null;
        ChannelSftp sftp = null;
        Channel channel = null;
        try {
            secretKeyPath = secreKeyPath1;
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

            //解决获取sftp中文文件名乱码问题
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
    public ExcelTreeDTO getFile(ChannelSftp sftp, String path, String fileType) {
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


}
