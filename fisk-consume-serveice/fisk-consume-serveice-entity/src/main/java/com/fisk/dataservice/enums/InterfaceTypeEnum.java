package com.fisk.dataservice.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2023-09-11
 * @Description:
 */
public enum InterfaceTypeEnum implements BaseEnum {
    /**
     * 1:Rest API 2:Web Service
     */
    NONE(0,"NONE"),
    REST_API(1,"Rest API "),
    WEB_SERVICE(2,"Web Service");
    InterfaceTypeEnum(int value, String name) {
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
