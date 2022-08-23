package com.fisk.dataaccess.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum DataTargetTypeEnum implements BaseEnum {

    /**
     * oracle类型
     */
    ORACLE(1, "Oracle"),
    SQL_SERVER(2, "SqlServer"),
    MYSQL(3, "MySQL"),
    FTP(5, "FTP"),
    API(6, "api");

    private final int value;
    private final String name;

    DataTargetTypeEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

}
