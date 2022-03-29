package com.fisk.datagovernance.enums.datasecurity;

import com.fisk.common.enums.BaseEnum;

/**
 * @author Lock
 * @version 1.0
 * @description 表类型(0: 表级  1: 行级  2: 列级)
 * @date 2022/3/22 14:01
 */
public enum SecurityTableTypeEnum implements BaseEnum {
    /**
     * 校验规则类型
     */
    NONE(0,"空"),
    TABLE_SECURITY(1, "表级安全"),
    ROW_SECURITY(2, "行级安全"),
    COLUMN_CHECK(2, "列级安全");

    SecurityTableTypeEnum(int value, String name) {
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

    public static SecurityTableTypeEnum getEnum(int value){
        for (SecurityTableTypeEnum e: SecurityTableTypeEnum.values()) {
            if(e.getValue() == value)
                return e;
        }
        return SecurityTableTypeEnum.NONE;
    }
}
