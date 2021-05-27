package com.fisk.common.enums.chartvisual;

import com.fisk.common.enums.BaseEnum;

import java.util.Arrays;

/**
 * 数据源类型
 *
 * @author gy
 */

public enum DataSourceTypeEnum implements BaseEnum {

    /**
     * 支持的所有数据源类型
     */

    MYSQL(0, "MYSQL"),

    SQLSERVER(1, "SQLSERVER");


    DataSourceTypeEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    private final String name;
    private final int value;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getValue() {
        return value;
    }
}
