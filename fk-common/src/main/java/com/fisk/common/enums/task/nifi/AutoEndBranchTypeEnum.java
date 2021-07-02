package com.fisk.common.enums.task.nifi;

import com.fisk.common.enums.BaseEnum;

/**
 * @author gy
 */

public enum AutoEndBranchTypeEnum implements BaseEnum {

    /**
     * Processor组件处理结果类型
     */
    SUCCESS(0, "success"),
    FAILURE(1, "failure"),
    ORIGINAL(2, "original"),
    RETRY(3, "retry"),
    SQL(4, "sql"),
    UNNMATCHED(5, "unmatched");

    AutoEndBranchTypeEnum(int value, String name) {
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
