package com.fisk.common.core.enums.flink;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum UploadWayEnum implements BaseEnum {

    /**
     * 本地上传
     */
    DEV(1, "dev"),
    /**
     * 远程上传
     */
    SSH(2, "ssh");

    private final String name;
    private final int value;

    UploadWayEnum(int value, String name) {
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
