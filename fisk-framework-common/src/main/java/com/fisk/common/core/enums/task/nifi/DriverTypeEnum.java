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
    ORACLE(3,"oracle.jdbc.driver.OracleDriver"),
    OPENEDGE(4,"com.ddtek.jdbc.openedge.OpenEdgeDriver"),
    DORIS(5, "com.mysql.jdbc.Driver"),
    MONGODB(6, ""),
    PI(7, "com.osisoft.jdbc.Driver"),
    ;
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

    public static DriverTypeEnum getValue(String value) {
        DriverTypeEnum[] typeEnums = values();
        for (DriverTypeEnum carTypeEnum : typeEnums) {
            String queryValue = carTypeEnum.getName();
            if (queryValue.equals(value)) {
                return carTypeEnum;
            }
        }
        return DriverTypeEnum.SQLSERVER;
    }

}
