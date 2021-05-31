package com.fisk.common.enums.chartvisual;

import com.fisk.common.enums.BaseEnum;

public enum AggregationTypeEnum implements BaseEnum {

    /**
     * 查询字段类型
     */
    COUNT(0, "count"),
    SUM(1, "sum"),
    MAX(2, "max"),
    MIN(3, "min"),
    AVG(4, "avg");

    AggregationTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
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
