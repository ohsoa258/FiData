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
    FACTTABLE(1, "FACTTABLE"), //事实表
    DIMENSIONTABLE(2, "DIMENSIONTABLE"), //维度表
    QUOTATABLE(3, "QUOTATABLE"), //指标表
    WIDETABLE(4, "WIDETABLE"); //宽表

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
