package com.fisk.system.vo.emailserver;

import com.fisk.system.enums.EmailServerTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version 1.0
 * @description 邮件服务器配置VO
 * @date 2022/3/22 15:37
 */
@Data
public class EmailServerVO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

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
     * 邮件服务器类型，仅查询
     */
    @ApiModelProperty(value = "邮件服务器类型，仅查询")
    public int emailServerTypeValue;

    /**
     * 是否启用SSL加密连接
     */
    @ApiModelProperty(value = "是否启用SSL加密连接")
    public Integer enableSsl;

    /*
     * 服务配置类型：1、邮箱 2、企业微信
     */
    @ApiModelProperty(value = "服务配置类型：1、邮箱 2、企业微信")
    public Integer serverConfigType;

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
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    public String createUser;
}
