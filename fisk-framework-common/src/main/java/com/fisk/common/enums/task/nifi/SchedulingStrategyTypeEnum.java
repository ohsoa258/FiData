package com.fisk.common.enums.task.nifi;

import com.fisk.common.enums.BaseEnum;

/**
 * @author gy
 */
public enum SchedulingStrategyTypeEnum implements BaseEnum {
    /**
     * Processor组件类型
     */
    TIMER(0, "TIMER_DRIVEN"),
    CRON(1, "CRON_DRIVEN"),
    EVENT(2, "EVENT_DRIVEN");

    SchedulingStrategyTypeEnum(int value, String name) {
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
