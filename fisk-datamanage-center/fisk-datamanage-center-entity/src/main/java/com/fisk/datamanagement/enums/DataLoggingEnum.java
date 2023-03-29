package com.fisk.datamanagement.enums;

import com.fisk.common.core.enums.BaseEnum;

public enum DataLoggingEnum implements BaseEnum {
    TOTAL_NUMBER_OF_RECORDS(1,"DataLoggingtotalNumberOfRecords"),
    DAILY_GAIN(2,"DataLoggingDailyGain")
    ;
    DataLoggingEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    private final int value;
    private final String name;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}
