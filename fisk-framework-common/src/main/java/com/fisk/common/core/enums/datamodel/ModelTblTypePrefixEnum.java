package com.fisk.common.core.enums.datamodel;

import com.fisk.common.core.enums.BaseEnum;

/**
 * 该枚举类用于数仓建模发布表时，如果数仓是doris类型，则用来区分：
 * 1、dim维度表建表时是主键模型
 * 2、fact表建表时是冗余模型
 */
public enum ModelTblTypePrefixEnum implements BaseEnum {

    DIM("dim_", 0),
    FACT("fact_", 1),
    HELP("help_", 2),
    CONFIG("config_", 3),
    DWD("dwd_", 4),
    DWS("dws_", 5),
    ;

    ModelTblTypePrefixEnum(String name, int value) {
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

    public static ModelTblTypePrefixEnum getEnum(String name) {
        for (ModelTblTypePrefixEnum e : ModelTblTypePrefixEnum.values()) {
            if (e.getName().equals(name))
                return e;
        }
        return null;
    }
}
