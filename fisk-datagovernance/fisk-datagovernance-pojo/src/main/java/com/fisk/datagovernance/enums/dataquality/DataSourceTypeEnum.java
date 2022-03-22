package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.enums.BaseEnum;

/**
 * @author dick
 * @version v1.0
 * @description 数据源类型枚举
 * @date 2022/1/6 14:51
 */
public enum DataSourceTypeEnum implements BaseEnum {

    /**
     * 数据源类型
     */
    MYSQL(0, "MYSQL", "com.mysql.jdbc.Driver"),

    SQLSERVER(1, "SQLSERVER", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),

    CUBE(2,"CUBE","org.olap4j.driver.xmla.XmlaOlap4jDriver"),

    TABULAR(3,"TABULAR","org.olap4j.driver.xmla.XmlaOlap4jDriver");

    DataSourceTypeEnum(int value, String name, String driverName) {
        this.driverName = driverName;
        this.name = name;
        this.value = value;
    }

    private final String driverName;
    private final String name;
    private final int value;

    public String getDriverName() {
        return driverName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getValue() {
        return value;
    }
}
