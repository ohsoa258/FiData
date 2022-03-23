package com.fisk.datamodel.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum TableHistoryTypeEnum implements BaseEnum {
    /**
     * 维度表
     */
    TABLE_DIMENSION(0,"维度表"),
    /**
     * 事实表
     */
    TABLE_FACT(1,"事实表");

    TableHistoryTypeEnum(int value, String name) {
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
