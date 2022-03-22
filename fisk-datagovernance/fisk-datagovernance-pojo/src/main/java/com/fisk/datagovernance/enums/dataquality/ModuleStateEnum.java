package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 组件状态
 * @date 2022/3/22 14:02
 */
public enum ModuleStateEnum implements BaseEnum {
    /**
     * 组件状态
     */
    Disable(0,"禁用"),
    Enable(1,"启用");

    ModuleStateEnum(int value, String name) {
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
