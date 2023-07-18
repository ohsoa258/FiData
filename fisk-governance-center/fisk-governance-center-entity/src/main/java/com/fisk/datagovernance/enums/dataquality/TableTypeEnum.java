package com.fisk.datagovernance.enums.dataquality;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description 表类型
 * @date 2022/5/31 16:33
 */
public enum TableTypeEnum implements BaseEnum {

    NONE(0, "空"),
    TABLE(1, "表"),
    VIEW(2, "试图");

    TableTypeEnum(int value, String name) {
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


    public static TableTypeEnum getEnum(int value) {
        for (TableTypeEnum e : TableTypeEnum.values()) {
            if (e.getValue() == value)
                return e;
        }
        return TableTypeEnum.NONE;
    }

    public static TableTypeEnum getEnumByName(String name) {
        for (TableTypeEnum e : TableTypeEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return TableTypeEnum.NONE;
    }
}
