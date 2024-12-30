package com.fisk.common.core.enums.dbdatatype;

import com.fisk.common.core.enums.BaseEnum;

public enum PITypeEnum implements BaseEnum {

    /**
     * bigint
     */
    Integer(1, "Integer"),
    Float(2, "Float"),
    Real(3, "Real"),
    String(4, "String"),
    Boolean(5, "Boolean"),
    Binary(6, "Binary"),
    ;

    private final String name;
    private final int value;

    PITypeEnum(int value, String name) {
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

    public static PITypeEnum getEnum(String value) {
        PITypeEnum[] typeEnums = values();
        for (PITypeEnum typeEnum : typeEnums) {
            String queryValue = typeEnum.getName();
            if (queryValue.equals(value)) {
                return typeEnum;
            }
        }
        return PITypeEnum.String;
    }

}
