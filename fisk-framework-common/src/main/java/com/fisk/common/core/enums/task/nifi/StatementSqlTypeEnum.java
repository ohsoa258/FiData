package com.fisk.common.core.enums.task.nifi;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author gy
 */
public enum StatementSqlTypeEnum implements BaseEnum {

    /**
     * 生成的sql类型
     */
    INSERT(0, "INSERT"),
    UPDATE(1, "UPDATE"),
    DELETE(2, "DELETE");

    StatementSqlTypeEnum(int value, String name) {
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
