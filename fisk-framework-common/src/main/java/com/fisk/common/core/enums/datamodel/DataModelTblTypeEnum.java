package com.fisk.common.core.enums.datamodel;

import com.fisk.common.core.enums.BaseEnum;

/**
 * 该枚举类用于数仓建模发布表时，如果数仓是doris类型，则用来区分：
 * 1、dim维度表建表时是主键模型
 * 2、fact表建表时是冗余模型
 */
public enum DataModelTblTypeEnum implements BaseEnum {

    DIM("DIM", 0),
    FACT("FACT", 1),
    HELP("HELP", 2),
    CONFIG("CONFIG", 3),
    ;

    DataModelTblTypeEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }

    private final String name;
    private final int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    public static DataModelTblTypeEnum getEnum(String name) {
        for (DataModelTblTypeEnum e : DataModelTblTypeEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return null;
    }
}
