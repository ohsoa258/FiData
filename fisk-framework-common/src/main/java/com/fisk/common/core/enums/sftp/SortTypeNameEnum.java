package com.fisk.common.core.enums.sftp;


import com.fisk.common.core.enums.BaseEnum;

/**
 * @author SongJianJian
 */

public enum SortTypeNameEnum implements BaseEnum {

    /**
     * 未开始
     */
    FILENAME_SORT(1, "文件名排序"),
    TIME_SORT(2, "时间排序");



    SortTypeNameEnum(int value, String name) {
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
