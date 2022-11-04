package com.fisk.common.core.enums.dbdatatype;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum SqlServerTypeEnum implements BaseEnum {

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
    DATETIMEOFFSET(8, "datetimeoffset"),
    DECIMAL(9, "decimal"),
    FLOAT(10, "float"),
    GEOGRAPHY(11, "geography"),
    GEOMETRY(12, "geometry"),
    HIERARCHYID(13, "hierarchyid"),
    IMAGE(14, "image"),
    INT(15, "int"),
    MONEY(16, "money"),
    NCHAR(17, "nchar"),
    NTEXT(18, "ntext"),
    NUMERIC(19, "numeric"),
    NVARCHAR(20, "nvarchar"),
    REAL(21, "real"),
    SMALLDATETIME(22, "smalldatetime"),
    SMALLINT(23, "smallint"),
    SMALLMONEY(24, "smallmoney"),
    SQL_VARIANT(25, "sql_variant"),
    TEXT(26, "text"),
    TIME(27, "time"),
    TIMESTAMP(28, "timestamp"),
    TINYINT(29, "tinyint"),
    UNIQUEIDENTIFIER(30, "uniqueidentifier"),
    VARBINARY(31, "varbinary"),
    VARCHAR(32, "varchar"),
    XML(33, "xml"),
    OTHER(34, "other");

    private final String name;
    private final int value;

    SqlServerTypeEnum(int value, String name) {
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

    public static SqlServerTypeEnum getValue(String value) {
        SqlServerTypeEnum[] typeEnums = values();
        for (SqlServerTypeEnum typeEnum : typeEnums) {
            String queryValue = typeEnum.getName();
            if (queryValue.equals(value)) {
                return typeEnum;
            }
        }
        return SqlServerTypeEnum.OTHER;
    }

}
