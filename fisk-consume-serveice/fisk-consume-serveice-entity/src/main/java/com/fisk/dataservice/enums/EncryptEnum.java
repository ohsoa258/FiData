package com.fisk.dataservice.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2024-07-10
 * @Description:
 */
public enum EncryptEnum implements BaseEnum {
    /**
     * api类型
     */
    KEEP_ONE_STAR_REST(1,"保留第一位，后面的字符用星号展示"),
    KEEP_FIRST_3_LAST_4(2,"保留前三位和后四位，中间的内容用星号展示"),
    KEEP_FIRST_3_MID_4_LAST(3,"保留前三位、中间四位和后四位，中间的内容用星号展示"),
    ALL_STAR(4,"全部用星号展示");

    EncryptEnum(int value, String name) {
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
