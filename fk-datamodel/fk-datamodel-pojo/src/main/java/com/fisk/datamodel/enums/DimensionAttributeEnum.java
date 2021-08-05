package com.fisk.datamodel.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum DimensionAttributeEnum implements BaseEnum {

    BUSINESS_KEY(0,"业务主键"),

    ASSOCIATED_DIMENSION(1,"关联维度"),

    ATTRIBUTE(2,"属性");



    DimensionAttributeEnum(int value, String name) {
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
