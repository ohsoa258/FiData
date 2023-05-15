package com.fisk.common.core.enums.task;

import com.fisk.common.core.enums.BaseEnum;

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
    BUILD_DORIS_TASK(2, "Doris生成"),
    /**
     * doris create stg&ods table
     */
    BUILD_DATAMODEL_DORIS_TABLE(3, "dmp_dw创建表"),
    /**
     * doris incremental update
     */
    BUILD_DORIS_INCREMENTAL_UPDATE_TASK(4, "Doris数据增量更新"),
    /**
     * atlas 删除实体
     */
    BUILD_ATLAS_ENTITYDELETE_TASK(5, "元数据删除"),
    /**
     *
     */
    BUILD_ATLAS_FIELDDELETE_TASK(20, "元数据字段删除"),
    /**
     * 数据接入 创建pg table
     */
    BUILD_DATAINPUT_PGSQL_TABLE_TASK(6, "数据接入创建pg table"),
    /**
     * 数据接入 stg to ods
     */
    BUILD_DATAINPUT_PGSQL_STGTOODS_TASK(7, "数据接入 STG TO ODS"),
    /**
     * 数据接入 删除表
     */
    BUILD_DATAINPUT_DELETE_PGSQL_STGTOODS_TASK(8, "PGSQL删除STG和ODS数据表"),
    /**
     * OLAP创建模型
     */
    BUILD_CREATEMODEL_TASK(9, "创建模型"),
    /*
     * nifi管道
     * */
    BUILD_CUSTOMWORK_TASK(10, "创建nifi管道"),
    /*
     * 立即同步
     * */
    BUILD_IMMEDIATELYSTART_TASK(12, "立即同步"),
    /*
     * 创建宽表
     * */
    BUILD_WIDE_TABLE_TASK(11, "创建宽表"),
    /*
     * 统一调度
     * */
    BUILD_TASK_BUILD_NIFI_DISPATCH_FLOW(12, "统一调度"),
    /**
     * 创建属性日志表
     */
    CREATE_ATTRIBUTE_TABLE_LOG(13, "创建属性日志表"),
    /**
     * mdm创建后台表生成任务
     */
    BACKGROUND_TABLE_TASK_CREATION(14, "mdm创建后台表生成任务"),

    BUILD_ACCESS_API_TASK(15, "非实时api同步"),

    BUILD_UNIVERSAL_PUBLISH_TASK(16, "通用调度"),
    /**
     * 元数据实时同步
     */
    BUILD_METADATA_FLOW(17, "元数据实时同步"),

    BUILD_EXEC_SCRIPT_TASK(18, "执行自定义脚本"),

    BUILD_TABLE_SERVER_TASK(19, "表服务同步"),

    BUILD_DELETE_TABLE_SERVER_TASK(20, "删除表服务nifi流程"),

    BUILD_TASK_OVER_TASK(21, "任务结束中心"),

    BATCH_APPROVAL_TASK(22, "流程批量审批"),

    MDM_PUBLISH_TASK(23, "mdmETL发布");

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
