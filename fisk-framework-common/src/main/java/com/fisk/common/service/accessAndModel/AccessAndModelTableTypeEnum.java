package com.fisk.common.service.accessAndModel;

import com.fisk.common.core.enums.BaseEnum;

public enum AccessAndModelTableTypeEnum implements BaseEnum {
    DIMENSION(1, "维度表"),
    FACT(2, "事实表"),
    PHYSICS(3, "物理表");

    AccessAndModelTableTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static AccessAndModelTableTypeEnum getNameByValue(int value) {
        switch (value) {
            /*
             * 表类别
             * */
            case 1:
                return DIMENSION;
            case 2:
                return FACT;
            case 3:
                return PHYSICS;
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
