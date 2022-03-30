package com.fisk.common.email.dto;

import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 邮件配置DTO
 * @date 2022/3/29 17:07
 */
@Data
public class MailServeiceDTO {
    /**
     * 开启debug调试
     */
    public boolean openDebug;

    /**
     * 开启身份认证
     */
    public boolean openAuth;

    /**
     * 邮件服务器主机名
     */
    public String host;

    /**
     * 邮件协议
     */
    public  String protocol;

    /**
     * 开启SSL
     */
    public boolean openSsl;

    /**
     * 发件账号
     */
    public String user;

    /**
     * 发件密码
     */
    public String password;
}
