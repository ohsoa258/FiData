package com.fisk.dataservice.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author dick
 * @version v1.0
 * @description 维度类型
 * @date 2022/1/6 14:51
 */

public enum DimensionTypeEnum implements BaseEnum {
    MEASURE(2,"度量"),
    OTHER(3,"维度");

    DimensionTypeEnum(int value, String name) {
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
