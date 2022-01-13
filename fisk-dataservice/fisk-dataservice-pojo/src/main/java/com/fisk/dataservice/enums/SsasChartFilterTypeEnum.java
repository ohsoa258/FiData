package com.fisk.dataservice.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author dick
 * @version v1.0
 * @description Ssas筛选类型枚举
 * @date 2022/1/6 14:51
 */
public enum SsasChartFilterTypeEnum implements BaseEnum {
    FILTER(1,"筛选器"),
    SLICE(2,"切片"),
    DRILL(3,"下钻"),
    SLICER(4,"切片器-时间区间"),
    APPOINT_SLICER(5,"切片器-指定时间");
    SsasChartFilterTypeEnum(int value, String name) {
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
