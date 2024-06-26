package com.fisk.dataaccess.enums.tablefield;

import com.fisk.common.core.enums.BaseEnum;

/**
 * 数据分类
 */
public enum DataClassificationEnum implements BaseEnum {


    /**
     * 数据分类:
     *
     * 公开数据
     * 内部数据
     * 敏感数据
     * 高度敏感数据
     */
    PUBLIC_DATA(1, "公开数据", "green"),
    INTERNAL_DATA(2, "内部数据", "blue"),
    MAX(3, "敏感数据", "orange"),
    MIN(4, "高度敏感数据", "red"),

    ;

    private final int value;
    private final String name;
    private final String level;

    DataClassificationEnum(int value, String name, String level) {
        this.name = name;
        this.value = value;
        this.level = level;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getLevel() {
        return level;
    }

    public static DataClassificationEnum getName(String name) {
        DataClassificationEnum[] enums = values();
        for (DataClassificationEnum typeEnum : enums) {
            String queryName = typeEnum.name();
            if (queryName.equals(name)) {
                return typeEnum;
            }
        }
        return null;
    }

}
