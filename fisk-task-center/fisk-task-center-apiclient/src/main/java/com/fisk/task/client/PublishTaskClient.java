package com.fisk.task.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.service.accessAndTask.DataTranDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.dataaccess.DataAccessIdDTO;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import com.fisk.datamodel.dto.businessarea.BusinessAreaGetDataDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishDataDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigTaskDTO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.system.dto.datasource.DataSourceSaveDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.daconfig.OverLoadCodeDTO;
import com.fisk.task.dto.dispatchlog.*;
import com.fisk.task.dto.metadatafield.MetaDataFieldDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.model.EntityDTO;
import com.fisk.task.dto.model.ModelDTO;
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
    public ResultEntity<Object> publishBuildDataServices(@RequestBody BuildTableServiceDTO data);

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
    public ResultEntity<Object> deleteTableTopicGroup(@RequestParam("dtos") List<TableTopicDTO> dtos);

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
     * 获取管道内每张表的状态
     *
     * @param nifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO
     * @return 返回值
     */
    @PostMapping("/pipeline/getPipelineTableLogs")
    public ResultEntity<List<PipelineTableLogDTO>> getPipelineTableLogs(@RequestBody List<NifiCustomWorkflowDetailDTO> nifiCustomWorkflowDetailDTO);

    /**
     * 获取管道呼吸灯
     *
     * @param nifiCustomWorkflows nifiCustomWorkflows
     * @return 返回值
     */
    @PostMapping("/pipeline/getNifiCustomWorkflowDetails")
    public ResultEntity<List<NifiCustomWorkflowVO>> getNifiCustomWorkflowDetails(@RequestBody List<NifiCustomWorkflowVO> nifiCustomWorkflows);

    /**
     * 获取nifi阶段信息
     *
     * @param list list
     * @return 返回值
     */
    @PostMapping("/pipeline/getNifiStage")
    public ResultEntity<List<NifiStageDTO>> getNifiStage(@RequestBody List<NifiCustomWorkflowDetailDTO> list);

    /**
     * 创建宽表
     *
     * @param wideTableFieldConfigTaskDTO wideTableFieldConfigTaskDTO
     * @return 返回值
     */
    @PostMapping("/olapTask/publishBuildWideTableTask")
    public ResultEntity<Object> publishBuildWideTableTask(@RequestBody WideTableFieldConfigTaskDTO wideTableFieldConfigTaskDTO);

    /**
     * 立即重启
     *
     * @param buildTableNifiSettingDTO
     * @return
     */
    @PostMapping("/publishTask/immediatelyStart")
    public ResultEntity<Object> immediatelyStart(@RequestBody BuildTableNifiSettingDTO buildTableNifiSettingDTO);

    /**
     * 统一调度
     *
     * @param unifiedControlDTO unifiedControlDTO
     * @return
     */
    @PostMapping("/olapTask/publishBuildunifiedControlTask")
    public ResultEntity<Object> publishBuildunifiedControlTask(@RequestBody UnifiedControlDTO unifiedControlDTO);

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
    public void saveNifiStage(@RequestParam String data);

    /**
     * 删除nifi管道
     *
     * @return
     */
    @PostMapping("/nifi/deleteCustomWorkNifiFlow")
    public void deleteCustomWorkNifiFlow(@RequestBody NifiCustomWorkListDTO nifiCustomWorkListDTO);

    /**
     * 接入日志完善
     *
     * @return
     */
    @PostMapping("/pipeline/getPipelineTableLog")
    public ResultEntity<List<PipelineTableLogVO>> getPipelineTableLog(@RequestParam("data") String data, @RequestParam("pipelineTableQuery") String pipelineTableQuery);

    /**
     * 管道job日志
     *
     * @return
     */
    @PostMapping("/dispatchLog/getPipelJobLogVos")
    public ResultEntity<List<PipelJobLogVO>> getPipelJobLogVos(@RequestBody List<PipelJobLogVO> pipelJobLogs);

    /**
     * 任务日志
     *
     * @return
     */
    @PostMapping("/dispatchLog/getPipelTaskLogVos")
    public ResultEntity<List<PipelTaskLogVO>> getPipelTaskLogVos(@RequestBody List<PipelTaskLogVO> pipelTaskLogs);

    /**
     * 阶段日志
     *
     * @return
     */
    @PostMapping("/dispatchLog/getPipelStageLogVos")
    public ResultEntity<List<PipelStageLogVO>> getPipelStageLogVos(@RequestParam String taskId);

    /**
     * 暂停管道
     *
     * @return
     */
    @PostMapping("/nifi/suspendCustomWorkNifiFlow")
    public ResultEntity<Object> suspendCustomWorkNifiFlow(@RequestParam("nifiCustomWorkflowId") String nifiCustomWorkflowId, @RequestParam("ifFire") boolean ifFire);

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
    public ResultEntity<List<PipelLogVO>> getPipelLogVos(@RequestBody PipelLogVO pipelLog);

    /**
     * 依据pipelTraceId查询pipelId
     *
     * @param pipelTraceId
     * @return
     */
    @GetMapping("/dispatchLog/getPipelIdByPipelTraceId")
    public ResultEntity<String> getPipelIdByPipelTraceId(@RequestParam("pipelTraceId") String pipelTraceId);

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

    /**
     * @param data
     * @return
     */
    @PostMapping("/publishTask/publishBuildDeleteDataServices")
    public ResultEntity<Object> publishBuildDeleteDataServices(@RequestBody BuildDeleteTableServiceDTO data);

    /**
     * task.build.task.over
     *
     * @param dto
     */
    @PostMapping("/publishTask/missionEndCenter")
    public ResultEntity<Object> missionEndCenter(@RequestBody KafkaReceiveDTO dto);


}
