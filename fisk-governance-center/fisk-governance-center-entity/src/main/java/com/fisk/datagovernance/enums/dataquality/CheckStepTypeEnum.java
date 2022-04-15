package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 校验步骤
 * @date 2022/3/22 14:00
 */
public enum CheckStepTypeEnum implements BaseEnum {
    /**
     * 校验步骤
     */
    NONE(0, "空"),
    TABLE_FRONT(1, "进表前"),
    TABLE_AFTER(2, "进表后");

    CheckStepTypeEnum(int value, String name) {
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

    public static CheckStepTypeEnum getEnum(int value) {
        for (CheckStepTypeEnum e : CheckStepTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return CheckStepTypeEnum.NONE;
    }
}
