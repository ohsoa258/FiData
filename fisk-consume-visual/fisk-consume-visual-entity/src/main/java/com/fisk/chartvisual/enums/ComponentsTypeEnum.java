package com.fisk.chartvisual.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * 组件类型
 *
 * @author wangyan
 */
public enum ComponentsTypeEnum implements BaseEnum {


    MENU(0,"菜单"),

    COMPONENTS(1,"组件");


    ComponentsTypeEnum(int value, String name) {
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
