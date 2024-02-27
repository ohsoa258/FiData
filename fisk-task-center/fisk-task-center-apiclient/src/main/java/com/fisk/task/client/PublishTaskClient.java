package com.fisk.task.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.service.accessAndModel.AccessAndModelTableDTO;
import com.fisk.common.service.accessAndModel.LogPageQueryDTO;
import com.fisk.common.service.accessAndModel.NifiLogResultDTO;
import com.fisk.common.service.accessAndTask.DataTranDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.dataaccess.DataAccessIdDTO;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import com.fisk.datamodel.dto.businessarea.BusinessAreaGetDataDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishDataDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigTaskDTO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.dataservice.dto.tableapi.TableApiServiceDTO;
import com.fisk.dataservice.dto.tableapi.TableApiTaskDTO;
import com.fisk.dataservice.dto.tableservice.TableServiceDTO;
import com.fisk.mdm.dto.accessmodel.AccessPublishDataDTO;
import com.fisk.system.dto.datasource.DataSourceSaveDTO;
import com.fisk.task.dto.AccessDataSuccessAndFailCountDTO;
import com.fisk.task.dto.DwLogQueryDTO;
import com.fisk.task.dto.DwLogResultDTO;
import com.fisk.task.dto.WsAccessDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.daconfig.OverLoadCodeDTO;
import com.fisk.task.dto.dispatchlog.*;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.metadatafield.MetaDataFieldDTO;
import com.fisk.task.dto.model.EntityDTO;
import com.fisk.task.dto.model.ModelDTO;
import com.fisk.task.dto.model.TableDTO;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.dto.pipeline.NifiStageDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import com.fisk.task.dto.query.DataServiceTableLogQueryDTO;
import com.fisk.task.dto.task.*;
import com.fisk.task.po.TableNifiSettingPO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 发送任务
 *
 * @author gy
 */
@FeignClient("task-center")
public interface PublishTaskClient {

    /**
     * 发送任务创建消息
     *
     * @param data dto
     * @return 发送结果
     */
    @PostMapping("/publishTask/nifiFlow")
    ResultEntity<Object> publishBuildNifiFlowTask(@RequestBody BuildNifiFlowDTO data);

    @PostMapping("/publishTask/publishBuildDataServices")
    ResultEntity<Object> publishBuildDataServices(@RequestBody BuildTableServiceDTO data);

    @PostMapping("/publishTask/publishBuildDataServiceApi")
    ResultEntity<Object> publishBuildDataServiceApi(@RequestBody BuildTableApiServiceDTO data);

    /**
     * 元数据实例&DB构建
     *
     * @param ArDto dto
     * @return 构建结果
     */
    @PostMapping("/publishTask/atlasBuildInstance")
    ResultEntity<Object> publishBuildAtlasInstanceTask(@RequestBody AtlasEntityQueryDTO ArDto);

    /**
     * 元数据Table&Column构建
     *
     * @param ArDto dto
     * @return 构建结果
     */

    @PostMapping("/publishTask/atlasBuildTableAndColumn")
    ResultEntity<Object> publishBuildAtlasTableTask(@RequestBody BuildPhysicalTableDTO ArDto);

    /**
     * 创建物理表
     *
     * @param ArDto
     * @return
     */
    @PostMapping("/publishTask/publishBuildPhysicsTableTask")
    ResultEntity<Object> publishBuildPhysicsTableTask(@RequestBody BuildPhysicalTableDTO ArDto);

    /**
     * 元数据删除
     *
     * @param entityId
     * @return
     */
    @PostMapping("/publishTask/atlasEntityDelete")
    ResultEntity<Object> publishBuildAtlasEntityDeleteTask(@RequestBody String entityId);

    /**
     * doris创建表BUILD_DATAMODEL_DORIS_TABLE
     *
     * @param modelPublishDataDTO
     * @return
     */
    @PostMapping("/publishTask/atlasDorisTable")
    ResultEntity<Object> publishBuildAtlasDorisTableTask(@RequestBody ModelPublishDataDTO modelPublishDataDTO);

    /**
     * 数仓建模-建doris聚合模型表
     *
     * @param modelPublishDataDTO
     * @return
     */
    @PostMapping("/publishTask/publishBuildDorisAggregateTbl")
    ResultEntity<Object> publishBuildDorisAggregateTbl(@RequestBody ModelPublishDataDTO modelPublishDataDTO);


    /**
     * mdmETL发布
     *
     * @param accessPublishDataDTO
     * @return
     */
    @PostMapping("/publishTask/mdmTableTask")
    ResultEntity<Object> publishBuildMdmTableTask(@RequestBody AccessPublishDataDTO accessPublishDataDTO);


    /**
     * 建模
     *
     * @param buildCreateModelTaskDto
     * @return
     */
    @PostMapping("/olapTask/CreateModel")
    ResultEntity<Object> publishOlapCreateModel(@RequestBody BusinessAreaGetDataDTO buildCreateModelTaskDto);

    @PostMapping("/olapTask/selectByName")
    ResultEntity<Object> selectByName(@RequestParam("tableName") String tableName);

    /**
     * pgsql 删除表
     *
     * @param delTable
     * @return
     */
    @PostMapping("/publishTask/deletePgsqlTable")
    public ResultEntity<Object> publishBuildDeletePgsqlTableTask(@RequestBody PgsqlDelTableDTO delTable);

    /**
     * 修改调度
     *
     * @param groupId
     * @param ProcessorId
     * @param schedulingStrategy
     * @param schedulingPeriod
     * @return
     */
    @PostMapping("/nifi/modifyScheduling")
    public ResultEntity<Object> modifyScheduling(@RequestParam("groupId") String groupId, @RequestParam("ProcessorId") String ProcessorId, @RequestParam("schedulingStrategy") String schedulingStrategy, @RequestParam("schedulingPeriod") String schedulingPeriod);


    /**
     * 删除nifi流程
     *
     * @param dataModelVO
     * @return
     */
    @PostMapping("/nifi/deleteNifiFlow")
    public ResultEntity<Object> deleteNifiFlow(@RequestBody DataModelVO dataModelVO);

    /**
     * 删除nifi流程
     *
     * @param dataModelVO
     * @return
     */
    @PostMapping("/publishTask/deleteNifiFlowByKafka")
    ResultEntity<Object> deleteNifiFlowByKafka(@RequestBody DataModelVO dataModelVO);


    /**
     * getTableNifiSetting
     *
     * @param dto
     * @return TableNifiSettingPO
     */
    @PostMapping("/nifi/getTableNifiSetting")
    public ResultEntity<TableNifiSettingPO> getTableNifiSetting(@RequestBody DataAccessIdDTO dto);

    /**
     * publishBuildNifiCustomWorkFlowTask
     *
     * @param nifiCustomWorkListDTO
     * @return
     */
    @PostMapping("/publishTask/NifiCustomWorkFlow")
    public ResultEntity<Object> publishBuildNifiCustomWorkFlowTask(@RequestBody NifiCustomWorkListDTO nifiCustomWorkListDTO);

    /**
     * deleteTableTopicByComponentId
     *
     * @param ids
     * @return
     */
    @PostMapping("/TableTopic/deleteTableTopicByComponentId")
    public ResultEntity<Object> deleteTableTopicByComponentId(@RequestParam("ids") List<Integer> ids);

    /**
     * deleteTableTopicGroup
     *
     * @param dtos
     * @return
     */
    @PostMapping("/TableTopic/deleteTableTopicGroup")
    ResultEntity<Object> deleteTableTopicGroup(@RequestParam("dtos") List<TableTopicDTO> dtos);

    /**
     * 拼接sql替换时间
     *
     * @return 返回值
     */
    @PostMapping("/TBETLIncremental/converSql")
    ResultEntity<Map<String, String>> converSql(@RequestBody DataTranDTO dto);

    /**
     * getSqlForPgOds
     *
     * @param configDTO configDTO
     * @return 返回值
     */
    @PostMapping("/nifi/getSqlForPgOds")
    ResultEntity<List<String>> getSqlForPgOds(@RequestBody DataAccessConfigDTO configDTO);

    /**
     * getSqlForPgOds
     *
     * @param configDTO configDTO
     * @return 返回值
     */
    @PostMapping("/nifi/getSqlForPgOdsV2")
    ResultEntity<List<String>> getSqlForPgOdsV2(@RequestBody DataAccessConfigDTO configDTO);

    /**
     * 获取管道内每张表的状态
     *
     * @param nifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO
     * @return 返回值
     */
    @PostMapping("/pipeline/getPipelineTableLogs")
    ResultEntity<List<PipelineTableLogDTO>> getPipelineTableLogs(@RequestBody List<NifiCustomWorkflowDetailDTO> nifiCustomWorkflowDetailDTO);

    /**
     * 获取管道呼吸灯
     *
     * @param nifiCustomWorkflows nifiCustomWorkflows
     * @return 返回值
     */
    @PostMapping("/pipeline/getNifiCustomWorkflowDetails")
    ResultEntity<List<NifiCustomWorkflowVO>> getNifiCustomWorkflowDetails(@RequestBody List<NifiCustomWorkflowVO> nifiCustomWorkflows);

    /**
     * 获取nifi阶段信息
     *
     * @param list list
     * @return 返回值
     */
    @PostMapping("/pipeline/getNifiStage")
    ResultEntity<List<NifiStageDTO>> getNifiStage(@RequestBody List<NifiCustomWorkflowDetailDTO> list);

    /**
     * 创建宽表
     *
     * @param wideTableFieldConfigTaskDTO wideTableFieldConfigTaskDTO
     * @return 返回值
     */
    @PostMapping("/olapTask/publishBuildWideTableTask")
    ResultEntity<Object> publishBuildWideTableTask(@RequestBody WideTableFieldConfigTaskDTO wideTableFieldConfigTaskDTO);

    /**
     * 立即重启
     *
     * @param buildTableNifiSettingDTO
     * @return
     */
    @PostMapping("/publishTask/immediatelyStart")
    ResultEntity<Object> immediatelyStart(@RequestBody BuildTableNifiSettingDTO buildTableNifiSettingDTO);

    /**
     * 统一调度
     *
     * @param unifiedControlDTO unifiedControlDTO
     * @return
     */
    @PostMapping("/olapTask/publishBuildunifiedControlTask")
    ResultEntity<Object> publishBuildunifiedControlTask(@RequestBody UnifiedControlDTO unifiedControlDTO);

    /**
     * 创建属性日志表
     *
     * @param data
     * @return
     */
    @PostMapping("/publishTask/pushModelByName")
    public ResultEntity<Object> pushModelByName(@RequestBody ModelDTO data);

    /**
     * 创建任务后台表
     *
     * @param data
     * @return
     */
    @PostMapping("/publishTask/createBackendTable")
    public ResultEntity<Object> createBackendTable(@RequestBody EntityDTO data);

    /**
     * consumer
     *
     * @return
     */
    @PostMapping("/pipeline/consumer")
    public void consumer(@RequestParam String message);

    /**
     * updateTableTopicByComponentId
     *
     * @return
     */
    @PostMapping("/pipeline/updateTableTopicByComponentId")
    public void updateTableTopicByComponentId(@RequestBody TableTopicDTO tableTopicDTO);

    /**
     * saveNifiStage
     *
     * @return
     */
    @PostMapping("/pipeline/saveNifiStage")
    void saveNifiStage(@RequestParam String data);

    /**
     * 删除nifi管道
     *
     * @return
     */
    @PostMapping("/nifi/deleteCustomWorkNifiFlow")
    void deleteCustomWorkNifiFlow(@RequestBody NifiCustomWorkListDTO nifiCustomWorkListDTO);

    /**
     * 接入日志完善
     *
     * @return
     */
    @PostMapping("/pipeline/getPipelineTableLog")
    ResultEntity<List<PipelineTableLogVO>> getPipelineTableLog(@RequestParam("data") String data, @RequestParam("pipelineTableQuery") String pipelineTableQuery);

    /**
     * 管道job日志
     *
     * @return
     */
    @PostMapping("/dispatchLog/getPipelJobLogVos")
    ResultEntity<List<PipelJobLogVO>> getPipelJobLogVos(@RequestBody List<PipelJobLogVO> pipelJobLogs);

    /**
     * 任务日志
     *
     * @return
     */
    @PostMapping("/dispatchLog/getPipelTaskLogVos")
    ResultEntity<List<PipelTaskLogVO>> getPipelTaskLogVos(@RequestBody List<PipelTaskLogVO> pipelTaskLogs);

    /**
     * 根据taskTraceId获取任务日志
     *
     * @param taskTraceId
     * @return
     */
    @GetMapping("/dispatchLog/getPipelTaskLogVo")
    ResultEntity<List<PipelTaskLogVO>> getPipelTaskLogVo(@RequestParam String taskTraceId);

    /**
     * 阶段日志
     *
     * @return
     */
    @PostMapping("/dispatchLog/getPipelStageLogVos")
    ResultEntity<List<PipelStageLogVO>> getPipelStageLogVos(@RequestParam String taskId);

    /**
     * 暂停管道
     *
     * @return
     */
    @PostMapping("/nifi/suspendCustomWorkNifiFlow")
    ResultEntity<Object> suspendCustomWorkNifiFlow(@RequestParam("nifiCustomWorkflowId") String nifiCustomWorkflowId, @RequestParam("ifFire") boolean ifFire);

    /**
     * 元数据实时同步
     *
     * @param dto
     */
    @PostMapping("/publishTask/metaData")
    ResultEntity<Object> metaData(@RequestBody BuildMetaDataDTO dto);

    /**
     * 获取管道日志
     *
     * @param pipelLog pipelLog
     * @return 执行结果
     */
    @PostMapping("/dispatchLog/getPipelLogVos")
    ResultEntity<List<PipelLogVO>> getPipelLogVos(@RequestBody PipelLogVO pipelLog);

    /**
     * 依据pipelTraceId查询pipelId
     *
     * @param pipelTraceId
     * @return
     */
    @GetMapping("/dispatchLog/getPipelIdByPipelTraceId")
    ResultEntity<String> getPipelIdByPipelTraceId(@RequestParam("pipelTraceId") String pipelTraceId);

    /**
     * 获取数据服务表服务同步日志
     *
     * @param dto dto
     * @return 执行结果
     */
    @PostMapping("/dispatchLog/getDataServiceTableLogVos")
    ResultEntity<DataServiceTableLogQueryVO> getDataServiceTableLogVos(@RequestBody DataServiceTableLogQueryDTO dto);

    /**
     * 同步数据源添加nifi变量
     *
     * @param dto
     * @return
     */
    @PostMapping("/nifi/add")
    ResultEntity<Object> addDataSetParams(@RequestBody DataSourceSaveDTO dto);

    /**
     * 同步数据源更新nifi变量
     *
     * @param dto
     * @return
     */
    @PutMapping("/nifi/edit")
    ResultEntity<Object> editDataSetParams(@RequestBody DataSourceSaveDTO dto);

    @PostMapping("/pipeline/overlayCodePreview")
    ResultEntity<Object> overlayCodePreview(@RequestBody OverLoadCodeDTO dto);

    @DeleteMapping("/publishTask/fieldDelete")
    ResultEntity<Object> fieldDelete(@RequestBody MetaDataFieldDTO fieldDTO);


    /**
     * 依据pipelTraceId查询pipelId
     *
     * @param pipelTraceId
     * @return
     */
    @GetMapping("/dispatchLog/getPipelStates")
    ResultEntity<List<String>> getPipelStates(@RequestParam("pipelTraceId") String pipelTraceId);

    @PostMapping("/publishTask/deleteAccessMdmNifiFlow")
    ResultEntity<Object> publishDeleteAccessMdmNifiFlowTask(@RequestBody BuildDeleteTableServiceDTO data);

    @PostMapping("/publishTask/deleteBackendTable")
    ResultEntity<Object> deleteBackendTable(@RequestBody TableDTO data);
    /**
     * @param data
     * @return
     */
    @PostMapping("/publishTask/publishBuildDeleteDataServices")
    ResultEntity<Object> publishBuildDeleteDataServices(@RequestBody BuildDeleteTableServiceDTO data);

    /**
     * @param data
     * @return
     */
    @PostMapping("/publishTask/publishBuildDeleteDataServiceApi")
    ResultEntity<Object> publishBuildDeleteDataServiceApi(@RequestBody BuildDeleteTableApiServiceDTO data);

    /**
     * task.build.task.over
     *
     * @param dto
     */
    @PostMapping("/publishTask/missionEndCenter")
    ResultEntity<Object> missionEndCenter(@RequestBody KafkaReceiveDTO dto);


    /**
     * 数据库同步服务，手动同步按钮
     *
     * @param dto
     * @return
     */
    @PostMapping("/publishTask/universalPublish")
    ResultEntity<Object> universalPublish(@RequestBody KafkaReceiveDTO dto);

    /**
     * 数据库同步服务，启用或禁用
     *
     * @param tableServiceDTO
     * @return
     */
    @PostMapping("/nifi/enableOrDisable")
    public ResultEntity<TableServiceDTO> enableOrDisable(@RequestBody TableServiceDTO tableServiceDTO);

    /**
     * 数据分发api同步服务，启用或禁用
     *
     * @param tableServiceDTO
     * @return
     */
    @PostMapping("/nifi/apiEnableOrDisable")
    ResultEntity<TableApiServiceDTO> apiEnableOrDisable(@RequestBody TableApiServiceDTO tableServiceDTO);

    /**
     * 创建任务批量审批
     *
     * @param dto
     * @return
     */
    @PostMapping("/publishTask/createBatchApproval")
    public ResultEntity<Object> createBatchApproval(@RequestBody BuildBatchApprovalDTO dto);

    /**
     * 执行一次管道
     *
     * @param id
     * @return
     */
    @GetMapping("/nifi/runOnce")
    public ResultEntity<Object> runOnce(@RequestParam("id") Long id);

    /**
     * 数据接入--首页展示信息--当日接入数据总量
     *
     * @return
     */
    @ApiOperation("数据接入--首页展示信息--当日接入数据总量")
    @GetMapping("/pipeline/accessDataTotalCount")
    ResultEntity<Long> accessDataTotalCount();

    /**
     * 数据接入--首页展示信息--当日接入数据的成功次数和失败次数
     *
     * @return
     */
    @ApiOperation("数据接入--首页展示信息--当日接入数据的成功次数和失败次数")
    @GetMapping("/pipeline/accessDataSuccessAndFailCount")
    ResultEntity<AccessDataSuccessAndFailCountDTO> accessDataSuccessAndFailCount();

    @PostMapping("/publishTask/savePipelTaskLog")
    ResultEntity<Object> savePipelTaskLog(@RequestBody TableApiTaskDTO data);

    /**
     * 前置机-数据接入发送消息到数据分发
     *
     * @param dto
     */
    @PostMapping("/ws/wsAccessToConsume")
    void wsAccessToConsume(@RequestBody WsAccessDTO dto);

    /**
     * 异步触发kafka消息队列
     *
     * @param kafkaReceive
     * @return
     */
    @PostMapping("/publishTask/syncKafka")
    ResultEntity<Object> syncKafka(@RequestBody KafkaReceiveDTO kafkaReceive);

    /**
     * dw数仓按时间获取单表nifi日志
     *
     * @param dwLogQueryDTO
     * @return
     */
    @ApiOperation("dw数仓按时间获取单表nifi日志")
    @PostMapping("/nifi/getDwTblNifiLog")
    DwLogResultDTO getDwTblNifiLog(@RequestBody DwLogQueryDTO dwLogQueryDTO);

    /**
     * 同步日志页面获取数接/数仓的指定表的nifi同步日志  根据表id 名称 类型
     *
     * @param dto
     * @return
     */
    @ApiOperation("同步日志页面获取数接/数仓的指定表的nifi同步日志  根据表id 名称 类型")
    @PostMapping("/nifi/getDwAndAccessTblNifiLog")
    Page<NifiLogResultDTO> getDwAndAccessTblNifiLog(@RequestBody LogPageQueryDTO dto);

}
