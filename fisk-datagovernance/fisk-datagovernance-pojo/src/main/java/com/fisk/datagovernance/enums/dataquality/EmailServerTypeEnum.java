package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 邮件服务器类型
 * @date 2022/3/22 14:07
 */
public enum EmailServerTypeEnum implements BaseEnum {
    /**
     * 邮件服务器类型
     */
    SMTP(1,"SMTP"),
    IMAP(2,"IMAP"),
    POP3(3,"POP3");

    EmailServerTypeEnum(int value, String name) {
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
