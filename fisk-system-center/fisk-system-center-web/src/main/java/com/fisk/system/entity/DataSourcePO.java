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
}
