package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @author ChenYa
 */
public enum AttributeSyncStatusEnum implements BaseEnum {

    /**
     * 提交状态
     */
    ERROR(0,"提交失败"),
    SUCCESS(1,"提交成功");

    @EnumValue
    private final int value;
    private final String name;


    AttributeSyncStatusEnum(int value, String name) {
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
