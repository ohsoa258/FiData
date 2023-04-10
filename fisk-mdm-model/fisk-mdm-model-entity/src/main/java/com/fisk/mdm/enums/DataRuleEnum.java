package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

public enum DataRuleEnum implements BaseEnum {

    Round(0,"四舍五入"),
    Split(1,"截取"),
    Default(2,"默认");

    @EnumValue
    private final int value;
    private final String name;


    DataRuleEnum(int value, String name) {
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
