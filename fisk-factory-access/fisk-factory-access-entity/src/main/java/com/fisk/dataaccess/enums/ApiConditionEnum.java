package com.fisk.dataaccess.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum ApiConditionEnum implements BaseEnum {

    /**
     * token
     */
    TOKEN(1, "TOKEN"),
    PAGENUM(2, "PAGENUM"),

    MAX(3, "MAX"),
    MIN(4, "MIN"),
    SUM(5, "SUM"),

    CURRENT_DATE(6, "CURRENT_DATE"),
    CURRENT_TIMESTAMP(7, "CURRENT_TIMESTAMP");

    private final int value;
    private final String name;

    ApiConditionEnum(int value, String name) {
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
