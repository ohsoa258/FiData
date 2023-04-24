package com.fisk.datamanagement.enums;

import com.fisk.common.core.enums.BaseEnum;

public enum ClassificationTypeEnum implements BaseEnum {
    DATA_ACCESS(1,"数据接入","数据贴源，对接上游系统数据"),
    ANALYZE_DATA(2,"数仓建模","数仓建模，数据仓库数据"),
    API_GATEWAY_SERVICE(3,"API网关服务","通过API，向下游消费数据"),
    DATABASE_SYNCHRONIZATION_SERVICE(4,"数据库同步服务","通过数据库配置，实现数据交互到下游数据库"),
    VIEW_ANALYZE_SERVICE(5,"数据分析试图服务","提供数据查询视图"),
    ;
    private  final  int value;
    private  final  String name;
    private  final String  description;
    ClassificationTypeEnum(int value,String name,String description) {
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

