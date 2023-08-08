package com.fisk.datamanagement.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2023-08-07
 * @Description:
 */
public enum AzureTypeEnum implements BaseEnum {
    /**
     * 请求成功
     */
    CHAT(1,"语义查询"),
    SQL(2, "SQL查询");

    AzureTypeEnum(int value, String name) {
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
