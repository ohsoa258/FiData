package com.fisk.task.controller;

import com.fisk.common.constants.MQConstants;
import com.fisk.common.enums.task.TaskTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.task.dto.atlas.AtlasEntityRdbmsDTO;
import com.fisk.task.dto.doris.TableInfoDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.service.IBuildTaskService;
import fk.atlas.api.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author gy
 */
@RestController
@RequestMapping("/publishTask")
@Slf4j
public class PublishTaskController {

    @Resource
    IBuildTaskService service;

    @PostMapping("/nifiFlow")
    public ResultEntity<Object> publishBuildNifiFlowTask(@RequestBody BuildNifiFlowDTO data) {
        return service.publishTask(TaskTypeEnum.BUILD_NIFI_FLOW.getName(),
                MQConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MQConstants.QueueConstants.BUILD_NIFI_FLOW,
                data);
    }

    /** 在Doris中生成stg&ods数据表
     * @param data
     * @param buildType
     * @return
     */
    @PostMapping("/dorisBuild")
    public ResultEntity<Object> publishBuildDorisTask(@RequestBody TableInfoDTO data,String buildType) {
        return service.publishTask(TaskTypeEnum.BUILD_DORIS_TASK.getName(),
                MQConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MQConstants.QueueConstants.BUILD_DORIS_FLOW,
                data);
    }

    /**
     * 在Atlas中生成instance
     * @param instanceData
     * @param dbData
     * @param buildType
     * @return
     */
    @PostMapping("/atlasBuildInstance")
    public ResultEntity<Object> publishBuildAtlasInstanceTask(@RequestBody EnttityRdbmsInstance.entity_rdbms_instance instanceData, EntityRdbmsDB.entity_rdbms_db dbData, EntityProcess.entity_rdbms_process processData, String buildType) {
        AtlasEntityRdbmsDTO eidto=new AtlasEntityRdbmsDTO();
        eidto.entityInstance=instanceData;
        eidto.entityDb=dbData;
        eidto.entityProcess=processData;
        return service.publishTask(TaskTypeEnum.BUILD_ATLAS_TASK.getName(),
                MQConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MQConstants.QueueConstants.BUILD_ATLAS_INSTANCE_FLOW,
                eidto);
    }

    /**
     * 在Atlas中生成数据的血缘关系
     * @param entity_rdbms_table
     * @param entity_rdbms_column
     * @param buildType
     * @return
     */
    @PostMapping("/atlasBuildTableAndColumn")
    public ResultEntity<Object> publishBuildAtlasTableTask(@RequestBody EntityRdbmsTable.entity_rdbms_table entity_rdbms_table, EntityRdbmsColumn.entity_rdbms_column entity_rdbms_column, String buildType) {
        AtlasEntityRdbmsDTO eidto=new AtlasEntityRdbmsDTO();
        eidto.entityTable=entity_rdbms_table;
        eidto.entityTableColumn=entity_rdbms_column;
        return service.publishTask(TaskTypeEnum.BUILD_ATLAS_TASK.getName(),
                MQConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MQConstants.QueueConstants.BUILD_ATLAS_TABLECOLUMN_FLOW,
                eidto);
    }
}
