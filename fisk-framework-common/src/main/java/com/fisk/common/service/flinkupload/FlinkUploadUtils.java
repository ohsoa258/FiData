package com.fisk.common.service.flinkupload;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author JianWenYang
 */
@Component
@Slf4j
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
            log.error("远程连接Flink失败:" + e);
            return null;
        }
    }

    public static String ExecCommand(Session session, String command) {
        try {
            String jobId = null;
            // 打开通道，设置通道类型，和执行的命令
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            channelExec.setErrStream(System.err);
            channelExec.connect();
            InputStreamReader reader = new InputStreamReader(channelExec.getInputStream());
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                log.info("SSH创建任务返回信息:" + line);
                line = line.replaceAll(" ", "");
                if (line.indexOf("JobID") > -1) {
                    jobId = line.split(":")[1];
                }
            }
            channelExec.disconnect();
            return jobId;
        } catch (Exception e) {
            log.error("SSH方式,创建Flink任务失败:" + e);
            return e.toString();
        }
    }

}
