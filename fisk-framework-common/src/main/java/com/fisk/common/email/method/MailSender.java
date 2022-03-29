package com.fisk.common.email.method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * @author dick
 * @version 1.0
 * @description 发邮件
 * @date 2022/3/29 17:05
 */
public class MailSender {
    @Autowired
    private JavaMailSender javaMailSender;

//    javaMailSender.get
}
