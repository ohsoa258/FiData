package com.fisk.datamodel.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum IndicatorsTypeEnum implements BaseEnum {

    /**
     * 原子指标
     */
    ATOMIC_INDICATORS(0,"原子指标"),

    DERIVED_INDICATORS(1,"派生指标");

    IndicatorsTypeEnum(int value, String name) {
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
