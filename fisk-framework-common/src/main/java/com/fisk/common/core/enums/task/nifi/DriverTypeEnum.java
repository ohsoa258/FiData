package com.fisk.common.core.enums.task.nifi;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author gy
 */
public enum DriverTypeEnum implements BaseEnum {

    /**
     * 控制器服务类型
     */
    MYSQL(0, "com.mysql.jdbc.Driver"),
    SQLSERVER(1, "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
    POSTGRESQL(2,"org.postgresql.Driver"),
    ORACLE(3,"oracle.jdbc.driver.OracleDriver");

    DriverTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    private final String name;
    private final int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}
