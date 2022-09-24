package com.fisk.common.core.enums.dbdatatype;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum OracleTypeEnum implements BaseEnum {
    /**
     * 数量
     */
    NUMBER(1, "NUMBER"),
    FLOAT(2, "FLOAT"),
    BINARY_FLOAT(3, "BINARY_FLOAT"),
    BINARY_DOUBLE(4, "BINARY_DOUBLE"),
    DATE(6, "DATE"),
    TIMESTAMP(7, "TIMESTAMP"),
    CHAR(8, "CHAR"),
    NCHAR(9, "NCHAR"),
    NVARCHAR2(10, "NVARCHAR2"),
    VARCHAR(11, "VARCHAR"),
    VARCHAR2(12, "VARCHAR2"),
    CLOB(13, "CLOB"),
    NCLOB(14, "NCLOB"),
    XMLType(15, "XMLType"),
    BLOB(16, "BLOB"),
    ROWID(17, "ROWID"),
    INTERVALDAYTOSECOND(18, "INTERVAL DAY TO SECOND"),
    INTERVALYEARTOMONTH(19, "INTERVAL YEAR TO MONTH"),
    DOUBLEPRECISION(20, "DOUBLE PRECISION"),
    TIMESTAMPWITHLOCALTIMEZONE(21, "TIMESTAMP WITH LOCAL TIME ZONE"),
    TIMESTAMPWITHTIMEZONE(22, "TIMESTAMP WITH TIME ZONE"),
    OTHER(-1, "OTHER");

    private final String name;
    private final int value;

    OracleTypeEnum(int value, String name) {
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

    public static OracleTypeEnum getValue(String value) {
        OracleTypeEnum[] oracleTypeEnums = values();
        for (OracleTypeEnum oracleTypeEnum : oracleTypeEnums) {
            String queryValue = oracleTypeEnum.getName();
            if (queryValue.equals(value)) {
                return oracleTypeEnum;
            }
        }
        return OracleTypeEnum.OTHER;
    }

}
