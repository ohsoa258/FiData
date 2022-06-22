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
import com.fisk.task.dto.model.EntityDTO;
import com.fisk.task.dto.model.ModelDTO;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.dto.task.BuildTableNifiSettingDTO;
import com.fisk.task.dto.task.NifiCustomWorkListDTO;
import com.fisk.task.listener.atlas.BuildAtlasTableAndColumnTaskListener;
import com.fisk.task.listener.doris.BuildDataModelDorisTableListener;
import com.fisk.task.service.task.IBuildKfkTaskService;
import com.fisk.task.service.task.IBuildTaskService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
        return iBuildKfkTaskService.publishTask("创建表:"+data.tableName+"的数据流任务",
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_NIFI_FLOW,
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
                MqConstants.QueueConstants.BUILD_DORIS_FLOW,
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
                MqConstants.QueueConstants.BUILD_ATLAS_INSTANCE_FLOW,
                ArDto);
    }

    /**
     * 在Atlas中生成数据库、表、字段的血缘关系
     * @param ArDto
     * @return
     */
    @PostMapping("/atlasBuildTableAndColumn")
    @ApiOperation(value = "在Atlas中生成数据库、表、字段的血缘关系")
    public ResultEntity<Object> publishBuildAtlasTableTask(@RequestBody BuildPhysicalTableDTO ArDto) {
        log.info("进入方法");
        ResultEntity<Object> resultEntity = new ResultEntity<Object>();
        resultEntity.code=0;
        resultEntity.msg="流程创建成功";
        buildAtlasTableAndColumnTaskListener.msg(JSON.toJSONString(ArDto),null);
        return resultEntity;

    }


    /**
     * 创建物理表
     * @param ArDto
     * @return
     */
    @PostMapping("/publishBuildPhysicsTableTask")
    @ApiOperation(value = "在ods库中生成数据表")
    public ResultEntity<Object> publishBuildPhysicsTableTask(@RequestBody BuildPhysicalTableDTO ArDto) {
        return iBuildKfkTaskService.publishTask("数据湖表:"+ArDto.tableName+",结构处理成功",
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_DATAINPUT_PGSQL_TABLE_FLOW,
                ArDto);
    }


    /**
     *pgsql stg to ods
     * @param entityId
     * @return
     */
    @PostMapping("/pgsqlStgOdsIncrementalUpdate")
    @ApiOperation(value = "数据接入 STG TO ODS")
    public ResultEntity<Object> publishBuildPGSqlStgToOdsTask(@RequestBody AtlasEntityDeleteDTO entityId) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_DATAINPUT_PGSQL_STGTOODS_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_DATAINPUT_PGSQL_STGTOODS_FLOW,
                entityId);
    };

    /**
     * Doris 增量更新
     * @param entityId
     * @return
     */
    @PostMapping("/dorisIncrementalUpdate")
    @ApiOperation(value = "Doris 增量更新")
    public ResultEntity<Object> publishBuildDorisIncrementalUpdateTask(@RequestBody AtlasEntityDeleteDTO entityId) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_DORIS_INCREMENTAL_UPDATE_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_DORIS_INCREMENTAL_FLOW,
                entityId);
    }

    /**
     * Atlas 删除实体
     * @param entityId
     * @return
     */
    @PostMapping("/atlasEntityDelete")
    @ApiOperation(value = "Atlas 删除实体")
    public ResultEntity<Object> publishBuildAtlasEntityDeleteTask(@RequestBody AtlasEntityDeleteDTO entityId) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_ATLAS_ENTITYDELETE_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_ATLAS_ENTITYDELETE_FLOW,
                entityId);
    }
    /**
     * pgsql 删除表
     * @param delTable
     * @return
     */
    @PostMapping("/deletePgsqlTable")
    @ApiOperation(value = "pgsql 删除表")
    public ResultEntity<Object> publishBuildDeletePgsqlTableTask(@RequestBody PgsqlDelTableDTO delTable) {
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_DATAINPUT_DELETE_PGSQL_STGTOODS_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_DATAINPUT_DELETE_PGSQL_TABLE_FLOW,
                delTable);
    }

    /**
     * doris创建表BUILD_DORIS_TABLE
     * @param modelPublishDataDTO
     * @return
     */
    @PostMapping("/atlasDorisTable")
    @ApiOperation(value = "dmp_dw创建表")
    public ResultEntity<Object> publishBuildAtlasDorisTableTask(@RequestBody ModelPublishDataDTO modelPublishDataDTO){
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_DATAMODEL_DORIS_TABLE.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_DATAMODEL_DORIS_TABLE,
                modelPublishDataDTO);
    }

    /**
     * 创建管道
     * @param nifiCustomWorkListDTO
     * @return
     */
    @PostMapping("/NifiCustomWorkFlow")
    @ApiOperation(value = "创建管道")
    public ResultEntity<Object> publishBuildNifiCustomWorkFlowTask(@RequestBody NifiCustomWorkListDTO nifiCustomWorkListDTO){
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_CUSTOMWORK_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_CUSTOMWORK_FLOW,
                nifiCustomWorkListDTO);
    }

    /**
     * 立即重启
     * @param buildTableNifiSettingDTO
     * @return
     */
    @PostMapping("/immediatelyStart")
    @ApiOperation(value = "立即启动")
    public ResultEntity<Object> immediatelyStart(@RequestBody BuildTableNifiSettingDTO buildTableNifiSettingDTO){
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_CUSTOMWORK_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_IMMEDIATELYSTART_FLOW,
                buildTableNifiSettingDTO);
    }


    @PostMapping("/pushModelByName")
    @ApiOperation(value = "创建属性日志表")
    public ResultEntity<Object> pushModelByName(@RequestBody ModelDTO dto){
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.CREATE_ATTRIBUTE_TABLE_LOG.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_MDM_MODEL_DATA,
                dto);
    }

    @PostMapping("/createBackendTable")
    @ApiOperation(value = "创建任务后台表")
    public ResultEntity<Object> createBackendTable(@RequestBody EntityDTO dto){
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BACKGROUND_TABLE_TASK_CREATION.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_MDM_ENTITY_DATA,
                dto);
    }

    @PostMapping("/importData")
    @ApiOperation(value = "调度调用第三方api,接收数据,并导入到FiData平台")
    public ResultEntity<Object> importData(@RequestBody ApiImportDataDTO dto){
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_ACCESS_API_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_ACCESS_API_FLOW,
                dto);}

    @PostMapping("/universalPublish")
    @ApiOperation(value = "通用调度,需要传入topic和内容")
    public ResultEntity<Object> universalPublish(@RequestBody KafkaReceiveDTO dto){
        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_UNIVERSAL_PUBLISH_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                pipelineTopicName,
                dto);}



}
