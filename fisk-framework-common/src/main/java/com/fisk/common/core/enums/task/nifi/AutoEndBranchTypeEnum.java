package com.fisk.common.core.enums.task.nifi;

import com.fisk.common.core.enums.BaseEnum;

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
    UNNMATCHED(5, "unmatched"),
    MATCHED(6, "matched"),
    MERGED(7, "merged"),
    SPLIT(8,"split"),
    EXPIRED(9,"expired"),
    COMMSFAILURE(10,"comms.failure"),
    NOTFOUND(11,"not.found"),
    PERMISSIONDENIED(12,"permission.denied"),
    NO_RETRY(13, "no retry"),
    RESPONSE(14, "Response");

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
