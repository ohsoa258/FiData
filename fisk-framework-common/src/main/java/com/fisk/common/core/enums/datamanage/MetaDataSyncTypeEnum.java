package com.fisk.common.core.enums.datamanage;

import com.fisk.common.core.enums.BaseEnum;

public enum MetaDataSyncTypeEnum implements BaseEnum {

    DATA_INPUT(1,"数据接入"),
    DW(2,"数仓建模"),
    MASTER_DATA(3,"主数据"),
    DATA_CONSUME_API(4,"数据消费API"),
    DATA_CONSUME_VIEW(5,"数据消费视图"),
    DATA_CONSUME_DATABASE_SYNC(6,"数据消费数据库同步服务");

    private final String name;
    private final int value;

    MetaDataSyncTypeEnum(int value, String name) {
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
