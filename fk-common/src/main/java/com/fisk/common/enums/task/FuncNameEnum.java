package com.fisk.common.enums.task;

import com.fisk.common.enums.BaseEnum;

public enum FuncNameEnum implements BaseEnum {
    PG_DATA_STG_TO_ODS_TOTAL(1,"pg_data_stg_to_ods_total"),
    PG_DATA_STG_TO_ODS_DELETE(2,"pg_data_stg_to_ods_delete");

    private final String name;
    private final int value;

    FuncNameEnum(int value, String name) {
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
