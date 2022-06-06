package com.fisk.datamanagement.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 */
public enum TableTypeEnum implements BaseEnum {

    /**
     * ods物理表
     */
    PHYSICS(0,"ods物理表"),
    /**
     * dw维度
     */
    DW_DIMENSION(1,"dw_dimension"),
    DW_FACT(2,"dw_fact"),
    DORIS_DIMENSION(3,"doris_dimension"),
    DORIS_FACT(4,"doris_fact");

    TableTypeEnum(int value, String name) {
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

    public static TableTypeEnum getEnum(int code) {
        for (TableTypeEnum enums : TableTypeEnum.values()) {
            if (enums.getValue() == code) {
                return enums;
            }
        }
        return TableTypeEnum.PHYSICS;
    }

}
