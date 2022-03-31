package com.fisk.common.core.enums.chartvisual;

import com.fisk.common.core.enums.BaseEnum;

public enum TableOrderEnum implements BaseEnum {
    /**
     * 图表交互类型（下钻，联动）
     */
    ASC(0, "asc"),
    DESC(1, "desc");

    TableOrderEnum(int value, String name) {
        this.name = name;
        this.value = value;
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
