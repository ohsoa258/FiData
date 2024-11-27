package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 规则状态
 * @date 2024/11/25 15:32
 */
public enum DataSetTypeEnum implements BaseEnum {

    ROW_COMPARE(1,"行对比"),
    VALUE_COMPARE(2,"值对比");

    DataSetTypeEnum(int value, String name) {
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
