package com.fisk.datamanagement.enums;

import com.fisk.common.core.enums.BaseEnum;

public enum MetaClassificationTypeEnum implements BaseEnum {
    OTHER(0,"未知","未知"),
    DATA_SOURCE(-1,"外部数据源","外部数据源"),
    DATA_FACTORY(-2,"数据工厂","数据工厂,数仓建模 MDM模型"),
    API_GATEWAY(-3,"API网关服务","通过API，向下游消费数据"),
    VIEW_ANALYZE_SERVICE(-4,"数据分析试图服务","提供数据查询视图"),
    DATABASE_SYNCHRONIZATION_SERVICE(-5,"数据库同步服务","通过数据库配置，实现数据交互到下游数据库");
    private  final  int value;
    private  final  String name;
    private  final String  description;
    MetaClassificationTypeEnum(int value,String name,String description) {
        this.value = value;
        this.name = name;
        this.description = description;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
    public String getDescription () {
        return description;
    }
    }
