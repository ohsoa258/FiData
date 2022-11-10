package com.fisk.dataaccess.enums;

import com.fisk.common.core.enums.BaseEnum;

/**
 * @author Lock
 */
public enum ComponentIdTypeEnum implements BaseEnum {
    /**
     * 查询类型,配置库
     */
    CFG_DB_POOL_COMPONENT_ID(0,"cfgDbPoolComponentId"),

    CFG_DB_POOL_PASSWORD(7,"CFG_DB_POOL_PASSWORD"),
    CFG_DB_POOL_USERNAME(8,"CFG_DB_POOL_USERNAME"),
    CFG_DB_POOL_URL(9,"CFG_DB_POOL_URL"),


    /**
     * pg-ods
     */
    PG_ODS_DB_POOL_COMPONENT_ID(2,"pgOdsDbPoolComponentId"),

    PG_ODS_DB_POOL_PASSWORD(10,"PG_ODS_DB_POOL_PASSWORD"),
    PG_ODS_DB_POOL_USERNAME(11,"PG_ODS_DB_POOL_USERNAME"),
    PG_ODS_DB_POOL_URL(12,"PG_ODS_DB_POOL_URL"),

    /**
     * pg-dw
     */
    PG_DW_DB_POOL_COMPONENT_ID(3,"pgDwDbPoolComponentId"),

    PG_DW_DB_POOL_PASSWORD(13,"PG_DW_DB_POOL_PASSWORD"),
    PG_DW_DB_POOL_USERNAME(14,"PG_DW_DB_POOL_USERNAME"),
    PG_DW_DB_POOL_URL(15,"PG_DW_DB_POOL_URL"),

    /**
     * doris-olap
     */
    DORIS_OLAP_DB_POOL_COMPONENT_ID(4,"dorisOlapDbPoolComponentId"),

    DORIS_OLAP_DB_POOL_PASSWORD(16,"DORIS_OLAP_DB_POOL_PASSWORD"),
    DORIS_OLAP_DB_POOL_USERNAME(17,"DORIS_OLAP_DB_POOL_USERNAME"),
    DORIS_OLAP_DB_POOL_URL(18,"DORIS_OLAP_DB_POOL_URL"),

    /*
    * 日常流程生成组Dailynififlow
    * */
    DAILY_NIFI_FLOW_GROUP_ID(5,"dailyNifiFlowGroupId"),

    /*
    * 管道生成组pipeline
    * */
    PIPELINE_NIFI_FLOW_GROUP_ID(6,"pipelineNifiFlowGroupId"),

    TRIGGERSCHEDULING_NIFI_FLOW_GROUP_ID(19,"triggerSchedulingNifiFlowGroupId"),

    KAFKA_BROKERS(20,"KAFKA_BROKERS"),

    KEYTAB_CREDENTIALS_SERVICE_ID(21,"KEYTAB_CREDENTIALS_SERVICE_ID"),

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
