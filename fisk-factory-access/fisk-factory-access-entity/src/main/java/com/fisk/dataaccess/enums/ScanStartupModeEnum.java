package com.fisk.dataaccess.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 * oracle-cdc扫描启动模式
 */
public enum ScanStartupModeEnum implements BaseEnum {

    /**
     * 从最开始读
     */
    STARTING_POSITION(0, "initial"),
    /**
     * 从最新位置开始读
     */
    LATEST_LOCATION(1, "latest-offset");

    private final int value;
    private final String name;

    ScanStartupModeEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

}
