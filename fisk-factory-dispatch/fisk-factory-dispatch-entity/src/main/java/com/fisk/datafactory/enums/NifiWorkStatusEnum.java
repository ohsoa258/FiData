package com.fisk.datafactory.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * Nifi管道工作状态枚举
 * @author: SongJianJian
 */
public enum NifiWorkStatusEnum implements BaseEnum {

    RUNNING_STATUS(1,"运行状态"),
    SUSPEND_STATUS(2,"暂停状态");

    NifiWorkStatusEnum(int value, String name) {
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
