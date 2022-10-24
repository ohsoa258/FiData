package com.fisk.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 数据源实体类
 * @date 2022/6/13 14:51
 */
@Data
@TableName("tb_datasource_config")
public class DataSourcePO extends BasePO
{
    /**
     * 连接名称
     */
    @TableField("`name`")
    public String name;

    /**
     * 连接字符串
     */
    public String conStr;

    /**
     * ip
     */
    public String conIp;

    /**
     * 端口
     */
    public int conPort;

    /**
     * 数据库名称
     */
    public String conDbname;

    /**
     * 连接类型
     */
    public int conType;

    /**
     * 账号
     */
    public String conAccount;

    /**
     * 密码
     */
    public String conPassword;

    /**
     * 请求协议
     */
    public String protocol;

    /**
     * 平台
     */
    public String platform;

    /**
     * oracle服务类型：0服务名 1SID
     */
    public int serviceType;

    /**
     * oracle服务名
     */
    public String serviceName;

    /**
     * oracle域名
     */
    public String domainName;

    /**
     * 数据源类型：1系统数据源 2外部数据源
     */
    public int sourceType;

    /**
     * 数据源用途
     */
    public String purpose;

    /**
     * 负责人
     */
    public String principal;
}
