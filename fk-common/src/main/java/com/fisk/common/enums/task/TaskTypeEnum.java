package com.fisk.common.enums.task;

import com.fisk.common.enums.BaseEnum;

/**
 * @author gy
 */

public enum TaskTypeEnum implements BaseEnum {

    /**
     * 后台任务
     */
    BUILD_NIFI_FLOW(0, "数据流创建"),
    /**
     * 构建atlas
     */
    BUILD_ATLAS_TASK(1, "元数据构建"),
    /**
     * doris create stg&ods table
     */
    BUILD_DORIS_TASK(2,"Doris生成"),
    /**
     * doris create stg&ods table
     */
    BUILD_DATAMODEL_DORIS_TABLE(3,"Doris创建表"),
    /**
     * doris incremental update
     */
    BUILD_DORIS_INCREMENTAL_UPDATE_TASK(4,"Doris数据增量更新"),
    /**
     * atlas 删除实体
     */
    BUILD_ATLAS_ENTITYDELETE_TASK(5,"元数据删除"),
    /**
     * 数据接入 创建pg table
     */
    BUILD_DATAINPUT_PGSQL_TABLE_TASK(6,"数据接入创建pg table"),
    /**
     * 数据接入 stg to ods
     */
    BUILD_DATAINPUT_PGSQL_STGTOODS_TASK(7,"数据接入 STG TO ODS"),
    /**
     * 数据接入 删除表
     */
    BUILD_DATAINPUT_DELETE_PGSQL_STGTOODS_TASK(8,"PGSQL删除STG和ODS数据表"),
    /**
     * OLAP创建模型
     */
    BUILD_CREATEMODEL_TASK(9,"创建模型"),
    /*
    * nifi管道
    * */
    BUILD_CUSTOMWORK_TASK(10,"创建nifi管道");

    TaskTypeEnum(int value, String name) {
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
