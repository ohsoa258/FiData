package com.fisk.chartvisual.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author WangYan
 * @date 2021/11/29 15:33
 * 指标类型
 */
public enum IndicatorTypeEnum implements BaseEnum {

    /**
     * 指标类型
     */
    ATOMIC_INDICATORS(0,"原子指标"),

    DERIVED_INDICATORS(1,"派生指标");

    IndicatorTypeEnum(int value, String name) {
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
