package com.fisk.dataaccess.enums.tablefield;

import com.fisk.common.core.enums.BaseEnum;

/**
 * 数据分级
 */
public enum DataLevelEnum implements BaseEnum {


    /**
     * 数据分级：
     *
     * 一级（一般数据）
     * 二级（重要数据）
     * 三级（敏感数据）
     * 四级（核心数据）
     */
    LEVEL1(1, "一级（一般数据）", "green"),
    LEVEL2(2, "二级（重要数据）", "blue"),
    LEVEL3(3, "三级（敏感数据）", "orange"),
    LEVEL4(4, "四级（核心数据）", "red"),

    ;

    private final int value;
    private final String name;
    private final String level;

    DataLevelEnum(int value, String name, String level) {
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

    public static DataLevelEnum getName(String name) {
        DataLevelEnum[] enums = values();
        for (DataLevelEnum typeEnum : enums) {
            String queryName = typeEnum.name();
            if (queryName.equals(name)) {
                return typeEnum;
            }
        }
        return null;
    }

}
