package com.fisk.task.enums;

import com.fisk.common.enums.BaseEnum;

public enum DataClassifyEnum  implements BaseEnum {
    /**
     * 数据接入,数据建模Datamodeling  DataAccess
     */
    DATAMODELING(0,"数据建模--维度事实"),
    DATAMODELKPL(2,"数据建模--指标"),
    DATAACCESS(1,"数据接入");

    private final String name;
    private final int value;

    DataClassifyEnum( int value,String name) {
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
