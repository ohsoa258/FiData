package com.fisk.dataservice.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2023-09-12
 * @Description:
 */
public enum JsonTypeEnum implements BaseEnum {

    /**
     * api类型
     */
    NONE(0,"NONE"),
    ARRAY(1, "数组"),
    OBJECT(2, "对象"),
    NUMBER(3, "数值对象"),
    STRING(4, "值对象");

    private final int value;
    private final String name;

    JsonTypeEnum(int value, String name) {
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