package com.fisk.dataaccess.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author Lock
 */
public enum ModuleTypeEnum implements BaseEnum {
    /**
     * 查询类型
     */
    DATA_AEECSS(1,"数据接入"),
    DATA_MODEL(2,"数据建模");

    ModuleTypeEnum(int value, String name) {
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
