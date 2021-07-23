package com.fisk.task.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/21 15:22
 * Description:
 */
public enum OdsDataSyncTypeEnum implements BaseEnum {
    /**
     * 任务状态
     */
    full_volume(0,"full_volume"),
    timestamp_incremental(1,"timestamp_incremental"),
    business_time_cover(2,"business_time_cover");

    OdsDataSyncTypeEnum(int value, String name) {
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
