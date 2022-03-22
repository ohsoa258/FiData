package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 表血缘校验范围
 * @date 2022/3/22 14:01
 */
public enum CheckConsanguinityTypeEnum implements BaseEnum {
    /**
     * 表血缘校验范围
     */
    UP_CHECK(1, "上游"),
    DOWN_CHECK(2, "下游"),
    UP_AND_DOWN(3, "上下游");

    CheckConsanguinityTypeEnum(int value, String name) {
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
