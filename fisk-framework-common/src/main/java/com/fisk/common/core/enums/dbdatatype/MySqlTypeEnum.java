package com.fisk.common.core.enums.dbdatatype;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum MySqlTypeEnum implements BaseEnum {

    /**
     * bigint
     */
    BIGINT(1, "bigint"),
    BINARY(2, "binary"),
    BIT(3, "bit"),
    BLOB(4, "blob"),
    CHAR(5, "char"),
    DATE(6, "date"),
    DATETIME(7, "datetime"),
    DECIMAL(8, "decimal"),
    DOUBLE(9, "double"),
    ENUM(10, "enum"),
    FLOAT(11, "float"),
    GEOMETRY(12, "geometry"),
    GEOMETRYCOLLECTION(13, "geometrycollection"),
    INT(14, "int"),
    INTEGER(15, "integer"),
    JSON(16, "json"),
    LINESTRING(17, "linestring"),
    LONGBLOB(18, "longblob"),
    LONGTEXT(19, "longtext"),
    MEDIUMBLOB(19, "mediumblob"),
    MEDIUMINT(20, "mediumint"),
    MEDIUMTEXT(21, "mediumtext"),
    MULTILINESTRING(22, "multilinestring"),
    MULTIPOINT(23, "multipoint"),
    MULTIPOLYGON(24, "multipolygon"),
    NUMERIC(25, "numeric"),
    POINT(26, "point"),
    POLYGON(27, "polygon"),
    REAL(28, "real"),
    SET(29, "set"),
    SMALLINT(30, "smallint"),
    TEXT(31, "text"),
    TIME(32, "time"),
    TIMESTAMP(33, "timestamp"),
    TINYBLOB(34, "tinyblob"),
    TINYINT(35, "tinyint"),
    TINYTEXT(36, "tinytext"),
    VARBINARY(37, "varbinary"),
    VARCHAR(38, "varchar"),
    YEAR(39, "year"),
    OTHER(40, "other");

    private final String name;
    private final int value;

    MySqlTypeEnum(int value, String name) {
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

    public static MySqlTypeEnum getValue(String value) {
        MySqlTypeEnum[] typeEnums = values();
        for (MySqlTypeEnum typeEnum : typeEnums) {
            String queryValue = typeEnum.getName();
            if (queryValue.equals(value)) {
                return typeEnum;
            }
        }
        return MySqlTypeEnum.OTHER;
    }

}
