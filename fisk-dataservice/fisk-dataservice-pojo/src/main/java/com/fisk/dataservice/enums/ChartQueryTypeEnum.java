package com.fisk.dataservice.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author dick
 * @version v1.0
 * @description ChartQueryTypeEnum
 * @date 2022/1/6 14:51
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
