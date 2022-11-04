package com.fisk.common.core.enums.dbdatatype;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum PgTypeEnum implements BaseEnum {
    /**
     * bit
     */
    BIT(1, "bit"),
    BOOL(2, "bool"),
    BOX(3, "box"),
    BYTEA(4, "bytea"),
    CHAR(5, "char"),
    CIDR(6, "cidr"),
    CIRCLE(7, "circle"),
    DATE(8, "date"),
    DECIMAL(9, "decimal"),
    FLOAT4(10, "float4"),
    FLOAT8(12, "float8"),
    INET(13, "inet"),
    INT2(14, "int2"),
    INT4(15, "int4"),
    INT8(16, "int8"),
    INTERVAL(17, "interval"),
    JSON(18, "json"),
    JSONB(19, "jsonb"),
    LINE(20, "line"),
    LSEG(21, "lseg"),
    MACADDR(22, "macaddr"),
    MONEY(23, "money"),
    NUMERIC(24, "numeric"),
    PATH(25, "path"),
    POINT(26, "point"),
    POLYGON(27, "polygon"),
    SERIAL2(28, "serial2"),
    SERIAL4(29, "serial4"),
    SERIAL8(30, "serial8"),
    TEXT(31, "text"),
    TIME(32, "time"),
    TIMESTAMP(33, "timestamp"),
    TIMESTAMPtz(34, "timestamptz"),
    TIMETZ(35, "timetz"),
    TSQUERY(36, "tsquery"),
    TSVECTOR(37, "tsvector"),
    TXID_SNAPshot(38, "txid_snapshot"),
    UUID(39, "uuid"),
    VARBIT(40, "varbit"),
    VARCHAR(41, "varchar"),
    XML(42, "xml"),
    OTHER(43, "other");

    private final String name;
    private final int value;

    PgTypeEnum(int value, String name) {
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

    public static PgTypeEnum getValue(String value) {
        PgTypeEnum[] typeEnums = values();
        for (PgTypeEnum typeEnum : typeEnums) {
            String queryValue = typeEnum.getName();
            if (queryValue.equals(value)) {
                return typeEnum;
            }
        }
        return PgTypeEnum.OTHER;
    }

}
