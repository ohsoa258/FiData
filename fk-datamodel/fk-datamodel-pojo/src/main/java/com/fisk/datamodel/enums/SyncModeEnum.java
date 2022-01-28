package com.fisk.datamodel.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum SyncModeEnum implements BaseEnum {

    FULL_AMOUNT(1,"追加"),

    INCREMENTAL(2,"全量覆盖"),

    TIME_COVER(3,"业务主键覆盖"),

    CUSTOM_OVERRIDE(4,"业务时间覆盖");

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
