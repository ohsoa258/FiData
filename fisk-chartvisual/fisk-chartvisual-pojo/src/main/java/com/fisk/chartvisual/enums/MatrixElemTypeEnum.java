package com.fisk.chartvisual.enums;

import com.fisk.common.enums.BaseEnum;


/**
 *
 * @author JinXingWang
 */

public enum MatrixElemTypeEnum implements BaseEnum {
    ROW(1,"行"),
    COLUMN(2,"列"),
    VALUE(3,"值");
    MatrixElemTypeEnum(int value, String name) {
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
