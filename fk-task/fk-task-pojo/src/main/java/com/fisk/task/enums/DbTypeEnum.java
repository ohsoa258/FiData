package com.fisk.task.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author: DennyHui
 * CreateTime: 2021/9/18 13:49
 * Description:
 */
public enum DbTypeEnum implements BaseEnum {

    sqlserver(0,"sqlserver"),
    mysql(1,"mysql"),
    postgresql(2,"postgresql"),
    oracle(3,"oracle");

    DbTypeEnum(int value, String name) {
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
