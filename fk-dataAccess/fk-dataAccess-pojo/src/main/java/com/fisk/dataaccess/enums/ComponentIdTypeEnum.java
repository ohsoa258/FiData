package com.fisk.dataaccess.enums;

import com.fisk.common.enums.BaseEnum;

/**
 * @author Lock
 */
public enum ComponentIdTypeEnum implements BaseEnum {
    /**
     * 查询类型,配置库
     */
    CFG_DB_POOL_COMPONENT_ID(0,"cfgDbPoolComponentId"),

    /**
     * pg-ods
     */
    PG_ODS_DB_POOL_COMPONENT_ID(2,"pgOdsDbPoolComponentId"),

    /**
     * pg-dw
     */
    PG_DW_DB_POOL_COMPONENT_ID(3,"pgDwDbPoolComponentId"),

    /**
     * doris-olap
     */
    DORIS_OLAP_DB_POOL_COMPONENT_ID(4,"dorisOlapDbPoolComponentId"),

    /*
    * 日常流程生成组Dailynififlow
    * */
    DAILY_NIFI_FLOW_GROUP_ID(5,"dailyNifiFlowGroupId"),

    /*
    * 管道生成组pipeline
    * */
    PIPELINE_NIFI_FLOW_GROUP_ID(6,"pipelineNifiFlowGroupId"),

    DRAFT(1,"草稿");


    ComponentIdTypeEnum(int value, String name) {
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
