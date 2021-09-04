package com.fisk.datamodel.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum SyncModeEnum implements BaseEnum {

    FULL_AMOUNT(1,"全量"),

    INCREMENTAL(2,"增量"),

    TIME_COVER(3,"业务时间覆盖"),

    CUSTOM_OVERRIDE(4,"自定义覆盖");

    SyncModeEnum(int value, String name) {
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
