package com.fisk.common.core.enums.datamanage;

import com.fisk.common.core.enums.BaseEnum;

/**
 * 数据操作类型
 * @author JinXingWang
 */
public enum DataOperationTypeEnum implements BaseEnum {

    ADD(1,"添加"),
    UPDATE(2,"修改"),
    DELETE(3,"删除");

    private final String name;
    private final int value;

    DataOperationTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
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
