package com.fisk.datagovernance.enums.datasecurity;

import com.fisk.common.core.enums.BaseEnum;

public enum ScanReceptionTypeEnum implements BaseEnum {
    NONE(0,"空"),
    EMAIL_NOTICE(1, "邮件通知"),
    IN_STATION_NOTICE(2, "站内通知"),
    WECHAT_NOTICE(3, "微信通知"),
    SMS_NOTICE(4, "短信通知");

    ScanReceptionTypeEnum(int value, String name) {
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

    public static ScanReceptionTypeEnum getEnum(int value){
        for (ScanReceptionTypeEnum e: ScanReceptionTypeEnum.values()) {
            if(e.getValue() == value)
                return e;
        }
        return ScanReceptionTypeEnum.NONE;
    }
}
