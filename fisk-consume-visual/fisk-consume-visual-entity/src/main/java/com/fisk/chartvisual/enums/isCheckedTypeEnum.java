package com.fisk.chartvisual.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * 是否选中
 *
 * @author gy
 */
public enum isCheckedTypeEnum implements BaseEnum {

    /**
     * 数据库类型
     */
    NOT_CHECKED(0,"未选中"),

    CHECKED(1,"已选中");


    isCheckedTypeEnum(int value, String name) {
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
