package com.fisk.system.dto.emailserver;

import com.fisk.system.enums.EmailServerTypeEnum;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version 1.0
 * @description 邮件服务器DTO
 * @date 2022/3/24 13:56
 */
public class EmailServerDTO {
    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    public String name;

    /**
     * 邮件服务器
     */
    @ApiModelProperty(value = "邮件服务器")
    public String emailServer;

    /**
     * 邮件服务器端口
     */
    @ApiModelProperty(value = "邮件服务器端口")
    public int emailServerPort;

    /**
     * 发件账号
     */
    @ApiModelProperty(value = "发件账号")
    public String emailServerAccount;

    /**
     * 发件密码
     */
    @ApiModelProperty(value = "发件密码")
    public String emailServerPwd;

    /**
     * 邮件服务器类型
     */
    @ApiModelProperty(value = "邮件服务器类型")
    public EmailServerTypeEnum emailServerType;

    /**
     * 是否启用SSL加密连接
     */
    @ApiModelProperty(value = "是否启用SSL加密连接")
    public Integer enableSsl;

    /*
     * 服务配置类型：1、邮箱 2、企业微信
     */
    @ApiModelProperty(value = "服务配置类型：1、邮箱 2、企业微信")
    public int serverConfigType;

    /*
     * 企业微信应用标识
     */
    @ApiModelProperty(value = "企业微信应用标识")
    public String wechatAgentId;

    /*
     * 企业微信应用服务地址
     */
    @ApiModelProperty(value = "企业微信应用服务地址")
    public String wechatAppServersAddress;

    /*
     * 企业微信公司标识
     */
    @ApiModelProperty(value = "企业微信公司标识")
    public String wechatCorpId;

    /*
     * 企业微信应用密钥
     */
    @ApiModelProperty(value = "企业微信应用密钥")
    public String wechatAppSecret;
}
