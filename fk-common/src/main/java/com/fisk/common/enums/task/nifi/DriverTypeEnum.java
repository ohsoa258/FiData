package com.fisk.common.enums.task.nifi;

import com.fisk.common.enums.BaseEnum;

/**
 * @author gy
 */
public enum DriverTypeEnum implements BaseEnum {

    /**
     * 控制器服务类型
     */
    MYSQL(0, "com.mysql.jdbc.Driver");

    DriverTypeEnum(int value, String name) {
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
