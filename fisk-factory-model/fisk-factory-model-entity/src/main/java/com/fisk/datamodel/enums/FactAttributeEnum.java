package com.fisk.datamodel.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum FactAttributeEnum implements BaseEnum {

    /**
     * 退化维度
     */
    DEGENERATION_DIMENSION(0,"退化维度"),
    /**
     * 维度键
     */
    DIMENSION_KEY(1,"维度键"),
    /**
     * 度量
     */
    MEASURE(2,"度量");

    FactAttributeEnum(int value, String name) {
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
