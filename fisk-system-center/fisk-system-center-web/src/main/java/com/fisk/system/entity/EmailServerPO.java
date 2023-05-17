package com.fisk.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 邮件服务器配置表
 * @date 2022/3/22 15:20
 */
@Data
@TableName("tb_emailserver_config")
public class EmailServerPO extends BasePO {
    /**
     * 名称
     */
    @TableField("`name`")
    public String name;

    /**
     * 邮件服务器
     */
    public String emailServer;

    /**
     * 邮件服务器端口
     */
    public int emailServerPort;

    /**
     * 发件账号
     */
    public String emailServerAccount;

    /**
     * 发件密码
     */
    public String emailServerPwd;

    /**
     * 邮件服务器类型
     */
    public int emailServerType;

    /**
     * 是否启用SSL加密连接
     */
    public Integer enableSsl;

    /*
     * 服务配置类型：1、邮箱 2、企业微信
     */
    @TableField("`server_config_type`")
    public int serverConfigType;

    /*
     * 企业微信应用标识
     */
    @TableField("`wechat_agent_id`")
    public String wechatAgentId;

    /*
     * 企业微信应用服务地址
     */
    @TableField("`wechat_app_servers_address`")
    public String wechatAppServersAddress;

    /*
     * 企业微信公司标识
     */
    @TableField("`wechat_corp_id`")
    public String wechatCorpId;

    /*
     * 企业微信应用密钥
     */
    @TableField("`wechat_app_secret`")
    public String wechatAppSecret;
}
