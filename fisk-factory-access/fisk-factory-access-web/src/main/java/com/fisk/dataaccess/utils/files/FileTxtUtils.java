package com.fisk.dataaccess.utils.files;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Properties;

/**
 * @author JianWenYang
 */
@Component
@Slf4j
public class FileTxtUtils {

    private static ChannelSftp sftp = null;

    /**
     * 生成文件
     *
     * @param path
     * @param fileName
     * @param data
     */
    public static void setFiles(String path, String fileName, String data) {
        BufferedWriter out = null;
        try {
            path = path + fileName;
            //相对路径，如果没有则要建立一个新的output.txt文件
            File writeName = new File(path);
            if (!writeName.exists()) {
                //创建新文件,有同名的文件的话直接覆盖
                writeName.createNewFile();
            }
            FileWriter writer = new FileWriter(writeName);
            out = new BufferedWriter(writer);
            out.write(data);
            //把缓存区内容压入文件
            out.flush();

            //InputStream client_fileInput = new FileInputStream(path);
            //uploadFile("192.168.1.92",22,"root","p/q2-q4!","/root/flink-job/sql/",fileName,client_fileInput);

        } catch (IOException e) {
            log.error("setFiles ex:", e);
        } finally {
            closeBufferedWriter(out);
        }
    }

    public static void closeBufferedWriter(BufferedWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                log.error("【closeBufferedWriter】关闭文本报错, ex", e);
            }
        }
    }

    /**
     * 将文件上传远程Linux服务器
     *
     * @param host
     * @param port
     * @param username
     * @param password
     * @param filePath
     * @param filename
     * @param input
     * @return
     */
    public static boolean uploadFile(String host, int port, String username, String password,
                                     String filePath, String filename, InputStream input) {
        boolean result = false;
        FTPClient ftp = new FTPClient();
        File file = null;
        try {
            JSch jsch = new JSch();
            //获取sshSession  账号-ip-端口
            Session sshSession = jsch.getSession(username, host, port);
            //添加密码
            sshSession.setPassword(password);
            Properties sshConfig = new Properties();
            //严格主机密钥检查
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            //开启sshSession链接
            sshSession.connect();
            //获取sftp通道
            Channel channel = sshSession.openChannel("sftp");
            //开启
            channel.connect();
            sftp = (ChannelSftp) channel;
            file = new File(filePath);
            //设置为被动模式
            ftp.enterLocalPassiveMode();
            //设置上传文件的类型为二进制类型
            //进入到要上传的目录  然后上传文件
            sftp.cd(filePath);
            sftp.put(input, filename);
            input.close();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return result;
    }

}
