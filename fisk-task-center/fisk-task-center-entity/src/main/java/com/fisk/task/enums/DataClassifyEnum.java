package com.fisk.task.enums;

import com.fisk.common.core.enums.BaseEnum;

public enum DataClassifyEnum  implements BaseEnum {
    /**
     * 数据接入,数据建模Datamodeling  DataAccess  dimension
     */
    DATAMODELING(0,"数据建模-维度事实"),
    DATAACCESS(1,"数据接入"),
    DATAACCESS_API(11,"数据接入-非实时api"),
    DATAMODELKPL(2,"数据建模-指标"),
    DATAMODELWIDETABLE(9,"数据建模-宽表"),
    UNIFIEDCONTROL(10,"统一调度"),
    //管道服务
    CUSTOMWORKDATAMODELING(3,"管道服务-数据建模-维度事实"),
    CUSTOMWORKDATAMODELDIMENSIONKPL(4,"管道服务-数据建模-维度指标"),
    CUSTOMWORKDATAMODELFACTKPL(8,"管道服务-数据建模-事实指标"),
    CUSTOMWORKDATAACCESS(5,"管道服务-数据接入"),
    CUSTOMWORKSTRUCTURE(6,"结构层级"),
    //scheduling component
    CUSTOMWORKSCHEDULINGCOMPONENT(7,"管道服务-调度组件"),
    QUALITYREPORT(12, "质量报告");

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
