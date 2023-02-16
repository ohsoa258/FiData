package com.fisk.common.core.enums.sftp;

import com.fisk.common.core.enums.BaseEnum;


public enum SourceTypeEnum implements BaseEnum {

    SFTP(1, "SFTP"),
    WINDOWS(2, "WINDOWS");



    SourceTypeEnum(int value, String name) {
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
