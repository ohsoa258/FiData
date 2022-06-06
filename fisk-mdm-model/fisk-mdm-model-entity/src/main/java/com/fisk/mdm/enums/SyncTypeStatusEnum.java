package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum SyncTypeStatusEnum implements BaseEnum {

    /**
     * 修改
     */
    UPDATE(1, "修改"),

    INSERT(2, "新增");

    @EnumValue
    private final int value;
    private final String name;

    SyncTypeStatusEnum(int value, String name) {
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
