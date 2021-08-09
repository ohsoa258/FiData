package com.fisk.common.filter.dto;

import com.fisk.common.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum FilterEnum implements BaseEnum {

    GREATER_THAN(0,"大于"),
    LESS_THAN(1,"小于"),
    EQUAL(2,"等于"),
    CONTAINS(3,"包含");

    FilterEnum(int value, String name) {
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
