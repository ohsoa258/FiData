package com.fisk.datafactory.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author Lock
 */
public enum ChannelDataEnum implements BaseEnum {

    /**
     * 开始
     */
    SCHEDULE_TASK(1,"开始"),
    /**
     * 任务组
     */
    TASKGROUP(2,"任务组"),
    /**
     * 数据湖表任务
     */
    DATALAKE_TASK(3,"数据湖表任务"),
    /**
     * 数仓维度表任务组
     */
    DW_DIMENSION_TASK(4,"数仓维度表任务"),
    /**
     * 数仓事实表任务组
     */
    DW_FACT_TASK(5,"数仓事实表任务"),
    /**
     * 分析模型维度表任务组
     */
    OLAP_DIMENSION_TASK(6,"分析模型维度表任务"),
    /**
     * 分析模型事实表任务组
     */
    OLAP_FACT_TASK(7,"分析模型事实表任务");

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
            String queryName=carTypeEnum.name;
            if (queryName.equals(name)) {
                return carTypeEnum;
            }
        }
        return null;
    }

}
