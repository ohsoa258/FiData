package com.fisk.datamodel.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum  CreateTypeEnum implements BaseEnum {

    /**
     * Doris创建维度表
     */
    CREATE_DIMENSION(0,"创建维度"),

    /**
     * Doris创建事实表
     */
    CREATE_FACT(1,"创建事实"),
    /**
     * Doris创建指标表
     */
    CREATE_DORIS(2,"创建指标表");

    CreateTypeEnum(int value, String name) {
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
