package com.fisk.common.service.flinkupload;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author JianWenYang
 */
@Component
public class FlinkUploadUtils {

    public static Session SSHConnection(String host, int port, String user, String password) {
        JSch jsch = new JSch();
        Session session;
        try {
            // 创建session并且打开连接，因为创建session之后要主动打开连接
            session = jsch.getSession(user, host, port);
            session.setPassword(password);
            // 连接时不进行公钥确认，如果第一次登陆会让你确定是否接受公钥，改配置跳过这一步
            session.setConfig("StrictHostKeyChecking", "no");
            // 设置超时时间
            session.setTimeout(6000);
            session.connect();
            return session;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String ExecCommand(Session session, String command) {
        try {
            // 打开通道，设置通道类型，和执行的命令
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            channelExec.setErrStream(System.err);
            channelExec.connect();
            InputStreamReader reader = new InputStreamReader(channelExec.getInputStream());
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            channelExec.disconnect();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

}
