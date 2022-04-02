package com.fisk.common.core.utils.email.dto;

import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 邮件DTO
 * @date 2022/3/29 17:03
 */
@Data
public class MailSenderDTO {
    /**
     * 发件账号
     */
    public String user;

    /**
     * 邮件收件人
     */
    public String toAddress;

    /**
     * 邮件抄送人
     */
    public String toCc;

    /**
     * 密抄
     */
    public String toBcc;

    /**
     * 邮件标题
     */
    public String subject;

    /**
     * 邮件正文
     */
    public String body;
}
