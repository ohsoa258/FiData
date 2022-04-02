package com.fisk.common.core.enums.task;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author gy
 */
public enum MessageStatusEnum implements BaseEnum {


    /**
     * 未读
     */
    UNREAD(0, "未读"),
    /**
     * 已读
     */
    READ(1, "已读");

    MessageStatusEnum(int value, String name) {
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
