package com.fisk.common.core.enums.dbdatatype;

import com.fisk.common.core.enums.BaseEnum;

public enum DM8TypeEnum implements BaseEnum {

    /**
     * bigint
     */
    CHAR(1, "CHAR"),
    CHARACTER(2, "CHARACTER"),
    VARCHAR(3, "VARCHAR"),
    VARCHAR2(4, "VARCHAR2"),
    ROWID(5, "ROWID"),
    NUMERIC(6, "NUMERIC"),
    DECIMAL(7, "DECIMAL"),
    DEC(8, "DEC"),
    NUMBER(9, "NUMBER"),
    INTEGER(10, "INTEGER"),
    INT(11, "INT"),
    BIGINT(12, "BIGINT"),
    TINYINT(13, "TINYINT"),
    BYTE(14, "BYTE"),
    SMALLINT(15, "SMALLINT"),
    BINARY(16, "BINARY"),
    VARBINARY(17, "VARBINARY"),
    RAW(18, "RAW"),
    FLOAT(19, "FLOAT"),
    DOUBLE(20, "DOUBLE"),
    REAL(21, "real"),
    DOUBLE_PRECISION(22, "DOUBLE PRECISION"),
    BIT(23, "BIT"),
    DATE(24, "DATE"),
    TIME(25, "TIME"),
    TIMESTAMP(26, "TIMESTAMP"),
    DATETIME(27, "DATETIME"),
    INTERVAL_YEAR_TO_MONTH(28, "INTERVAL YEAR TO MONTH"),
    INTERVAL_YEAR(29, "INTERVAL YEAR"),
    INTERVAL_MONTH(30, "INTERVAL MONTH"),
    INTERVAL_DAY(31, "INTERVAL DAY"),
    INTERVAL_DAY_TO_HOUR(32, "INTERVAL DAY TO HOUR"),
    INTERVAL_DAY_TO_MINUTE(33, "INTERVAL DAY TO MINUTE"),
    INTERVAL_DAY_TO_SECOND(34, "INTERVAL DAY TO SECOND"),
    INTERVAL_HOUR(35, "INTERVAL HOUR"),
    INTERVAL_HOUR_TO_MINUTE(36, "INTERVAL HOUR TO MINUTE"),
    INTERVAL_HOUR_TO_SECOND(37, "INTERVAL HOUR TO SECOND"),
    INTERVAL_MINUTE(38, "INTERVAL MINUTE"),
    INTERVAL_MINUTE_TO_SECOND(39, "INTERVAL MINUTE TO SECOND"),
    INTERVAL_SECOND(39, "INTERVAL SECOND"),
    TIME_WITH_TIME_ZONE(40, "TIME WITH TIME ZONE"),
    TIMESTAMP_WITH_TIME_ZONE(41, "TIMESTAMP WITH TIME ZONE"),
    TIMESTAMP_WITH_LOCAL_TIME_ZONE(41, "TIMESTAMP WITH LOCAL TIME ZONE"),
    TEXT(42, "TEXT"),
    LONG(43, "LONG"),
    LONGVARCHAR(44, "LONGVARCHAR"),
    IMAGE(45, "IMAGE"),
    LONGVARBINARY(46, "LONGVARBINARY"),
    BLOB(47, "BLOB"),
    CLOB(48, "CLOB"),
    BFILE(49, "BFILE"),
    OTHER(99, "other"),
    ;

    private final String name;
    private final int value;

    DM8TypeEnum(int value, String name) {
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

    public static DM8TypeEnum getEnum(String value) {
        DM8TypeEnum[] typeEnums = values();
        for (DM8TypeEnum typeEnum : typeEnums) {
            String queryValue = typeEnum.getName();
            if (queryValue.equals(value)) {
                return typeEnum;
            }
        }
        return DM8TypeEnum.OTHER;
    }

}
