package com.fisk.dataservice.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 日志类型
 * @date 2022/3/7 12:06
 */
public enum LogTypeEnum implements BaseEnum {

    /**
     * 日志类型
     */
    API(100, "数据服务API"),
    SCAN_API(200, "扫描数据服务API");

    LogTypeEnum(int value, String name) {
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
