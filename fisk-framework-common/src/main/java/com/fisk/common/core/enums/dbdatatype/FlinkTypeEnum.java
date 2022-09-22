package com.fisk.common.core.enums.dbdatatype;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum FlinkTypeEnum implements BaseEnum {

    /**
     * 非常小的整数
     */
    TINYINT(1, "TINYINT"),
    SMALLINT(2, "SMALLINT"),
    INT(3, "INT"),
    BIGINT(4, "BIGINT"),
    DECIMAL(5, "DECIMAL"),
    STRING(6, "STRING"),
    FLOAT(7, "FLOAT"),
    DOUBLE(8, "DOUBLE"),
    BOOLEAN(9, "BOOLEAN"),
    TIMESTAMP(10, "TIMESTAMP"),
    TIMESTAMP_LTZ(11, "TIMESTAMP_LTZ"),
    BYTES(12, "BYTES"),
    TIME(13, "TIME"),
    CHAR(14, "CHAR"),
    VARCHAR(15, "VARCHAR");

    private final String name;
    private final int value;

    FlinkTypeEnum(int value, String name) {
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


}
