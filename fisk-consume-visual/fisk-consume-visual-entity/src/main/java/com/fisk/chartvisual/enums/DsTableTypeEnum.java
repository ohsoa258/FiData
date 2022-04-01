package com.fisk.chartvisual.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @author WangYan
 * @date 2022/1/6 16:11
 */
public enum DsTableTypeEnum implements BaseEnum {

    /**
     * 目标字段类型
     */
    NUMERICAL(2001,"数值"),
    TEXT(2002,"文本"),
    DATATIME(2003,"时间");

    DsTableTypeEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    @EnumValue
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
