package com.fisk.chartvisual.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * 是否存在
 *
 * @author wangyan
 */
public enum isExistTypeEnum implements BaseEnum {

    /**
     * 数据库改数据是否存在
     */
    NOT_Exist(0,"不存在"),

    EXIST(1,"已存在");


    isExistTypeEnum(int value, String name) {
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
