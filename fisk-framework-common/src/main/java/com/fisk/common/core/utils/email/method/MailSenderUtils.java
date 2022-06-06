package com.fisk.common.core.utils.email.method;

import com.fisk.common.core.utils.email.dto.MailSenderDTO;
import com.fisk.common.core.utils.email.dto.MailServeiceDTO;
import com.sun.mail.util.MailSSLSocketFactory;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.Date;
import java.util.Properties;

/**
 * @author dick
 * @version 1.0
 * @description 发邮件
 * @date 2022/3/29 17:05
 */
public class MailSenderUtils {

    public static void send(MailServeiceDTO serveiceDTO, MailSenderDTO senderDTO) throws Exception {
        if (senderDTO == null || senderDTO == null) {
            return;
        }
        Properties props = new Properties();
        if (serveiceDTO.isOpenAuth()) {
            // 发送服务器需要身份验证
            props.setProperty("mail.smtp.auth", "true");
        }
        // 设置邮件服务器主机名
        props.setProperty("mail.host", serveiceDTO.getHost());
        // 发送邮件协议名称
        props.setProperty("mail.transport.protocol", serveiceDTO.getProtocol());
        if (serveiceDTO.getPort() == 465 || serveiceDTO.getPort() == 587) {
            // PS: 某些邮箱服务器要求 SMTP 连接需要使用 SSL 安全认证 (为了提高安全性, 邮箱支持SSL连接, 也可以自己开启),
            // 如果无法连接邮件服务器, 仔细查看控制台打印的 log, 如果有有类似 “连接失败, 要求 SSL 安全连接” 等错误,
            // 打开下面 注释代码, 开启 SSL 安全连接。

            // SMTP 服务器的端口 (非 SSL 连接的端口一般默认为 25, 可以不添加, 如果开启了 SSL 连接,
            // 需要改为对应邮箱的 SMTP 服务器的端口, 具体可查看对应邮箱服务的帮助, QQ邮箱的SMTP(SLL)端口为465或587,
            // 其他邮箱自行去查看)
            final String smtpPort = String.valueOf(serveiceDTO.getPort());
            props.setProperty("mail.smtp.port", smtpPort);
            props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.smtp.socketFactory.fallback", "false");
            props.setProperty("mail.smtp.socketFactory.port", smtpPort);

            // 配置ssl加密工厂
            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.socketFactory", sf);
        }
        Session session = Session.getInstance(props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        //匿名只能访问函数内容的final类型的变量，可以访问外部类的成员变量
                        return new PasswordAuthentication(serveiceDTO.getUser(), serveiceDTO.getPassword());
                    }

                }
        );
        session.setDebug(serveiceDTO.openDebug);
        //构建邮件详情
        Message mimeMessage = createMimeMessage(session, senderDTO);
        //建立发送邮件的对象
        session.getTransport();
        Transport.send(mimeMessage);
    }

    private static Message createMimeMessage(Session session, MailSenderDTO senderDTO) throws Exception {
        // 1. 创建邮件对象
        Message mimeMessage = new MimeMessage(session);
        // 2. 发件人
        mimeMessage.setFrom(new InternetAddress(senderDTO.getUser()));
        // 3. 收件人
        mimeMessage.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(senderDTO.getToAddress()));
        // 4. 抄送人
        if (senderDTO.getToCc() != null && senderDTO.getToCc() != "") {
            mimeMessage.setRecipients(MimeMessage.RecipientType.CC, InternetAddress.parse(senderDTO.getToCc()));
        }
        // 5. 密抄
        if (senderDTO.getToBcc() != null && senderDTO.getToBcc() != "") {
            mimeMessage.setRecipients(MimeMessage.RecipientType.BCC, InternetAddress.parse(senderDTO.getToBcc()));
        }
        // 6. 邮件主题
        mimeMessage.setSubject(senderDTO.getSubject());
        // 7. 设置邮件正文
        mimeMessage.setText(senderDTO.getBody());

        if (senderDTO.sendAttachment
                && senderDTO.getAttachmentName() != null && !senderDTO.getAttachmentName().isEmpty()
                && senderDTO.getAttachmentPath() != null && !senderDTO.getAttachmentPath().isEmpty()) {
            File tmpFile = new File(senderDTO.getAttachmentPath());
            if (tmpFile.exists()) {
                MimeMultipart multipart = new MimeMultipart();
                MimeBodyPart file1 = new MimeBodyPart();
                DataHandler handler = new DataHandler(new FileDataSource(tmpFile.getPath()));
                file1.setDataHandler(handler);
                //对文件名进行编码，防止出现乱码
                String fileName = MimeUtility.encodeWord(tmpFile.getName(), "utf-8", "B");
                file1.setFileName(fileName);
                multipart.addBodyPart(file1);
                mimeMessage.setContent(multipart);
            }
        }

        // 8. 设置发件时间
        mimeMessage.setSentDate(new Date());
        // 9. 保存上面的所有设置
        mimeMessage.saveChanges();
        return mimeMessage;
    }
}


