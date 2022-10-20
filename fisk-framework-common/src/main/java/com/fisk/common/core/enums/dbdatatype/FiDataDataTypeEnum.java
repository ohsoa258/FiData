package com.fisk.common.core.enums.dbdatatype;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum FiDataDataTypeEnum implements BaseEnum {

    /**
     * 字符串
     */
    NVARCHAR(1, "NVARCHAR"),
    FLOAT(2, "FLOAT"),
    TIMESTAMP(3, "TIMESTAMP"),
    TEXT(4, "TEXT"),
    INT(5, "INT"),
    OTHER(-1, "OTHER");

    private final String name;
    private final int value;

    FiDataDataTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static FiDataDataTypeEnum getValue(String value) {
        FiDataDataTypeEnum[] fiDataTypeEnums = values();
        for (FiDataDataTypeEnum typeEnum : fiDataTypeEnums) {
            String queryValue = typeEnum.getName();
            if (queryValue.equals(value)) {
                return typeEnum;
            }
        }
        return FiDataDataTypeEnum.OTHER;
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
