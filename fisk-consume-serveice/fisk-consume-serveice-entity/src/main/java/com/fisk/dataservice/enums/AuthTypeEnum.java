package com.fisk.dataservice.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2023-09-11
 * @Description:
 */
public enum AuthTypeEnum implements BaseEnum {
    /**
     * api类型
     */
    NONE(0,"NONE"),
    HEADER(1,"header"),
    PARAMS(2,"params");

    AuthTypeEnum(int value, String name) {
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
