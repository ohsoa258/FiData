package com.fisk.common.core.enums.chartvisual;

import com.fisk.common.core.enums.BaseEnum;


/**
 * 字段类型（name / value）
 *
 * @author gy
 */

public enum ColumnTypeEnum implements BaseEnum {

    /**
     * 查询字段类型
     */
    NAME(0, "NAME"),
    VALUE(1, "VALUE");

    ColumnTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    private final String name;
    private final int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}
