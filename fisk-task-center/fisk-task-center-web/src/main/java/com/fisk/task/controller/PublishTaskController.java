package com.fisk.task.controller;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.enums.task.TaskTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.datamodel.dto.modelpublish.ModelPublishDataDTO;
import com.fisk.task.dto.atlas.AtlasEntityDeleteDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.dto.daconfig.ApiImportDataDTO;
import com.fisk.task.dto.doris.TableInfoDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.metadatafield.MetaDataFieldDTO;
import com.fisk.task.dto.model.EntityDTO;
import com.fisk.task.dto.model.ModelDTO;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.dto.task.*;
import com.fisk.task.listener.atlas.BuildAtlasTableAndColumnTaskListener;
import com.fisk.task.listener.doris.BuildDataModelDorisTableListener;
import com.fisk.task.service.task.IBuildKfkTaskService;
import com.fisk.task.service.task.IBuildTaskService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author gy
 */
@RestController
@RequestMapping("/publishTask")
@Slf4j
public class PublishTaskController {

    @Resource
    IBuildTaskService service;
    @Resource
    IBuildKfkTaskService iBuildKfkTaskService;
    @Resource
    BuildAtlasTableAndColumnTaskListener buildAtlasTableAndColumnTaskListener;
    @Resource
    BuildDataModelDorisTableListener buildDataModelDorisTableListener;
    @Value("${nifi.pipeline.topicName}")
    public String pipelineTopicName;

    @PostMapping("/nifiFlow")
    @ApiOperation(value = "创建同步数据nifi流程")
    public ResultEntity<Object> publishBuildNifiFlowTask(@RequestBody BuildNifiFlowDTO data) {
        return iBuildKfkTaskService.publishTask("数据湖表:" + data.tableName + "的结构及数据流任务",
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.NifiTopicConstants.BUILD_NIFI_FLOW,
                data);
    }

    @PostMapping("/publishBuildDataServices")
    @ApiOperation(value = "表服务同步")
    public ResultEntity<Object> publishBuildDataServices(@RequestBody BuildTableServiceDTO data) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_TABLE_SERVER_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.DataServiceTopicConstants.BUILD_TABLE_SERVER_FLOW,
                data);
    }

    @PostMapping("/publishBuildDeleteDataServices")
    @ApiOperation(value = "删除表服务nifi流程")
    public ResultEntity<Object> publishBuildDeleteDataServices(@RequestBody BuildDeleteTableServiceDTO data) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_DELETE_TABLE_SERVER_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.DataServiceTopicConstants.BUILD_DELETE_TABLE_SERVER_FLOW,
                data);
    }

    /**
     * 在Doris中生成stg&ods数据表
     *
     * @param data
     * @return
     */
    @PostMapping("/dorisBuild")
    @ApiOperation(value = "在Doris中生成stg&ods数据表")
    public ResultEntity<Object> publishBuildDorisTask(@RequestBody TableInfoDTO data) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_DORIS_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.DorisTopicConstants.BUILD_DORIS_FLOW,
                data);
    }

    /**
     * 在Atlas中生成实例与数据库的血缘关系
     *
     * @param ArDto
     * @return
     */
    @PostMapping("/atlasBuildInstance")
    @ApiOperation(value = "在Atlas中生成实例与数据库的血缘关系")
    public ResultEntity<Object> publishBuildAtlasInstanceTask(@RequestBody AtlasEntityQueryDTO ArDto) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_ATLAS_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.MetaDataTopicConstants.BUILD_ATLAS_INSTANCE_FLOW,
                ArDto);
    }

    /**
     * 在Atlas中生成数据库、表、字段的血缘关系
     *
     * @param ArDto
     * @return
     */
    @PostMapping("/atlasBuildTableAndColumn")
    @ApiOperation(value = "在Atlas中生成数据库、表、字段的血缘关系")
    public ResultEntity<Object> publishBuildAtlasTableTask(@RequestBody BuildPhysicalTableDTO ArDto) {
        log.info("进入方法");
        ResultEntity<Object> resultEntity = new ResultEntity<Object>();
        resultEntity.code = 0;
        resultEntity.msg = "流程创建成功";
        buildAtlasTableAndColumnTaskListener.msg(JSON.toJSONString(ArDto), null);
        return resultEntity;

    }


    /**
     * 创建物理表
     *
     * @param ArDto
     * @return
     */
    @PostMapping("/publishBuildPhysicsTableTask")
    @ApiOperation(value = "在ods库中生成数据表")
    public ResultEntity<Object> publishBuildPhysicsTableTask(@RequestBody BuildPhysicalTableDTO ArDto) {
        log.info("进入建表" + ArDto.tableName);
        return iBuildKfkTaskService.publishTask("数据湖表:" + ArDto.tableName + ",的结构及数据流任务",
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.DataInputTopicConstants.BUILD_DATAINPUT_PGSQL_TABLE_FLOW,
                ArDto);
    }


    /**
     * pgsql stg to ods
     *
     * @param entityId
     * @return
     */
    @PostMapping("/pgsqlStgOdsIncrementalUpdate")
    @ApiOperation(value = "数据接入 STG TO ODS")
    public ResultEntity<Object> publishBuildPGSqlStgToOdsTask(@RequestBody AtlasEntityDeleteDTO entityId) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_DATAINPUT_PGSQL_STGTOODS_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.DataInputTopicConstants.BUILD_DATAINPUT_PGSQL_STGTOODS_FLOW,
                entityId);
    }

    ;

    /**
     * Doris 增量更新
     *
     * @param entityId
     * @return
     */
    @PostMapping("/dorisIncrementalUpdate")
    @ApiOperation(value = "Doris 增量更新")
    public ResultEntity<Object> publishBuildDorisIncrementalUpdateTask(@RequestBody AtlasEntityDeleteDTO entityId) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_DORIS_INCREMENTAL_UPDATE_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.DorisTopicConstants.BUILD_DORIS_INCREMENTAL_FLOW,
                entityId);
    }

    /**
     * Atlas 删除实体
     *
     * @param entityId
     * @return
     */
    @PostMapping("/atlasEntityDelete")
    @ApiOperation(value = "Atlas 删除实体")
    public ResultEntity<Object> publishBuildAtlasEntityDeleteTask(@RequestBody AtlasEntityDeleteDTO entityId) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_ATLAS_ENTITYDELETE_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.MetaDataTopicConstants.BUILD_ATLAS_ENTITYDELETE_FLOW,
                entityId);
    }

    /**
     * pgsql 删除表
     *
     * @param delTable
     * @return
     */
    @PostMapping("/deletePgsqlTable")
    @ApiOperation(value = "pgsql 删除表")
    public ResultEntity<Object> publishBuildDeletePgsqlTableTask(@RequestBody PgsqlDelTableDTO delTable) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_DATAINPUT_DELETE_PGSQL_STGTOODS_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.DataInputTopicConstants.BUILD_DATAINPUT_DELETE_PGSQL_TABLE_FLOW,
                delTable);
    }

    /**
     * doris创建表BUILD_DORIS_TABLE
     *
     * @param modelPublishDataDTO
     * @return
     */
    @PostMapping("/atlasDorisTable")
    @ApiOperation(value = "dmp_dw创建表")
    public ResultEntity<Object> publishBuildAtlasDorisTableTask(@RequestBody ModelPublishDataDTO modelPublishDataDTO) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_DATAMODEL_DORIS_TABLE.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.MdmTopicConstants.BUILD_DATAMODEL_DORIS_TABLE,
                modelPublishDataDTO);
    }

    /**
     * 创建管道
     *
     * @param nifiCustomWorkListDTO
     * @return
     */
    @PostMapping("/NifiCustomWorkFlow")
    @ApiOperation(value = "创建管道")
    public ResultEntity<Object> publishBuildNifiCustomWorkFlowTask(@RequestBody NifiCustomWorkListDTO nifiCustomWorkListDTO) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_CUSTOMWORK_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.DataServiceTopicConstants.BUILD_CUSTOMWORK_FLOW,
                nifiCustomWorkListDTO);
    }

    /**
     * 立即重启
     *
     * @param buildTableNifiSetting
     * @return
     */
    @PostMapping("/immediatelyStart")
    @ApiOperation(value = "立即启动")
    public ResultEntity<Object> immediatelyStart(@RequestBody BuildTableNifiSettingDTO buildTableNifiSetting) {
        return iBuildKfkTaskService.publishTask("【数据库运维】" + buildTableNifiSetting.tableNifiSettings.get(0).tableName + "表数据同步",
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.NifiTopicConstants.BUILD_IMMEDIATELYSTART_FLOW,
                buildTableNifiSetting);
    }


    @PostMapping("/pushModelByName")
    @ApiOperation(value = "创建属性日志表")
    public ResultEntity<Object> pushModelByName(@RequestBody ModelDTO dto) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.CREATE_ATTRIBUTE_TABLE_LOG.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.MdmTopicConstants.BUILD_MDM_MODEL_DATA,
                dto);
    }

    @PostMapping("/createBackendTable")
    @ApiOperation(value = "创建任务后台表")
    public ResultEntity<Object> createBackendTable(@RequestBody EntityDTO dto) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BACKGROUND_TABLE_TASK_CREATION.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.MdmTopicConstants.BUILD_MDM_ENTITY_DATA,
                dto);
    }

    @PostMapping("/importData")
    @ApiOperation(value = "调度调用第三方api,接收数据,并导入到FiData平台")
    public ResultEntity<Object> importData(@RequestBody ApiImportDataDTO dto) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_ACCESS_API_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_ACCESS_API_FLOW,
                dto);
    }

    @PostMapping("/universalPublish")
    @ApiOperation(value = "任务发布中心调度")
    public ResultEntity<Object> universalPublish(@RequestBody KafkaReceiveDTO dto) {
        log.info("任务发布中心调度");
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_UNIVERSAL_PUBLISH_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_TASK_PUBLISH_FLOW,
                dto);
    }

    @PostMapping("/metaData")
    @ApiOperation(value = "元数据实时同步")
    public ResultEntity<Object> metaData(@RequestBody BuildMetaDataDTO dto) {
        log.info("元数据实时同步");
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_METADATA_FLOW.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.MetaDataTopicConstants.BUILD_METADATA_FLOW,
                dto);
    }

    @PostMapping("/BuildExecScript")
    @ApiOperation(value = "执行自定义脚本")
    public ResultEntity<Object> BuildExecScript(@RequestBody ExecScriptDTO dto) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_EXEC_SCRIPT_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_EXEC_SCRIPT_FLOW,
                dto);
    }

    /**
     * task.build.task.over
     *
     * @param dto
     */
    @PostMapping("/missionEndCenter")
    @ApiOperation(value = "任务结束中心")
    public ResultEntity<Object> missionEndCenter(@RequestBody KafkaReceiveDTO dto) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_TASK_OVER_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_TASK_OVER_FLOW,
                dto);
    }

    /**
     * 创建任务批量审批
     *
     * @param dto
     * @return
     */
    @PostMapping("/createBatchApproval")
    public ResultEntity<Object> createBatchApproval(@RequestBody BuildBatchApprovalDTO dto){
        log.info("创建任务批量审批");
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BATCH_APPROVAL_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_MDM_APPROVAL_DATA,
                dto);
    }


    @DeleteMapping("/fieldDelete")
    @ApiOperation(value = "元数据字段删除")
    public ResultEntity<Object> fieldDelete(@RequestBody MetaDataFieldDTO fieldDTO) {
        log.info("元数据字段删除");
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_ATLAS_FIELDDELETE_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.MetaDataTopicConstants.BUILD_METADATA_FIELD_FLOW,
                fieldDTO);
    }




}
