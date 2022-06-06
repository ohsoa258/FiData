package com.fisk.system.relenish;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author gy
 * @version 1.0
 * @description 用户字段列表
 * @date 2022/4/22 13:20
 */
public enum UserFieldEnum implements BaseEnum {
    /**
     * 用户字段列表
     */
    USER_NAME(0, "username"),
    USER_ACCOUNT(1, "user_account");

    private final String name;
    private final int value;

    UserFieldEnum(int value, String name) {
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
