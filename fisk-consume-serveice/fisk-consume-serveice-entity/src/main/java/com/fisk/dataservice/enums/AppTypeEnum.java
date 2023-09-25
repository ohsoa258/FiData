package com.fisk.dataservice.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2023-09-08
 * @Description:
 */
public enum AppTypeEnum implements BaseEnum {
    /**
     * api类型
     */
    NONE(0,"NONE"),
    TABLE_TYPE(1,"table"),
    API_TYPE(2,"api");

    AppTypeEnum(int value, String name) {
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
