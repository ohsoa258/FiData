package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

public enum ValidityEnum implements BaseEnum {
    /**
     * 发布状态
     */
    Invalid(0,"失效"),
    Validity(1,"有效"),
    All(2,"全部");

    @EnumValue
    private final int value;
    private final String name;


    ValidityEnum(int value, String name) {
        this.value=value;
        this.name=name;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}
