package com.fisk.common.core.enums.dbdatatype;

import com.fisk.common.core.enums.BaseEnum;

/**
 * SAP BW字段类型的枚举类型
 */
public enum SapBwFieldTypeEnum implements BaseEnum {
    /**
     * bigint
     */
    BIGINT(1, "bigint"),
    BINARY(2, "binary"),
    BIT(3, "bit"),
    CHAR(4, "char"),
    DATE(5, "date"),
    DATETIME(6, "datetime"),
    DATETIME2(7, "datetime2"),
    DECIMAL(8, "decimal"),
    FLOAT(9, "float"),
    INT(10, "int"),
    MONEY(11, "money"),
    NCHAR(12, "nchar"),
    NVARCHAR(13, "nvarchar"),
    REAL(14, "real"),
    SMALLINT(15, "smallint"),
    SMALLMONEY(16, "smallmoney"),
    TEXT(17, "text"),
    TIME(18, "time"),
    TIMESTAMP(19, "timestamp"),
    TINYINT(20, "tinyint"),
    VARBINARY(21, "varbinary"),
    VARCHAR(22, "varchar"),
    XML(23, "xml"),
    OTHER(24, "other");

    private final String name;
    private final int value;

    SapBwFieldTypeEnum(int value, String name) {
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

    public static SapBwFieldTypeEnum getValue(String value) {
        SapBwFieldTypeEnum[] typeEnums = values();
        for (SapBwFieldTypeEnum typeEnum : typeEnums) {
            String queryValue = typeEnum.getName();
            if (queryValue.equals(value)) {
                return typeEnum;
            }
        }
        return SapBwFieldTypeEnum.OTHER;
    }
}