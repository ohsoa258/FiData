package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @author WangYan
 * @date 2022/4/5 12:11
 */
public enum ObjectTypeEnum implements BaseEnum {

    /**
     * 对象类型
     */
    MODEL(0,"模型"),

    ENTITY(1,"实体"),

    ATTRIBUTES(2,"属性");

    ObjectTypeEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    @EnumValue
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
