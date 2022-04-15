package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @author ChenYa
 */
public enum ModelVersionTypeEnum implements BaseEnum {

    /**
     * 版本创建类型
     */
    USER_CREAT(1,"用户手动创建"),

    SYSTEM_CREAT(2,"系统job自动创建");

    @EnumValue
    private final int value;
    private final String name;

    ModelVersionTypeEnum(int value, String name) {
        this.value=value;
        this.name=name;
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
