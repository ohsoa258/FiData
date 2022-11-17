package com.fisk.dataaccess.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author cfk
 */
public enum syncModeTypeEnum implements BaseEnum {
    /**
     * 同步类型
     */
    FULL_VOLUME(1, "full_volume"),
    ADD(2, "add"),
    INCREMENT_MERGE(3, "increment_merge"),
    TIME_INCREMENT(4, "time_increment"),
    INCREMENT_DELINSERT(5, "increment_del_insert"),
    /**
     * ods表什么操作都不做
     */
    FULL_VOLUME_VERSION(6, "full_volume_version");
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
                return INCREMENT_MERGE.getName();
            case 4:
                return TIME_INCREMENT.getName();
            case 5:
                return INCREMENT_DELINSERT.getName();
            default:
                return null;
        }
    }
}
