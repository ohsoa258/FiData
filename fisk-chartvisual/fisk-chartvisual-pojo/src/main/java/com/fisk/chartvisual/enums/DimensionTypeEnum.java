package com.fisk.chartvisual.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * 维度类型
 * @author JinXingWang
 */

public enum DimensionTypeEnum implements BaseEnum {
    MEASURE(0,"度量"),
    OTHER(1,"维度");

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
