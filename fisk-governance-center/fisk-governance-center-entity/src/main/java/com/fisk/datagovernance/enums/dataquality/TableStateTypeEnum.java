package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 表状态
 * @date 2022/3/22 14:02
 */
public enum TableStateTypeEnum implements BaseEnum {
    /**
     * 表状态
     */
    NORMAL(1,"正常"),
    RECGCLED(0,"已回收");

    TableStateTypeEnum(int value, String name) {
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
