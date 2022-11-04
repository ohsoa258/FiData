package com.fisk.common.core.enums.factory;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum BusinessTimeEnum implements BaseEnum {
    /**
     * 每年
     */
    YEAR(1, "每年"),
    MONTH(2, "每月"),
    DAY(3, "每天"),
    OTHER(4, "其他");

    private final String name;
    private final int value;

    BusinessTimeEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    public static BusinessTimeEnum getValue(String value) {
        BusinessTimeEnum[] timeEnums = values();
        for (BusinessTimeEnum typeEnum : timeEnums) {
            String queryValue = typeEnum.getName();
            if (queryValue.equals(value)) {
                return typeEnum;
            }
        }
        return BusinessTimeEnum.OTHER;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

}
