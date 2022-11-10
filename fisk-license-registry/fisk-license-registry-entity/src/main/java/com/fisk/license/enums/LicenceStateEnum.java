package com.fisk.license.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 许可证状态
 * @date 2022/11/9 14:19
 */
public enum LicenceStateEnum implements BaseEnum {
    /**
     * 许可证状态
     */
    LICENCE_NONE(0, "Licence不存在"),
    LICENCE_UNAUTHORIZED(1, "Licence未授权"),
    LICENCE_AUTHORIZED(2, "Licence已授权"),
    LICENCE_EXPIRED(3, "Licence已过期"),
    LICENCE_DISABLED(4, "Licence已禁用");

    LicenceStateEnum(int value, String name) {
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

    public static LicenceStateEnum getEnum(int value) {
        for (LicenceStateEnum e : LicenceStateEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return LicenceStateEnum.LICENCE_NONE;
    }
}
