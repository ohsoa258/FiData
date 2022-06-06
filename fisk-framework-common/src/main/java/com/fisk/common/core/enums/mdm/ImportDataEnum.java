package com.fisk.common.core.enums.mdm;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JianWenYang
 * @date 2022-05-16 22:02
 */
public enum ImportDataEnum implements BaseEnum {

    /**
     * 列名
     */
    COLUMN_NAME(0, "列名"),

    COLUMN_VALUE(1, "列值");


    private final String name;
    private final int value;

    ImportDataEnum(int value, String name) {
        this.name = name;
        this.value = value;
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
