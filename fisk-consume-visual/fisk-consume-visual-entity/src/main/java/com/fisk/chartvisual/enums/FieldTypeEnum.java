package com.fisk.chartvisual.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 *  数据域配置字段类型
 *
 * @author wagyan
 */
public enum FieldTypeEnum implements BaseEnum {

    /**
     * 字段类型
     */
    COLUMN(0,"列"),

    ROW(1,"行"),

    VALUE(2,"值");


    FieldTypeEnum(int value, String name) {
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
