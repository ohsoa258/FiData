package com.fisk.dataservice.enums;

import com.fisk.common.enums.BaseEnum;

/**
 *  数据域配置字段类型
 *
 * @author wagyan
 */
public enum DataDoFieldTypeEnum implements BaseEnum {

    /**
     * 字段类型
     */
    COLUMN(0,"列"),

    VALUE(1,"值"),

    WHERE(2,"筛选器"),

    SLICER(3,"切片器-时间区间"),

    APPOINT_SLICER(4,"切片器-指定时间");

    DataDoFieldTypeEnum(int value, String name) {
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
