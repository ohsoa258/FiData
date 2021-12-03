package com.fisk.task.enums;

import com.fisk.common.enums.BaseEnum;

public enum OlapTableEnum implements BaseEnum {
    /**
     * 任务状态
     */
    KPI(0,"指标表"),
    DIMENSION(1,"维度表"),
    /*
    * 物理表,事实表physics
    * */
    FACT(2,"事实表"),
    PHYSICS(3,"物理表"),
    //CustomWork管道服务
    /**
     * 任务状态
     */
    CUSTOMWORKDIMENSIONKPI(4,"管道服务-维度指标表"),
    CUSTOMWORKFACTKPI(8,"管道服务-事实指标表"),
    CUSTOMWORKDIMENSION(5,"管道服务-维度表"),
    /*
     * 物理表,事实表physics
     * */
    CUSTOMWORKFACT(6,"管道服务-事实表"),
    CUSTOMWORKPHYSICS(7,"管道服务-物理表");


    OlapTableEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    private final String name;
    private final int value;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}
