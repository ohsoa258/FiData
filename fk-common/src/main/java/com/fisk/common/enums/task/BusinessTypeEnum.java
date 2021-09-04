package com.fisk.common.enums.task;

import com.fisk.common.enums.BaseEnum;
/**
 * cfk
 */
public enum BusinessTypeEnum implements BaseEnum {
    /**
     * datainput
     */
    DATAINPUT(0, "datainput"),
    /**
     * datamodel
     */
    DATAMODEL(1, "datamodel");

    BusinessTypeEnum(int value, String name) {
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
