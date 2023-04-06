package com.fisk.datamanagement.enums;

import com.fisk.common.core.enums.BaseEnum;

public enum ClassificationTypeEnum implements BaseEnum {
    DATA_ACCESS(1,"数据接入"),
    ANALYZE_DATA(2,"分析数据"),
    API_GATEWAY_SERVICE(3,"API网关服务"),
    DATABASE_SYNCHRONIZATION_SERVICE(4,"数据库同步服务"),
    VIEW_ANALYZE_SERVICE(5,"数据分析试图服务"),
    ;
    private  final  int value;
    private  final  String name;
    ClassificationTypeEnum(int value,String name){
        this.value=value;
        this.name=name;
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
