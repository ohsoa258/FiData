package com.fisk.dataaccess.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author Lock
 */
public enum ComponentIdTypeEnum implements BaseEnum {
    /**
     * 查询类型
     */
    CFG_DB_POOL_COMPONENT_ID(0,"cfgDbPoolComponentId"),

    DRAFT(1,"草稿");


    ComponentIdTypeEnum(int value, String name) {
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
