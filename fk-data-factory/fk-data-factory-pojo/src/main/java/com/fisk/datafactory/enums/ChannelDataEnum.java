package com.fisk.datafactory.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author Lock
 */
public enum ChannelDataEnum implements BaseEnum {

    /**
     * 调度任务
     */
    SCHEDULE_TASK(1,"schedule_task"),
    /**
     * 任务组
     */
    TASKGROUP(2,"taskgroup"),
    /**
     * 数据湖表任务
     */
    DATALAKE_TASK(3,"datalake_task"),
    /**
     * 数仓维度表任务组
     */
    DW_DIMENSION_TASK(4,"dw_dimension_task"),
    /**
     * 数仓事实表任务组
     */
    DW_FACT_TASK(5,"dw_fact_task"),
    /**
     * 分析模型维度表任务组
     */
    OLAP_DIMENSION_TASK(6,"olap_dimension_task"),
    /**
     * 分析模型事实表任务组
     */
    OLAP_FACT_TASK(7,"olap_fact_task");

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


}
