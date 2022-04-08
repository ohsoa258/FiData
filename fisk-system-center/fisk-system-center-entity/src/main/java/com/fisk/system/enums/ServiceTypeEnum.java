package com.fisk.system.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum ServiceTypeEnum implements BaseEnum {

    /**
     * 父级
     */
    PARENT_LEVEL(1,"父级");

    ServiceTypeEnum(int value, String name) {
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
