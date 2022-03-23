package com.fisk.datamodel.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum PublicStatusEnum implements BaseEnum {

    UN_PUBLIC(0,"未发布"),

    PUBLIC_SUCCESS(1,"发布成功"),

    PUBLIC_FAILURE(2,"发布失败"),

    PUBLIC_ING(3,"正在发布");

    PublicStatusEnum(int value, String name) {
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
