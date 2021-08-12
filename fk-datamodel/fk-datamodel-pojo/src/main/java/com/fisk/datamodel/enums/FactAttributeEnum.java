package com.fisk.datamodel.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum FactAttributeEnum implements BaseEnum {

    /**
     * 事实属性
     */
    FACTS_PROPERTIES(0,"事实属性"),
    /**
     * 关联维度
     */
    ASSOCIATED_DIMENSION(1,"关联维度"),
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
