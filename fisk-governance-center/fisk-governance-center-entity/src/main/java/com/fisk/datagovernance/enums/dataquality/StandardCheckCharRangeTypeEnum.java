package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 规范检查-字符范围类型
 * @date 2024/6/21 14:58
 */
public enum StandardCheckCharRangeTypeEnum implements BaseEnum {
    NONE(0, "空"),
    CHARACTER_PRECISION_RANGE(1, "字符精度范围"),
    CHARACTER_LENGTH_RANGE(2, "字符长度范围");

    StandardCheckCharRangeTypeEnum(int value, String name) {
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


    public static StandardCheckCharRangeTypeEnum getEnum(int value) {
        for (StandardCheckCharRangeTypeEnum e : StandardCheckCharRangeTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return StandardCheckCharRangeTypeEnum.NONE;
    }

    public static StandardCheckCharRangeTypeEnum getEnumByName(String name) {
        for (StandardCheckCharRangeTypeEnum e : StandardCheckCharRangeTypeEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return StandardCheckCharRangeTypeEnum.NONE;
    }
}
