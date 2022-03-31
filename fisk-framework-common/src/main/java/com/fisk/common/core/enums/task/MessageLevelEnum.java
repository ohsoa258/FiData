package com.fisk.common.core.enums.task;

import com.fisk.common.core.enums.BaseEnum;

/**
 *
 */
public enum MessageLevelEnum implements BaseEnum {
    /**
     * 消息发送等级
     */
    LOW(0,"低"),

    MEDIUM(1,"中"),

    HIGH(2,"高");

    MessageLevelEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    private final String name;
    private final int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}
