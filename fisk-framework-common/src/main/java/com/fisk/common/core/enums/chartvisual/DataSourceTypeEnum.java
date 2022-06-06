package com.fisk.common.core.enums.chartvisual;

import com.fisk.common.core.enums.BaseEnum;

/**
 * 数据源类型
 *
 * @author gy
 */

public enum DataSourceTypeEnum implements BaseEnum {

    /**
     * 支持的所有数据源类型
     */

    MYSQL(0, "MYSQL", "com.mysql.jdbc.Driver"),

    SQLSERVER(1, "SQLSERVER", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),

    CUBE(2,"CUBE","org.olap4j.driver.xmla.XmlaOlap4jDriver"),

    TABULAR(3,"TABULAR","org.olap4j.driver.xmla.XmlaOlap4jDriver"),

    SQLSERVER_WINDOWS(4, "SQLSERVER_WINDOWS", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),

    PG(5, "PG", "org.postgresql.Driver");

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
