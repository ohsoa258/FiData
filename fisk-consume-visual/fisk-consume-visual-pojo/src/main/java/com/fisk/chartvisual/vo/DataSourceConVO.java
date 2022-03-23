package com.fisk.chartvisual.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    public String conIp;
    public int conPort;
    public String conCube;
    public DataSourceTypeEnum conType;
    public String conDbname;
    public String conAccount;
    public String conPassword;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public String createTime;
}
