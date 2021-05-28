package com.fisk.common.enums.chartvisual;

import com.fisk.common.enums.BaseEnum;

import java.util.Arrays;

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

    SQLSERVER(1, "SQLSERVER", "com.microsoft.sqlserver.jdbc.SQLServerDriver");


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
