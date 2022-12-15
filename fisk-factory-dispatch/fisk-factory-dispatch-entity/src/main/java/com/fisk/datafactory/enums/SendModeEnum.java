package com.fisk.datafactory.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author cfk
 */

public enum SendModeEnum implements BaseEnum {
    /**
     * 成功的时候发通知
     */
    success(1,"成功的时候发通知"),
    /**
     *失败的时候发通知
     */
    failure(2,"失败的时候发通知"),
    /**
     *完成的时候发通知
     */
    finish(3,"完成的时候发通知");

    SendModeEnum(int value, String name) {
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
