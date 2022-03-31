package com.fisk.common.core.enums.task.nifi;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author gy
 */
public enum DbPoolTypeEnum implements BaseEnum {

    /**
     * 控制器服务类型
     */
    SOURCE(0, "source db"),
    TARGET(1, "target db (doris)"),
    CONFIG(2, "config db (mysql)");

    DbPoolTypeEnum(int value, String name) {
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
