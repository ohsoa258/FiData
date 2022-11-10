package com.fisk.datamodel.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum DataModelTableTypeEnum implements BaseEnum {
    /**
     * dw_dimension
     */
    DW_DIMENSION(1, "dw_dimension"),
    DW_FACT(2, "dw_fact"),
    DORIS_DIMENSION(3, "doris_dimension"),
    DORIS_FACT(4, "doris_fact"),
    WIDE_TABLE(5, "wide_table");

    DataModelTableTypeEnum(int value, String name) {
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
}
