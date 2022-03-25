package com.fisk.datamanagement.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum DataTypeEnum implements BaseEnum {

    /**
     * 数据接入
     */
    DATA_INPUT(1,"数据接入"),
    DATA_MODEL(2,"数据建模"),
    DATA_DORIS(3,"doris");

    DataTypeEnum(int value, String name) {
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
