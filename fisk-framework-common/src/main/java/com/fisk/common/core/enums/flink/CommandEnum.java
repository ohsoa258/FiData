package com.fisk.common.core.enums.flink;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum CommandEnum implements BaseEnum {

    /**
     * 普通方式
     */
    DEV(1, "dev"),
    /**
     * 远程上传
     */
    YARN(2, "yarn");

    private final String name;
    private final int value;

    CommandEnum(int value, String name) {
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
