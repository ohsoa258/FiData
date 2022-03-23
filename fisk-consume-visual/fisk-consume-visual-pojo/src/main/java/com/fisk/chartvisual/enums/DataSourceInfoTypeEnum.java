package com.fisk.chartvisual.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * 查询类型
 *
 * @author gy
 */
public enum DataSourceInfoTypeEnum implements BaseEnum {

    /**
     * 数据库类型
     */
    DATABASE_NAME(0,"库名"),

    TABLE_NAME(1,"表名");


    DataSourceInfoTypeEnum(int value, String name) {
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
