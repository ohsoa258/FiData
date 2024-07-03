package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 值域检查类型
 * @date 2022/5/31 16:33
 */
public enum RangeCheckTypeEnum implements BaseEnum {

    NONE(0, "空"),
    SEQUENCE_RANGE(1, "序列范围"),
    VALUE_RANGE(2, "取值范围"),
    DATE_RANGE(3, "日期范围");

    RangeCheckTypeEnum(int value, String name) {
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

    public static RangeCheckTypeEnum getEnum(int value) {
        for (RangeCheckTypeEnum e : RangeCheckTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return RangeCheckTypeEnum.NONE;
    }

    public static RangeCheckTypeEnum getEnumByName(String name) {
        for (RangeCheckTypeEnum e : RangeCheckTypeEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return RangeCheckTypeEnum.NONE;
    }
}
