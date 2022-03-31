package com.fisk.dataservice.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version v1.0
 * @description api类型枚举
 * @date 2022/1/6 14:51
 */
public enum ApiTypeEnum  implements BaseEnum {

    /**
     * api类型
     */
    SQL(1,"SQL"),
    CUSTOM_SQL(2,"自定义SQL");

    ApiTypeEnum(int value, String name) {
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
