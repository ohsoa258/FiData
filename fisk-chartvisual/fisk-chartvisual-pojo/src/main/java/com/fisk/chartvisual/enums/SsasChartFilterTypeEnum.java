package com.fisk.chartvisual.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * Ssas筛选类型枚举
 * @author JinXingWang
 */

public enum SsasChartFilterTypeEnum implements BaseEnum {
    FILTER(1,"筛选器"),
    SLICE(2,"切片"),
    DRILL(3,"下钻");
    SsasChartFilterTypeEnum(int value, String name) {
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
