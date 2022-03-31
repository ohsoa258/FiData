package com.fisk.common.core.enums.task;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author cfk
 */
public enum TopicTypeEnum implements BaseEnum {
    NO_TYPE(0,"无类别常量"),
    DAILY_NIFI_FLOW(1, "日常调度"),
    PIPELINE_NIFI_FLOW(2, "管道调度"),
    COMPONENT_NIFI_FLOW(3,"组件调度");


    private final String name;
    private final int value;

    TopicTypeEnum(int value, String name) {
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
