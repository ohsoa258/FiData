package com.fisk.chartvisual.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 数据源配置
 * @author gy
 */
@Data
@TableName("tb_datasource_con")
public class DataSourceConPO {

    public long id;

    public String conStr;

    public int conType;

    public String conAccount;

    public String conPassword;

    public Date createTime;

    public String createUser;

    public Date updateTime;

    public String updateUser;

    public int delFlag;

}
