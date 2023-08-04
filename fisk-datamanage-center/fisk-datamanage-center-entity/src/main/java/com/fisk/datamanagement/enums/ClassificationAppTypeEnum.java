package com.fisk.datamanagement.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JinXingWang
 */

public enum ClassificationAppTypeEnum implements BaseEnum {
    REAL_TIME(0,"实时应用","实时应用"),
    NON_REAL_TIME(1,"非实时应用","非实时应用");

    private final int value;
    private final String name;
    private final String description;

    ClassificationAppTypeEnum(int value, String name, String description) {
        this.value = value;
        this.name = name;
        this.description = description;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static ClassificationAppTypeEnum getEnumByName(String name) {
        ClassificationAppTypeEnum[] carTypeEnums = values();
        for (ClassificationAppTypeEnum carTypeEnum : carTypeEnums) {
            String queryName = carTypeEnum.name;
            if (queryName.equals(name)) {
                return carTypeEnum;
            }
        }
        return null;
    }

    public static ClassificationAppTypeEnum getEnumByValue(int value) {
        ClassificationAppTypeEnum[] carTypeEnums = values();
        for (ClassificationAppTypeEnum carTypeEnum : carTypeEnums) {
            if (carTypeEnum.value == value) {
                return carTypeEnum;
            }
        }
        return null;
    }
}
