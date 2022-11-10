package com.fisk.task.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author cfk
 */
public enum TaskStateEnum implements BaseEnum {
    start(0,"任务发布中心的开始模块"),
    end(1,"任务发布中心的下一级处理模块")
    ;

    private final String name;
    private final int value;

    TaskStateEnum(int value, String name) {
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
