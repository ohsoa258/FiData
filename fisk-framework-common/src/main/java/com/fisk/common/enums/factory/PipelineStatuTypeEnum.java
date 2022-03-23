package com.fisk.common.enums.factory;

import com.fisk.common.enums.BaseEnum;

/**
 * @author cfk
 */
public enum PipelineStatuTypeEnum implements BaseEnum {
    /**
     * 0: 未发布  1: 发布成功  2: 发布失败  3: 正在发布
     */
    no_publish(0, "未发布"),
    success_publish(1, "发布成功"),
    failure_publish(2, "发布失败"),
    being_publish(3, "正在发布");

    PipelineStatuTypeEnum(int value, String name) {
        this.name = name;
        this.value = value;
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
