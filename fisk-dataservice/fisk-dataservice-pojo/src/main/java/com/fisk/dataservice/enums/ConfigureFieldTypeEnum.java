package com.fisk.dataservice.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * 配置字段类型
 *
 * @author wagyan
 */
public enum ConfigureFieldTypeEnum implements BaseEnum {

    /**
     * 字段类型
     */
    GROUPING(0,"分组字段"),

    AGGREGATION(1,"聚合字段"),

    RESTRICT(2,"权限控制字段"),

    QUERY(3,"查询字段");

    ConfigureFieldTypeEnum(int value, String name) {
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
