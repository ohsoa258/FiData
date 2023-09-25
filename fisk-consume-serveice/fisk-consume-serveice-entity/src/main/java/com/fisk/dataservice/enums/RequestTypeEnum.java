package com.fisk.dataservice.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2023-09-13
 * @Description:
 */
public enum RequestTypeEnum implements BaseEnum {
    /**
     * api请求类型
     */
    NONE(0,"NONE"),
    GET(1,"GET"),
    POST(2,"POST");

    RequestTypeEnum(int value, String name) {
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
