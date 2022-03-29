package com.fisk.common.email.dto;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 邮件DTO
 * @date 2022/3/29 17:03
 */
public class MailSenderDTO {
    // 收件人地址
    private String toAddress;

    // 邮件主题
    private String subject;

    // 邮件内容
    private String content;

    // 是否是html 注意字段boolean类型 不要用is开头 idea会生成get,set方法会进行优化 isHtml -> isHtml(),getHtml() -- html -> isHtml(),getHtml()
    private boolean html = false;

    // 图片地址集合
    private List<String> photoList;

    // 附件地址集合
    private List<String> attachList;
}
