package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 通知类型
 * @date 2022/3/22 14:03
 */
public enum NoticeTypeEnum implements BaseEnum {
    /**
     * 表状态
     */
    EMAIL_NOTICE(1,"邮件通知"),
    SYSTEM_NOTICE(2,"站内通知");

    NoticeTypeEnum(int value, String name) {
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
