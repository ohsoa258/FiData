package com.fisk.common.core.enums.chartvisual;

import com.fisk.common.core.constants.SystemConstants;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @author Lock
 */
public enum AggregationTypeEnum implements BaseEnum {
    /**
     * 查询字段类型
     */
    COUNT(0, "count(" + SystemConstants.BUILD_SQL_REPLACE_STR + ")"),
    SUM(1, "sum(" + SystemConstants.BUILD_SQL_REPLACE_STR + ")"),
    MAX(2, "max(" + SystemConstants.BUILD_SQL_REPLACE_STR + ")"),
    MIN(3, "min(" + SystemConstants.BUILD_SQL_REPLACE_STR + ")"),
    AVG(4, "avg(" + SystemConstants.BUILD_SQL_REPLACE_STR + ")"),
    COUNT_DISTINCT(5, "count(distinct " + SystemConstants.BUILD_SQL_REPLACE_STR + ")");

    AggregationTypeEnum(int value, String name) {
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
