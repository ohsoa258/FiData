package com.fisk.task.enums;

import com.fisk.common.enums.BaseEnum;

public enum OlapTableEnum implements BaseEnum {
    /**
     * 任务状态
     */
    KPI(0,"指标表"),
    DIMENSION(1,"维度表");

    OlapTableEnum(int value, String name) {
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
