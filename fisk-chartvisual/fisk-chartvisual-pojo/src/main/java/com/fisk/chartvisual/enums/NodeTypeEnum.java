package com.fisk.chartvisual.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * 维度类型
 * @author JinXingWang
 */
public enum NodeTypeEnum implements BaseEnum {
    MEASURE(0,"度量"),
    OTHER(1,"维度"),
    BUSINESS_DOMAIN(2,"业务域"),
    DIMENSION_FIELD(3,"维度字段"),
    BUSINESS_PROCESS(4,"业务过程"),
    FACT(5,"事实"),
    ATOMIC_METRICS(6,"原子指标"),
    DERIVED_METRICS(7,"派生指标");

    NodeTypeEnum(int value, String name) {
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
