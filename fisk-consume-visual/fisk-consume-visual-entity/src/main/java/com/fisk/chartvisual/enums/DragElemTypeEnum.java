package com.fisk.chartvisual.enums;

import com.fisk.common.core.enums.BaseEnum;


/**
 *
 * @author JinXingWang
 */

public enum DragElemTypeEnum implements BaseEnum {

    /**
     * 字段类型
     */
    ROW(1,"行"),
    COLUMN(2,"列"),
    VALUE(3,"值"),
    FILTER(4,"筛选器");
    DragElemTypeEnum(int value, String name) {
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
