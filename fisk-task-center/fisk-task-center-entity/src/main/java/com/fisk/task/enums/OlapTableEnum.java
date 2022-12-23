package com.fisk.task.enums;

import com.fisk.common.core.enums.BaseEnum;

public enum OlapTableEnum implements BaseEnum {
    /**
     * 任务状态
     */
    KPI(0, "指标表"),
    DIMENSION(1, "维度表"),
    /*
     * 物理表,事实表physics
     * */
    FACT(2, "事实表"),
    PHYSICS(3, "物理表"),
    PHYSICS_API(10, "非实时api"),
    PHYSICS_RESTAPI(11, "实时api"),
    WIDETABLE(9, "宽表"),
    //CustomWork管道服务
    /**
     * 任务状态
     */
    CUSTOMWORKDIMENSIONKPI(4, "管道服务-维度指标表"),
    CUSTOMWORKFACTKPI(8, "管道服务-事实指标表"),
    CUSTOMWORKDIMENSION(5, "管道服务-维度表"),
    /*
     * 物理表,事实表physics
     * */
    CUSTOMWORKFACT(6, "管道服务-事实表"),
    CUSTOMWORKPHYSICS(7, "管道服务-物理表"),
    GOVERNANCE(12, "数据质量"),
    CUSTOMIZESCRIPT(13,"自定义脚本任务"),
    SFTPFILECOPYTASK(14,"SFTP文件复制"),
    DATASERVICES(15,"数据服务表")
    ;


    OlapTableEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static OlapTableEnum getNameByValue(int value) {
        switch (value) {
            /*
             * 表类别
             * */
            case 0:
                return KPI;
            case 1:
                return DIMENSION;
            case 2:
                return FACT;
            case 3:
                return PHYSICS;
            case 4:
                return CUSTOMWORKDIMENSION;
            case 5:
                return CUSTOMWORKDIMENSION;
            case 6:
                return CUSTOMWORKFACT;
            case 7:
                return CUSTOMWORKPHYSICS;
            case 8:
                return CUSTOMWORKFACTKPI;
            case 9:
                return WIDETABLE;
            case 10:
                return PHYSICS_API;
            case 11:
                return PHYSICS_RESTAPI;
            case 12:
                return GOVERNANCE;
            case 13:
                return CUSTOMIZESCRIPT;
            case 14:
                return SFTPFILECOPYTASK;
            default:
                return null;
        }
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
