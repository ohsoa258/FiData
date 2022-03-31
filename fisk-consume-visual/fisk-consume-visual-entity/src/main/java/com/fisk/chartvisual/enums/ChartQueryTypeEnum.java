package com.fisk.chartvisual.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * 查询类型
 *
 * @author gy
 */
public enum ChartQueryTypeEnum implements BaseEnum {

    /**
     * 查询类型
     */
    RELEASE(0,"发布"),

    DRAFT(1,"草稿");


    ChartQueryTypeEnum(int value, String name) {
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
