package com.fisk.dataaccess.enums;


import com.fisk.common.core.enums.BaseEnum;

/**
 * @author cfk
 */

public enum PublishTypeEnum implements BaseEnum {

    /**
     * 未开始
     */
    NOT_START(0, "未开始"),
    /**
     * 发布成功
     */
    SUCCESS(1, "发布成功"),
    /**
     * 发布失败
     */
    FAIL(2, "发布失败"),
    /**
     * 正在发布
     */
    ON_GOING(3, "正在发布");

    PublishTypeEnum(int value, String name) {
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
