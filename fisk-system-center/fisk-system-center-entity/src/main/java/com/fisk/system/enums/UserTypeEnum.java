package com.fisk.system.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @Author: wangjian
 * @Date: 2023-03-31
 */
public enum UserTypeEnum implements BaseEnum {
    /**
     * 用户类型
     */
    ROLE(1,"用户"),
    USER(2,"角色");

    UserTypeEnum(int value, String name) {
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
