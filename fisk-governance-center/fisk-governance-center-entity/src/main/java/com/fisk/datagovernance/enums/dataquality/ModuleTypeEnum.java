package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 组件规则类型
 * @date 2022/3/22 14:01
 */
public enum ModuleTypeEnum implements BaseEnum {
    /**
     * 组件规则类型
     */
    STRONG_RULE(1, "强规则"),
    WEAK_RULE(3, "弱规则");

    ModuleTypeEnum(int value, String name) {
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
