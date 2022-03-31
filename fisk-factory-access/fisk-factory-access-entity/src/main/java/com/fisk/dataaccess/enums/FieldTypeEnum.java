package com.fisk.dataaccess.enums;

import com.fisk.common.core.enums.BaseEnum;

public enum FieldTypeEnum implements BaseEnum {
    INT(1, "INT"),
    STRING1(2, "VARCHAR"),
    DATETIME(3, "TIMESTAMP"),
    STRING2(4, "TEXT"),
    OTHER(0, "其他类型");

    FieldTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    private final String name;
    private final int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    public static FieldTypeEnum getValue(String name) {
        FieldTypeEnum[] fieldTypeEnums = values();
        for (FieldTypeEnum fieldTypeEnum : fieldTypeEnums) {
            String queryName = fieldTypeEnum.name;
            if (queryName.equals(name)) {
                return fieldTypeEnum;
            }
        }
        return null;
    }
}