package com.fisk.datamanagement.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author JinXingWang
 */

public enum MetadataAuditOperationTypeEnum implements BaseEnum {
    ALL(0,"全部"),
    ADD(1,"添加"),
    EDIT(2,"编辑"),
    DELETE(3,"删除"),
    ;
    private final int value;
    private final String name;

    MetadataAuditOperationTypeEnum(int value, String name) {
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
