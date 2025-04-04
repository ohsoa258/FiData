package com.fisk.task.utils.nifi;

import com.davis.client.model.*;
import com.fisk.common.core.baseObject.entity.BusinessResult;
import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.common.core.enums.task.nifi.AutoEndBranchTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.nifi.FunnelDTO;
import com.fisk.task.dto.nifi.*;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.dto.task.TableNifiSettingDTO;
import com.fisk.task.dto.task.UpdateControllerServiceConfigDTO;
import com.fisk.task.vo.ProcessGroupsVO;

import java.util.List;
import java.util.Map;

/**
 * Nifi组件创建
 *
 * @author gy
 */
public interface INiFiHelper {

    /* ===========组=========== */

    /**
     * 创建组
     *
     * @param dto dto
     * @return 创建的实体
     */
    BusinessResult<ProcessGroupEntity> buildProcessGroup(BuildProcessGroupDTO dto);

    /**
     * 根据组id获取组
     *
     * @param id 组id
     * @return 组
     */
    BusinessResult<ProcessGroupEntity> getProcessGroupById(String id);

    /* ===========连接池=========== */

    /**
     * 数据库连接对象创建
     *
     * @param dto dto
     * @return 创建的连接对象
     */
    BusinessResult<ControllerServiceEntity> buildDbControllerService(BuildDbControllerServiceDTO dto);

    /**
     * 数据库连接对象创建
     *
     * @param dto dto
     * @return 创建的连接对象
     */
    BusinessResult<ControllerServiceEntity> buildMongoDbControllerService(BuildDbControllerServiceDTO dto);

    /**
     * 获取控制器服务的实体
     *
     * @param id controller-service的id
     * @return 控制器服务实体
     */
    ControllerServiceEntity getDbControllerService(String id);

    /**
     * 修改控制器服务运行状态
     *
     * @param id controller-service的id
     * @return 控制器服务实体
     */
    BusinessResult<ControllerServiceEntity> updateDbControllerServiceState(String id);

    /**
     * 修改控制器服务运行状态
     *
     * @param entity 修改的controller-service实体
     * @return 控制器服务实体
     */
    BusinessResult<ControllerServiceEntity> updateDbControllerServiceState(ControllerServiceEntity entity);

    /* 组件创建 */

    /**
     * 创建MergeContent组件
     *
     * @param data dto
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildMergeContentProcess(BuildMergeContentProcessorDTO data);

    /**
     * 创建ReplaceText组件
     *
     * @param data dto
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildReplaceTextProcess(BuildReplaceTextProcessorDTO data, List<String> auto);

    /**
     * 创建PublishMQ组件
     *
     * @param data dto
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildPublishMqProcess(BuildPublishMqProcessorDTO data);

    /**
     * 创建ExecuteSQL组件
     *
     * @param data    dto
     * @param autoEnd auto end
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildExecuteSqlProcess(BuildExecuteSqlProcessorDTO data, List<String> autoEnd);

    /**
     * 创建ExecuteSQL组件
     *
     * @param data    dto
     * @param autoEnd auto end
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildExecuteSqlProcessForDoris(BuildExecuteSqlProcessorDTO data, List<String> autoEnd);

    /**
     * 创建ConsumeKafka组件
     *
     * @param data dto
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildConsumeKafkaProcessor(BuildConsumeKafkaProcessorDTO data);

    /**
     * 创建PublishKafka组件
     *
     * @param data dto
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildPublishKafkaProcessor(BuildPublishKafkaProcessorDTO data);

    /**
     * 创建ConvertToJson组件
     *
     * @param data dto
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildConvertToJsonProcess(BuildConvertToJsonProcessorDTO data);

    /**
     * 创建ConvertJsonToSql组件
     *
     * @param data dto
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildConvertJsonToSqlProcess(BuildConvertJsonToSqlProcessorDTO data);

    /**
     * 创建ConvertJsonToSql组件
     *
     * @param data dto
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildPutSqlProcess(BuildPutSqlProcessorDTO data);

    /**
     * 创建UpdateAttribute组件
     *
     * @param data dto
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildUpdateAttribute(BuildUpdateAttributeDTO data);

    /**
     * 创建EvaluateJsonPath组件
     *
     * @param dto dto
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildEvaluateJsonPathProcess(BuildProcessEvaluateJsonPathDTO dto);

    /**
     * 连接两个Processor组件
     *
     * @param groupId  组id
     * @param sourceId 源组件的id
     * @param targetId 目标组件的id
     * @param type     自动结束的流程
     * @return 连接entity
     */
    BusinessResult<ConnectionEntity> buildConnectProcessors(String groupId, String sourceId, String targetId, AutoEndBranchTypeEnum type);

    /**
     * 连接两个Processor组件 是否集群
     *
     * @param groupId  组id
     * @param sourceId 源组件的id
     * @param targetId 目标组件的id
     * @param type     自动结束的流程
     * @return 连接entity
     */
    BusinessResult<ConnectionEntity> buildConnectProcessorsForNifiClusterEnable(String groupId, String sourceId, String targetId, AutoEndBranchTypeEnum type);

    /**
     * 连接两个Processor组件,多种关系
     *
     * @param groupId  组id
     * @param sourceId 源组件的id
     * @param targetId 目标组件的id
     * @param type     自动结束的流程
     * @return 连接entity
     */
    BusinessResult<ConnectionEntity> buildConnectProcessor(String groupId, String sourceId, String targetId, List<AutoEndBranchTypeEnum> type);

    /**
     * 查询所有的分组
     *
     * @param groupId 组id
     * @return 所有的分组
     */
    BusinessResult<ProcessGroupsVO> getAllGroups(String groupId);

    /**
     * 查询分组个数
     *
     * @param groupId 组id
     * @return 所有的分组
     */
    int getGroupCount(String groupId);

    /**
     * Processor组件状态设置为开启
     *
     * @param groupId 组id
     * @param entity  需要设置的组件
     * @return 设置结果
     */
    List<ProcessorEntity> enabledProcessor(String groupId, ProcessorEntity... entity);

    /**
     * Processor组件状态设置为开启
     *
     * @param groupId 组id
     * @param entity  需要设置的组件
     * @return 设置结果
     */
    List<ProcessorEntity> enabledProcessor(String groupId, List<ProcessorEntity> entity);

    /**
     * 查询组件
     *
     * @param id 组件id
     * @return 组件entity
     */
    ProcessorEntity getProcessor(String id);

    /**
     * 创建splitjson组件
     *
     * @param data dto
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildSplitJsonProcess(BuildSplitJsonProcessorDTO data);

    /**
     * 调用存储过程
     *
     * @param dto
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildCallDbProcedureProcess(BuildCallDbProcedureProcessorDTO dto);

    /**
     * 调用存储过程
     *
     * @param dto
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildCallDbProcedureProcessForDoris(BuildCallDbProcedureProcessorDTO dto);

    /**
     * 定义insert语句values参数
     *
     * @param config                          config
     * @param buildProcessEvaluateJsonPathDTO buildProcessEvaluateJsonPathDTO
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildSqlParameterProcess(DataAccessConfigDTO config, BuildProcessEvaluateJsonPathDTO buildProcessEvaluateJsonPathDTO);

    /**
     * 拼装insert语句
     *
     * @param config config
     * @param data   data
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildAssembleSqlProcess(DataAccessConfigDTO config, BuildReplaceTextProcessorDTO data);

    /**
     * ExecuteSQLRecordProcess
     *
     * @param executeSQLRecordDTO executeSQLRecordDTO
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildExecuteSQLRecordProcess(ExecuteSQLRecordDTO executeSQLRecordDTO);

    /**
     * ExecuteSQLRecordProcess
     *
     * @param mongoDTO
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildGetMongoProcess(GetMongoDTO mongoDTO);

    BusinessResult<ProcessorEntity> buildConvertAvroToJSON(GetMongoDTO mongoDTO);

    /**
     * ExecuteSQLRecordProcess
     *
     * @param executeSQLRecordDTO executeSQLRecordDTO
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildExecuteSQLRecordProcessForDoris(ExecuteSQLRecordDTO executeSQLRecordDTO);

    /**
     * buildPutDatabaseRecordProcess
     *
     * @param putDatabaseRecordDTO putDatabaseRecordDTO
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildPutDatabaseRecordProcess(PutDatabaseRecordDTO putDatabaseRecordDTO);

    /**
     * buildAvroReaderService
     *
     * @param data data
     * @return 控制器服务
     */
    BusinessResult<ControllerServiceEntity> buildAvroReaderService(BuildAvroReaderServiceDTO data);

    /**
     * buildUpdateRecord
     *
     * @param buildUpdateRecordDTO buildUpdateRecordDTO
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildUpdateRecord(BuildUpdateRecordDTO buildUpdateRecordDTO);

    /**
     * buildConvertMongoJsonToAvro
     *
     * @param buildUpdateRecordDTO BuildConvertRecordDTO
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildConvertMongoJsonToAvro(BuildConvertRecordDTO buildUpdateRecordDTO);

    /**
     * buildAvroRecordSetWriterService
     *
     * @param data data
     * @return 控制器服务
     */
    BusinessResult<ControllerServiceEntity> buildAvroRecordSetWriterService(BuildAvroRecordSetWriterServiceDTO data);

    /**
     * buildAvroRecordSetWriterService
     *
     * @param data data
     * @return 控制器服务
     */
    BusinessResult<ControllerServiceEntity> buildJsonTreeReaderService(BuildAvroRecordSetWriterServiceDTO data);

    /**
     * 更新组件配置
     *
     * @param groupId  groupId
     * @param entities entities
     * @return Processor组件集合
     */
    List<ProcessorEntity> updateProcessorConfig(String groupId, List<ProcessorEntity> entities);

    /**
     * 停止组件
     *
     * @param groupId  groupId
     * @param entities entities
     * @return Processor组件集合
     */
    List<ProcessorEntity> stopProcessor(String groupId, List<ProcessorEntity> entities);

    /**
     * 修改组件调度
     *
     * @param groupId            groupId
     * @param ProcessorId        ProcessorId
     * @param schedulingStrategy schedulingStrategy
     * @param schedulingPeriod   schedulingPeriod
     * @return ResultEnum
     */
    ResultEnum modifyScheduling(String groupId, String ProcessorId, String schedulingStrategy, String schedulingPeriod);

    /**
     * 清空nifi组件队列
     *
     * @param groupId groupId
     * @return ResultEnum
     */
    ResultEnum emptyNifiConnectionQueue(String groupId);

    /**
     * 修改控制器服务状态
     *
     * @param controllerServicesId controllerServicesId
     * @return ResultEnum
     */
    ResultEnum controllerServicesRunStatus(String controllerServicesId);

    /**
     * 删除nifi流程
     *
     * @param dataModelVO dataModelVO
     * @return ResultEnum
     */
    ResultEnum deleteNifiFlow(DataModelVO dataModelVO);

    /**
     * 创建input port组件
     *
     * @param buildPortDTO buildPortDTO
     * @return 返回值
     */
    PortEntity buildInputPort(BuildPortDTO buildPortDTO);

    /**
     * 创建output port组件
     *
     * @param buildPortDTO buildPortDTO
     * @return 返回值
     */
    PortEntity buildOutputPort(BuildPortDTO buildPortDTO);

    /**
     * 创建input_port连接
     *
     * @param buildConnectDTO buildConnectDTO
     * @return 执行结果
     */
    ConnectionEntity buildInputPortConnections(BuildConnectDTO buildConnectDTO);

    /**
     * 创建output_port连接
     *
     * @param buildConnectDTO buildConnectDTO
     * @return 返回值
     */
    ConnectionEntity buildOutPortPortConnections(BuildConnectDTO buildConnectDTO);

    /**
     * 删除input组件
     *
     * @param portEntities portEntities
     * @return ResultEnum
     */
    ResultEnum deleteNifiInputProcessor(List<PortEntity> portEntities);

    /**
     * 删除output组件
     *
     * @param portEntities portEntities
     * @return ResultEnum
     */
    ResultEnum deleteNifiOutputProcessor(List<PortEntity> portEntities);

    /**
     * 修改output 组件状态
     *
     * @param portEntities        portEntities
     * @param portRunStatusEntity portRunStatusEntity
     * @return ResultEnum
     */
    ResultEnum updateOutputStatus(List<PortEntity> portEntities, PortRunStatusEntity portRunStatusEntity);

    /**
     * 修改input组件状态
     *
     * @param portEntities        portEntities
     * @param portRunStatusEntity portRunStatusEntity
     * @return ResultEnum
     */
    ResultEnum updateInputStatus(List<PortEntity> portEntities, PortRunStatusEntity portRunStatusEntity);

    /**
     * 创建RedisConnectionPoolService控制器服务
     *
     * @param controllerServiceEntity controllerServiceEntity
     * @return ControllerServiceEntity 控制器服务
     */
    BusinessResult<ControllerServiceEntity> createRedisConnectionPoolService(BuildRedisConnectionPoolServiceDTO controllerServiceEntity);

    /**
     * 创建RedisDistributedMapCacheClientService控制器服务
     *
     * @param controllerServiceEntity controllerServiceEntity
     * @return ControllerServiceEntity 控制器服务
     */
    BusinessResult<ControllerServiceEntity> createRedisDistributedMapCacheClientService(BuildRedisDistributedMapCacheClientServiceDTO controllerServiceEntity);

    /**
     * 创建notify组件
     *
     * @param buildNotifyProcessorDTO buildNotifyProcessorDTO
     * @return ProcessorEntity
     */
    BusinessResult<ProcessorEntity> createNotifyProcessor(BuildNotifyProcessorDTO buildNotifyProcessorDTO);

    /**
     * 创建wait组件
     *
     * @param buildWaitProcessorDTO buildWaitProcessorDTO
     * @return ProcessorEntity
     */
    BusinessResult<ProcessorEntity> createWaitProcessor(BuildWaitProcessorDTO buildWaitProcessorDTO);

    /**
     * 创建漏斗
     *
     * @param funnelDTO funnelDTO
     * @return FunnelEntity
     */
    BusinessResult<FunnelEntity> createFunnel(FunnelDTO funnelDTO);

    /**
     * 创建nifi全局变量
     *
     * @param variable 配置信息
     * @return
     */
    void buildNifiGlobalVariable(Map<String, String> variable);

    /**
     * 修改nifi全局变量
     *
     * @param variable
     */
    void updateNifiGlobalVariable(Map<String, String> variable);

    /**
     * getSqlForPgOds
     *
     * @param config 配置信息
     * @return List<String>
     */
    List<String> getSqlForPgOds(DataAccessConfigDTO config);

    /**
     * getSqlForDorisOds
     *
     * @param config 配置信息
     * @return List<String>
     */
    List<String> getSqlForDorisOds(DataAccessConfigDTO config);

    /**
     * getSqlForPgOds  API&WEBSERVICE
     *
     * @param config 配置信息
     * @return List<String>
     */
    List<String> getSqlForPgOdsV2(DataAccessConfigDTO config);

    /**
     * assemblySql
     *
     * @param config              配置信息
     * @param synchronousTypeEnum synchronousTypeEnum
     * @param funcName            funcName
     * @return String
     */
    String assemblySql(DataAccessConfigDTO config, SynchronousTypeEnum synchronousTypeEnum, String funcName, BuildNifiFlowDTO buildNifiFlow);

    /**
     * immediatelyStart
     *
     * @param tableNifiSettingDTO 配置信息
     * @return String
     */
    void immediatelyStart(TableNifiSettingDTO tableNifiSettingDTO);

    /**
     * buildGetFTPProcess
     *
     * @param data 配置信息
     * @return ProcessorEntity
     */
    BusinessResult<ProcessorEntity> buildGetFTPProcess(BuildGetFTPProcessorDTO data);

    /**
     * buildConvertExcelToCSVProcess
     *
     * @param data 配置信息
     * @return ProcessorEntity
     */
    BusinessResult<ProcessorEntity> buildConvertExcelToCSVProcess(BuildConvertExcelToCSVProcessorDTO data);

    /**
     * buildCSVReaderService
     *
     * @param data 配置信息
     * @return ControllerServiceEntity
     */
    BusinessResult<ControllerServiceEntity> buildCSVReaderService(BuildCSVReaderProcessorDTO data);

    /**
     * buildConvertRecordProcess
     *
     * @param data 配置信息
     * @return ProcessorEntity
     */
    BusinessResult<ProcessorEntity> buildConvertRecordProcess(BuildConvertRecordProcessorDTO data);

    /**
     * buildFetchFTPProcess
     *
     * @param data 配置信息
     * @return ProcessorEntity
     */
    BusinessResult<ProcessorEntity> buildFetchFTPProcess(BuildFetchFTPProcessorDTO data);

    /**
     * buildGenerateFlowFileProcessor
     *
     * @param data 调用api参数准备
     * @return ProcessorEntity
     */
    public BusinessResult<ProcessorEntity> buildGenerateFlowFileProcessor(BuildGenerateFlowFileProcessorDTO data, List<String> autoEnd);

    /**
     * buildInvokeHTTPProcessor
     *
     * @param data 发送http请求
     * @return ProcessorEntity
     */
    public BusinessResult<ProcessorEntity> buildInvokeHTTPProcessor(BuildInvokeHttpProcessorDTO data, List<String> autoEnd);

    /**
     * buildKeytabCredentialsService
     *
     * @param data 创建Kerberos认证控制器服务
     * @return ProcessorEntity
     */
    public BusinessResult<ControllerServiceEntity> buildKeytabCredentialsService(BuildKeytabCredentialsServiceDTO data);

    /**
     * 停止单个组件
     *
     * @param groupId         groupId
     * @param processorEntity processorEntity
     * @return ProcessorEntity
     */
    public List<ProcessorEntity> stopProcessor(String groupId, ProcessorEntity processorEntity);

    /**
     * 创建sftp组件
     *
     * @param data
     * @return
     */
    public BusinessResult<ProcessorEntity> buildFetchSFTPProcess(BuildFetchSFTPProcessorDTO data);

    /**
     * 修改控制器服务配置信息
     *
     * @param dto 修改参数
     */
    BusinessResult<String> UpdateDbControllerServiceConfig(UpdateControllerServiceConfigDTO dto);

}
