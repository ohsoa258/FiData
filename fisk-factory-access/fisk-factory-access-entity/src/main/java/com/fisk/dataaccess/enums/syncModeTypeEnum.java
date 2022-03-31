package com.fisk.dataaccess.enums;

import com.fisk.common.core.enums.BaseEnum;

public enum syncModeTypeEnum implements BaseEnum {
    /**
     * 同步类型
     */
    FULL_VOLUME(1, "full_volume"),
    ADD(2, "add"),
    INCREMENT(3, "increment"),
    TIME_INCREMENT(4, "time_increment");

    syncModeTypeEnum(int value, String name) {
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

    public static String getNameByValue(int value) {
        switch (value) {
            case 1:
                return FULL_VOLUME.getName();
            case 2:
                return ADD.getName();
            case 3:
                return INCREMENT.getName();
            case 4:
                return TIME_INCREMENT.getName();
            default:
                return null;
        }
    }
}
