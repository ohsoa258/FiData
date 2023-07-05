package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 波动检查类型
 * @date 2022/5/31 16:33
 */
public enum FluctuateCheckTypeEnum implements BaseEnum {

    NONE(0, "空"),
    SUM(1, "SUM"),
    COUNT(2, "COUNT"),
    AVG(3, "AVG"),
    MAX(4, "MAX"),
    MIN(5, "MIN");

    FluctuateCheckTypeEnum(int value, String name) {
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


    public static FluctuateCheckTypeEnum getEnum(int value) {
        for (FluctuateCheckTypeEnum e : FluctuateCheckTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return FluctuateCheckTypeEnum.NONE;
    }

    public static FluctuateCheckTypeEnum getEnumByName(String name) {
        for (FluctuateCheckTypeEnum e : FluctuateCheckTypeEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return FluctuateCheckTypeEnum.NONE;
    }
}
