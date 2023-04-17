package com.fisk.dataaccess.enums;

import com.fisk.common.core.enums.BaseEnum;

public enum AppDriveTypeEnum implements BaseEnum {

    /**
     * 驱动类型
     */
    MYSQL(1, "MYSQL"),
    SQLSERVER(2, "SQLSERVER"),
    ORACLE(3, "ORACLE"),
    POSTGRESQL(4,"POSTGRESQL"),
    RESTFULAPI(5,"RESTFULAPI"),
    API(6,"API"),
    FTP(7,"FTP"),
    SFTP(8,"SFTP");

    AppDriveTypeEnum(int value, String name) {
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
