package com.fisk.datamodel.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum DataBaseTypeEnum implements BaseEnum {

    /**
     * MySql
     */
    MYSQL(1,"mysql"),
    SQL_SERVER(2,"sqlserver"),
    ORACLE(3,"oracle"),
    POSTGRESQL(4,"postgresql"),
    DORIS(5,"doris"),
    OTHER(-1,"其他");

    DataBaseTypeEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    private final int value;
    private final String name;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    public static DataBaseTypeEnum getValue(String value) {
        DataBaseTypeEnum[] carTypeEnums = values();
        for (DataBaseTypeEnum carTypeEnum : carTypeEnums) {
            String queryValue=carTypeEnum.getName();
            if (queryValue.equals(value)) {
                return carTypeEnum;
            }
        }
        return DataBaseTypeEnum.OTHER;
    }

}
