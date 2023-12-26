package com.fisk.common.service.accessAndModel;

import com.fisk.common.core.enums.BaseEnum;

public enum ServerTypeEnum implements BaseEnum {
    ACCESS(1, "数据接入"),
    MODEL(2, "数仓建模"),
    DISPATCH(3, "数据管道");

    ServerTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static ServerTypeEnum getNameByValue(int value) {
        switch (value) {
            /*
             * 表类别
             * */
            case 1:
                return ACCESS;
            case 2:
                return MODEL;
            case 3:
                return DISPATCH;
            default:
                return null;
        }
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

