package com.fisk.chartvisual.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author JinXingWang
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
