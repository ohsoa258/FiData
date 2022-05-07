package com.fisk.datafactory.enums;

import com.fisk.common.core.enums.BaseEnum;
import com.fisk.task.enums.OlapTableEnum;

/**
 * @author Lock
 */
public enum ChannelDataEnum implements BaseEnum {

    /**
     * 开始
     */
    SCHEDULE_TASK(1, "开始"),
    /**
     * 任务组
     */
    TASKGROUP(2, "任务组"),
    /**
     * 数据湖表任务
     */
    DATALAKE_TASK(3, "数据湖表任务"),

    /**
     * 数仓表任务
     */
    DW_TASK(11, "数仓表任务"),
    /**
     * 数仓维度表任务组
     */
    DW_DIMENSION_TASK(4, "数仓维度表任务"),
    /**
     * 数仓事实表任务组
     */
    DW_FACT_TASK(5, "数仓事实表任务"),


    /**
     * 分析模型任务
     */
    OLAP_TASK(12, "分析模型任务"),
    /**
     * 分析模型维度表任务组
     */
    OLAP_DIMENSION_TASK(6, "分析模型维度表任务"),
    /**
     * 分析模型事实表任务组
     */
    OLAP_FACT_TASK(7, "分析模型事实表任务"),
    /**
     * 分析模型宽表任务
     */
    OLAP_WIDETABLE_TASK(8, "分析模型宽表任务"),
    /**
     * 数据湖ftp任务
     */
    DATALAKE_FTP_TASK(9, "数据湖ftp任务"),
    /**
     * 数据湖非实时api任务
     */
    DATALAKE_API_TASK(10, "数据湖非实时api任务");

    ChannelDataEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    private final int value;
    private final String name;

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    public static ChannelDataEnum getValue(String name) {
        ChannelDataEnum[] carTypeEnums = values();
        for (ChannelDataEnum carTypeEnum : carTypeEnums) {
            String queryName = carTypeEnum.name;
            if (queryName.equals(name)) {
                return carTypeEnum;
            }
        }
        return null;
    }

    public static ChannelDataEnum getName(int value) {
        ChannelDataEnum[] carTypeEnums = values();
        for (ChannelDataEnum carTypeEnum : carTypeEnums) {
            if (carTypeEnum.value == value) {
                return carTypeEnum;
            }
        }
        return null;
    }

    public static OlapTableEnum getOlapTableEnum(Integer value) {
        switch (value) {
            case 3:
                return OlapTableEnum.PHYSICS;
            case 4:
                return OlapTableEnum.DIMENSION;
            case 5:
                return OlapTableEnum.FACT;
            case 6:
                return OlapTableEnum.KPI;
            case 7:
                return OlapTableEnum.KPI;
            default:
                return null;
        }
    }

}
