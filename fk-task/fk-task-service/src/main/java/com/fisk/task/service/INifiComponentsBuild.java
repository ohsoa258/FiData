package com.fisk.task.service;

import com.davis.client.model.*;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.nifi.AutoEndBranchTypeEnum;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.vo.pgsql.NifiVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.nifi.*;
import com.fisk.task.dto.nifi.FunnelDTO;
import com.fisk.task.vo.ProcessGroupsVO;

import java.util.List;

/**
 * Nifi组件创建
 *
 * @author gy
 */
public interface INifiComponentsBuild {

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
    BusinessResult<ProcessorEntity> buildReplaceTextProcess(BuildReplaceTextProcessorDTO data);

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
     * @param data dto
     * @param autoEnd auto end
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildExecuteSqlProcess(BuildExecuteSqlProcessorDTO data, List<String> autoEnd);

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
     * @param type 自动结束的流程
     * @return 连接entity
     */
    BusinessResult<ConnectionEntity> buildConnectProcessors(String groupId, String sourceId, String targetId, AutoEndBranchTypeEnum type);

    /**
     * 查询所有的分组
     *
     * @param groupId  组id
     * @return 所有的分组
     */
    BusinessResult<ProcessGroupsVO> getAllGroups(String groupId);

    /**
     * 查询分组个数
     *
     * @param groupId  组id
     * @return 所有的分组
     */
    int getGroupCount(String groupId);

    /**
     * Processor组件状态设置为开启
     * @param groupId 组id
     * @param entity 需要设置的组件
     * @return 设置结果
     */
    List<ProcessorEntity> enabledProcessor(String groupId, ProcessorEntity... entity);

    /**
     * Processor组件状态设置为开启
     * @param groupId 组id
     * @param entity 需要设置的组件
     * @return 设置结果
     */
    List<ProcessorEntity> enabledProcessor(String groupId, List<ProcessorEntity> entity);

    /**
     * 查询组件
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
    /*
    * 调用存储过程
    * */
    BusinessResult<ProcessorEntity> buildCallDbProcedureProcess(BuildCallDbProcedureProcessorDTO dto);
    /*
    * 定义insert语句values参数
    * */
    BusinessResult<ProcessorEntity> buildSqlParameterProcess(DataAccessConfigDTO config,BuildProcessEvaluateJsonPathDTO buildProcessEvaluateJsonPathDTO);
    /*
    * 拼装insert语句
    * */
    BusinessResult<ProcessorEntity> buildAssembleSqlProcess(DataAccessConfigDTO config,BuildReplaceTextProcessorDTO data);

    BusinessResult<ProcessorEntity> buildExecuteSQLRecordProcess(ExecuteSQLRecordDTO executeSQLRecordDTO);

    BusinessResult<ProcessorEntity> buildPutDatabaseRecordProcess(PutDatabaseRecordDTO putDatabaseRecordDTO);

    BusinessResult<ControllerServiceEntity> buildAvroReaderService(BuildAvroReaderServiceDTO data);

    BusinessResult<ProcessorEntity> buildUpdateRecord(BuildUpdateRecordDTO buildUpdateRecordDTO);

    BusinessResult<ControllerServiceEntity> buildAvroRecordSetWriterService(BuildAvroRecordSetWriterServiceDTO data);

    /*
     * 更新组件配置
     * */
    List<ProcessorEntity> updateProcessorConfig(String groupId, List<ProcessorEntity> entities);
    /*
     * 停止组件
     * */
     List<ProcessorEntity> stopProcessor(String groupId, List<ProcessorEntity> entities);

     /*
     * 修改组件调度
     * */
     ResultEnum modifyScheduling(String groupId, String ProcessorId, String schedulingStrategy, String schedulingPeriod);

    /*
     * 清空nifi组件队列
     * */
     ResultEnum emptyNifiConnectionQueue(String groupId);


    /*
     * 修改控制器服务状态
     * */
     ResultEnum controllerServicesRunStatus(String controllerServicesId);


    /*
     * 删除nifi流程
     * */
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

    /*
    * 删除input组件
    * */
    ResultEnum deleteNifiInputProcessor(List<PortEntity> portEntities);

    /*
    * 删除output组件
    * */
    ResultEnum deleteNifiOutputProcessor(List<PortEntity> portEntities);

    /*
    * 修改output 组件状态
    * */
    ResultEnum updateOutputStatus(List<PortEntity> portEntities,PortRunStatusEntity portRunStatusEntity);

    /**
     *修改input组件状态
     */
    ResultEnum updateInputStatus(List<PortEntity> portEntities,PortRunStatusEntity portRunStatusEntity);
    /*
    * 创建RedisConnectionPoolService控制器服务
    * */
    BusinessResult<ControllerServiceEntity> createRedisConnectionPoolService(BuildRedisConnectionPoolServiceDTO controllerServiceEntity);

    /*
     * 创建RedisConnectionPoolService控制器服务
     * */
    BusinessResult<ControllerServiceEntity> createRedisDistributedMapCacheClientService(BuildRedisDistributedMapCacheClientServiceDTO controllerServiceEntity);

    /*
    * 创建notify组件
    * */
    BusinessResult<ProcessorEntity> createNotifyProcessor(BuildNotifyProcessorDTO buildNotifyProcessorDTO);

    /*
    * 创建wait组件
    * */
    BusinessResult<ProcessorEntity> createWaitProcessor(BuildWaitProcessorDTO buildWaitProcessorDTO);

    /*
    * 创建漏斗
    * */
    BusinessResult<FunnelEntity> createFunnel(FunnelDTO funnelDTO);


}
