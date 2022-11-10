package com.fisk.mdm.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fisk.common.core.enums.BaseEnum;

/**
 * @author ChenYa
 * @date 2022/4/9 10:16
 */
public enum ModelVersionStatusEnum  implements BaseEnum {

    /**
     * 版本状态
     */
    OPEN(0,"打开"),

    LOCK(1,"锁定"),

    SUBMITTED(2,"已提交");

    @EnumValue
    private final int value;
    private final String name;

    ModelVersionStatusEnum(int value, String name) {
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
