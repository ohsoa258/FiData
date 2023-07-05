package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 波动检查运算符
 * @date 2022/5/31 16:33
 */
public enum FluctuateCheckOperatorEnum implements BaseEnum {

    NONE(0, "空"),
    GREATER_THAN(1, ">"),
    GREATER_THAN_OR_EQUAL(2, ">="),
    EQUAL(3, "="),
    LESS_THAN(4, "<"),
    LESS_THAN_OR_EQUAL(5, "<=");

    FluctuateCheckOperatorEnum(int value, String name) {
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


    public static FluctuateCheckOperatorEnum getEnum(int value) {
        for (FluctuateCheckOperatorEnum e : FluctuateCheckOperatorEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return FluctuateCheckOperatorEnum.NONE;
    }

    public static FluctuateCheckOperatorEnum getEnumByName(String name) {
        for (FluctuateCheckOperatorEnum e : FluctuateCheckOperatorEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return FluctuateCheckOperatorEnum.NONE;
    }
}
