package com.fisk.common.core.enums.fidatadatasource;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/7/6 14:49
 */
public enum TableBusinessTypeEnum implements BaseEnum {

    NONE(0, "NONE"), //空
    DW_DIMENSION(1, "dw_dimension"), //dw维度表
    DW_FACT(2, "dw_fact"), //dw事实表
    DORIS_DIMENSION(3, "doris_dimension"), //doris维度表
    DORIS_FACT(4, "doris_fact"), //doris事实表
    WIDE_TABLE(5, "wide_table"); //宽表

    TableBusinessTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
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

    public static TableBusinessTypeEnum getEnum(int value){
        for (TableBusinessTypeEnum e:TableBusinessTypeEnum.values()) {
            if(e.getValue() == value)
                return e;
        }
        return TableBusinessTypeEnum.NONE;
    }
}
