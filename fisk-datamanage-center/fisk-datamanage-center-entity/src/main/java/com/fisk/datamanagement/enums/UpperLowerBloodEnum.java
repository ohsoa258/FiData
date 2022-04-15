package com.fisk.datamanagement.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum UpperLowerBloodEnum implements BaseEnum {

    /**
     * 上级
     */
    UPPER(1,"上级"),
    LOWER(2,"下级"),
    UPPER_LOWER(3,"上下级");

    UpperLowerBloodEnum(int value, String name) {
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
