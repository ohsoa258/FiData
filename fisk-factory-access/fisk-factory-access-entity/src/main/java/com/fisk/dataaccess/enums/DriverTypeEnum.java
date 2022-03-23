package com.fisk.dataaccess.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author Lock
 */
public enum DriverTypeEnum implements BaseEnum {
    /**
     * 查询类型
     */
    MYSQL(1, "com.mysql.jdbc.Driver"),
    SQLSERVER(2, "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
    ORACLE(3, "oracle.jdbc.driver.OracleDriver"),
    PGSQL(4,"org.postgresql.Driver");

    DriverTypeEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    private final int value;
    private final String name;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}
