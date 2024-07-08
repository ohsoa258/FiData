package com.fisk.datamanagement.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2024-01-30
 * @Description:
 */
public enum ValueRangeTypeEnum implements BaseEnum {
    NONE(0,"无"),
    DATASET(1,"代码集"),
    VALUE(2,"数值"),
    VALUE_RANGE(3,"数值范围"),
    ;
    ValueRangeTypeEnum(int value, String name) {
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

    public static ValueRangeTypeEnum getEnum(int code) {
        for (ValueRangeTypeEnum enums : ValueRangeTypeEnum.values()) {
            if (enums.getValue() == code) {
                return enums;
            }
        }
        return null;
    }
}
