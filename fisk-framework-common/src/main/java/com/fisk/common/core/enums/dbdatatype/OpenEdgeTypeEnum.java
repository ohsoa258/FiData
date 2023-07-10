package com.fisk.common.core.enums.dbdatatype;

import com.fisk.common.core.enums.BaseEnum;

public enum OpenEdgeTypeEnum implements BaseEnum {
    BIGINT(1, "bigint"),
    BINARY(2, "binary"),
    LOGICAL(3, "logical"),
    CHAR(4, "char"),
    DATE(5, "date"),
    DATETIME(6, "datetime"),
    DATETIMETZ(7, "datetime-tz"),
    DECIMAL(8, "decimal"),
    FLOAT(9, "float"),
    GEOGRAPHY(10, "geography"),
    GEOMETRY(11, "geometry"),
    HIERARCHYID(12, "hierarchyid"),
    IMAGE(13, "image"),
    INTEGER(14, "integer"),
    MONEY(15, "money"),
    NCHAR(16, "nchar"),
    CLOB(17, "clob"),
    DECIMALPS(18, "decimal(p, s)"),
    NVARCHAR(19, "nvarchar"),
    REAL(20, "real"),
    SMALLDATETIME(21, "smalldatetime"),
    SMALLINT(22, "smallint"),
    SMALLMONEY(23, "smallmoney"),
    SQL_VARIANT(24, "sql_variant"),
    TEXT(25, "text"),
    TIME(26, "time"),
    TIMESTAMP(27, "timestamp"),
    TINYINT(28, "tinyint"),
    UNIQUEIDENTIFIER(29, "uniqueidentifier"),
    VARBINARY(30, "varbinary"),
    VARCHAR(31, "varchar"),
    XML(32, "xml"),
    OTHER(33, "other");
    private final String name;
    private final int value;

    OpenEdgeTypeEnum(int value, String name) {
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

    public static OpenEdgeTypeEnum getValue(String value) {
        OpenEdgeTypeEnum[] typeEnums = values();
        for (OpenEdgeTypeEnum typeEnum : typeEnums) {
            String queryValue = typeEnum.getName();
            if (queryValue.equals(value)) {
                return typeEnum;
            }
        }
        return OpenEdgeTypeEnum.OTHER;
    }
}