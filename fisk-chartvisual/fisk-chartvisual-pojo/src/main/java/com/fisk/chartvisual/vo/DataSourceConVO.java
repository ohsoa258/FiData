package com.fisk.chartvisual.vo;

import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import lombok.Data;

/**
 * 数据源连接视图
 * @author gy
 */
@Data
public class DataSourceConVO {
    public int id;
    public String name;
    public String conStr;
    public DataSourceTypeEnum conType;
    public String conDbname;
    public String conAccount;
    public String conPassword;
    public String createTime;
}
