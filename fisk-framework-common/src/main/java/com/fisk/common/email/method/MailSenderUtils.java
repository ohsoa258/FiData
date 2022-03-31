package com.fisk.common.email.method;

import com.fisk.common.email.dto.MailSenderDTO;
import com.fisk.common.email.dto.MailServeiceDTO;
import com.sun.mail.util.MailSSLSocketFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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
        if (serveiceDTO.isOpenSsl()) {
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
        // 8. 设置发件时间
        mimeMessage.setSentDate(new Date());
        // 9. 保存上面的所有设置
        mimeMessage.saveChanges();
        return mimeMessage;
    }
}


