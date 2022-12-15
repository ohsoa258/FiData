package com.fisk.dataaccess.enums;


import com.fisk.common.core.enums.BaseEnum;

/**
 * @author SongJianJian
 */

public enum SortTypeEnum implements BaseEnum {

    /**
     * 未开始
     */
    POSITIVE_SORT(1, "正序"),
    REVERSE_SORT(2, "倒序");


    SortTypeEnum(int value, String name) {
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
