package com.fisk.dataservice.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 日志错误等级
 * @date 2022/3/7 14:22
 */
public enum LogLevelTypeEnum implements BaseEnum {

    /**
     * 日志错误等级
     */
    DEBUG(100, "DEBUG"),
    INFO(200, "INFO"),
    WARNING(300, "WARNING"),
    ERROR(400, "ERROR");

    LogLevelTypeEnum(int value, String name) {
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
