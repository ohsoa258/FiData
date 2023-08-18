package com.fisk.task.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2023-08-18
 * @Description:
 */
public enum ScheduleEnum implements BaseEnum {
    TIMER_DRIVEN(0,"TIMER_DRIVEN"),
    CRON_DRIVEN(1,"CRON_DRIVEN");

    private final String name;
    private final int value;

    ScheduleEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}