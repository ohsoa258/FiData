package com.fisk.datamodel.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author SongJianJian
 */
public enum PrefixTempNameEnum implements BaseEnum {
    DIMENSION_TEMP_NAME(0,"temp");

    PrefixTempNameEnum(int value, String name) {
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
