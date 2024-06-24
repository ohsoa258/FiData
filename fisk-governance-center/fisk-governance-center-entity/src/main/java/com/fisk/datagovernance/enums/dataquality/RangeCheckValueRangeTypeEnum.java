package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 值域检查-取值范围类型
 * @date 2024/6/21 15:07
 */
public enum RangeCheckValueRangeTypeEnum implements BaseEnum {
    NONE(0, "空"),
    UNIDIRECTIONAL_VALUE(1, "单向取值"),
    INTERVAL_VALUE(2, "区间取值");

    RangeCheckValueRangeTypeEnum(int value, String name) {
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


    public static RangeCheckValueRangeTypeEnum getEnum(int value) {
        for (RangeCheckValueRangeTypeEnum e : RangeCheckValueRangeTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return RangeCheckValueRangeTypeEnum.NONE;
    }

    public static RangeCheckValueRangeTypeEnum getEnumByName(String name) {
        for (RangeCheckValueRangeTypeEnum e : RangeCheckValueRangeTypeEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return RangeCheckValueRangeTypeEnum.NONE;
    }
}
