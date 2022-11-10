package com.fisk.common.core.enums.dbdatatype;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum FiDataDataTypeEnum implements BaseEnum {

    /**
     * 字符串
     */
    NVARCHAR(1, "NVARCHAR", "字符串型"),
    FLOAT(2, "FLOAT", "浮点型"),
    TIMESTAMP(3, "TIMESTAMP", "时间戳类型"),
    TEXT(4, "TEXT", "文本型"),
    INT(5, "INT", "整型"),
    OTHER(-1, "OTHER", "其他");

    private final String name;
    private final int value;
    private final String description;

    FiDataDataTypeEnum(int value, String name, String description) {
        this.value = value;
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

}
