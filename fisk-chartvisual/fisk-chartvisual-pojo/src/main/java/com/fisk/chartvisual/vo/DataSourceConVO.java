package com.fisk.chartvisual.vo;

import lombok.Data;

/**
 * 数据源连接视图
 * @author gy
 */
@Data
public class DataSourceConVO {
    public int id;
    public String conStr;
    public String conType;
    public String conAccount;
    public String conPassword;
    public String conCreateTime;
}
