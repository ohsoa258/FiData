package com.fisk.common.core.enums.dataservice;

import com.fisk.common.core.enums.BaseEnum;

/**
 * 数据源类型
 *
 * @author
 */

public enum DataSourceTypeEnum implements BaseEnum {

    /**
     * 支持的所有数据源类型
     */
    MYSQL(0, "MYSQL", "com.mysql.jdbc.Driver"),

    SQLSERVER(1, "SQLSERVER", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),

    CUBE(2, "CUBE", "org.olap4j.driver.xmla.XmlaOlap4jDriver"),

    TABULAR(3, "TABULAR", "org.olap4j.driver.xmla.XmlaOlap4jDriver"),

    POSTGRESQL(4, "POSTGRESQL", "org.postgresql.Driver"),

    DORIS(5, "DORIS", "com.mysql.jdbc.Driver"),

    ORACLE(6, "ORACLE", "oracle.jdbc.driver.OracleDriver");

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

    public static DataSourceTypeEnum getEnum(int value) {
        for (DataSourceTypeEnum e : DataSourceTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return DataSourceTypeEnum.MYSQL;
    }

    public static DataSourceTypeEnum getEnum(String name) {
        for (DataSourceTypeEnum e : DataSourceTypeEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return DataSourceTypeEnum.MYSQL;
    }
}
