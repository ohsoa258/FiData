package com.fisk.common.core.enums.metadataentitylog;

import com.fisk.common.core.enums.BaseEnum;

public enum MetaDataeLogEnum implements BaseEnum {
    INSERT_OPERATION(1,"新增"),
    UPDATE_OPERATION(2,"修改"),
    DELETE_OPERATION(3,"删除"),
    SELECT_OPERATION(4,"查询")
    ;

    private final String name;
    private final int value;
    MetaDataeLogEnum(int value, String name) {
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
