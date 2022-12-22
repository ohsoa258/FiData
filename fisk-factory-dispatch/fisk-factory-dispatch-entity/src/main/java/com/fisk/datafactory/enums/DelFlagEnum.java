package com.fisk.datafactory.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author SongJianJian
 *
 * 删除标志枚举
 */
public enum DelFlagEnum implements BaseEnum {

    DELETE_FLAG(0, "已删除"),
    NORMAL_FLAG(1, "未删除");

    DelFlagEnum(int value, String name){
        this.value = value;
        this.name = name;
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
