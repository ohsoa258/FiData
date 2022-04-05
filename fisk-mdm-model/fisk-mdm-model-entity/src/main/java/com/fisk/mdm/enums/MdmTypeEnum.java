package com.fisk.mdm.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author WangYan
 * @date 2022/4/5 12:11
 */
public enum MdmTypeEnum implements BaseEnum {

    /**
     * mdm类型
     */
    CODE(0,"code"),

    NAME(1,"name"),

    BUSINESSFIELD(2,"业务字段");

    MdmTypeEnum(int value, String name) {
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
