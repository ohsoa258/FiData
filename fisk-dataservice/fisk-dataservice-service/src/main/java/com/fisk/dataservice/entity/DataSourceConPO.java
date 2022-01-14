package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 数据源实体类
 * @date 2022/1/6 14:51
 */
@Data
@TableName("tb_datasource_config")
public class DataSourceConPO extends BasePO
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
    public Integer conPort;

    /**
     * 模型
     */
    public String conCube;

    /**
     * 数据库名称
     */
    public String conDbname;

    /**
     * 连接类型
     */
    public Integer conType;

    /**
     * 账号
     */
    public String conAccount;

    /**
     * 密码
     */
    public String conPassword;
}
