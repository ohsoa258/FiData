package com.fisk.dataaccess.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author cfk
 */
public enum DeltaTimeParameterTypeEnum implements BaseEnum {
    /*
     *增量时间参数类型
     */
    CONSTANT(1,"固定值"),
    VARIABLE(2,"脚本"),
    THE_DEFAULT_EMPTY(3,"默认值");

    DeltaTimeParameterTypeEnum(int value, String name) {
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
