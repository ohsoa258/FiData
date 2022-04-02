package com.fisk.task.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author gy
 */
public enum TaskStatusEnum implements BaseEnum {

    /**
     * 任务状态
     */
    TASK_BUILD(0,"任务创建成功"),
    PROCESSING(1,"处理中"),
    SUCCESS(2,"处理成功"),
    FAILURE(3,"处理失败");

    TaskStatusEnum(int value, String name) {
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
