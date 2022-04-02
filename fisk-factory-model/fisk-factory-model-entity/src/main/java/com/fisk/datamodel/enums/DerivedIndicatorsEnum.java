package com.fisk.datamodel.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum DerivedIndicatorsEnum implements BaseEnum {
    /**
     * 基于原子指标
     */
    BASED_ATOMIC(0,"基于原子指标"),
    /**
     * 基于指标公式
     */
    BASED_FORMULA(1,"基于指标公式");

    DerivedIndicatorsEnum(int value, String name) {
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
