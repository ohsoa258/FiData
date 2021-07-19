package com.fisk.task.consumer.nifi;

import com.alibaba.fastjson.JSON;
import com.davis.client.model.ConnectionEntity;
import com.davis.client.model.ControllerServiceEntity;
import com.davis.client.model.ProcessGroupEntity;
import com.davis.client.model.ProcessorEntity;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.constants.NifiConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.nifi.AutoEndBranchTypeEnum;
import com.fisk.common.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.enums.task.nifi.StatementSqlTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.NifiAccessDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.daconfig.DataSourceConfig;
import com.fisk.task.dto.nifi.*;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.INifiComponentsBuild;
import com.fisk.task.utils.NifiPositionHelper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gy
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_NIFI_FLOW)
@Slf4j
public class BuildNifiTaskListener {

    @Value("${dorisconstr.url}")
    private String dorisUrl;
    @Value("${dorisconstr.username}")
    private String dorisUser;
    @Value("${dorisconstr.password}")
    private String dorisPwd;
    @Value("${dorisconstr.driver_class_name}")
    private String dorisDriver;

    @Resource
    INifiComponentsBuild componentsBuild;
    @Resource
    DataAccessClient client;

    @RabbitHandler
    @MQConsumerLog
    public void msg(String data, Channel channel, Message message) {
        BuildNifiFlowDTO dto = JSON.parseObject(data, BuildNifiFlowDTO.class);
        //获取数据接入配置项
        DataAccessConfigDTO configDTO = getConfigData(dto.id, dto.appId);
        if (configDTO == null) {
            log.error("数据接入配置项获取失败。id: 【" + dto.id + "】, appId: 【" + dto.appId + "】");
            return;
        }
        log.info(JSON.toJSONString("【数据接入配置项参数】" + configDTO));
        //1. 创建应用组
        ProcessGroupEntity groupEntity = buildAppGroup(configDTO);
        //2. 创建jdbc连接池
        List<ControllerServiceEntity> dbPool = buildDsConnectionPool(configDTO, groupEntity.getId());
        //3. 创建任务组
        ProcessGroupEntity taskGroupEntity = buildTaskGroup(configDTO, groupEntity.getId());
        //4. 创建组件
        List<ProcessorEntity> processors = buildProcessor(configDTO, taskGroupEntity.getId(), dbPool.get(0).getId(), dbPool.get(1).getId());
        //5. 启动组件
        enabledProcessor(taskGroupEntity.getId(), processors);
        //6. 回写id
        writeBackComponentId(dto.appId, groupEntity.getId(), dto.id, taskGroupEntity.getId());
    }

    /**
     * 获取数据接入的配置
     *
     * @param appId 配置的id
     * @return 数据接入配置
     */
    private DataAccessConfigDTO getConfigData(long id, long appId) {

        //region
        //DataAccessConfigDTO dto = new DataAccessConfigDTO();
        //GroupConfig groupConfig = new GroupConfig();
        //groupConfig.appName = "Rabbit Consumer Build Nifi Data Flow";
        //groupConfig.appDetails = "...";
        //groupConfig.newApp = true;
        //groupConfig.componentId = "017a121f-4f36-11d6-6dbb-07fd97574e96";
        //dto.groupConfig = groupConfig;
        //TaskGroupConfig taskGroupConfig = new TaskGroupConfig();
        //taskGroupConfig.appName = "Task1";
        //taskGroupConfig.appDetails = "...";
        //dto.taskGroupConfig = taskGroupConfig;
        //DataSourceConfig config1 = new DataSourceConfig();
        //config1.type = DriverTypeEnum.MYSQL;
        //config1.user = "root";
        //config1.password = "root123";
        //config1.jdbcStr = "jdbc:mysql://192.168.11.130:3306/dmp_chartvisual_db";
        //config1.componentId = "017a1221-4f36-11d6-116e-33e37592ccd8";
        //dto.sourceDsConfig = config1;
        DataSourceConfig config2 = new DataSourceConfig();
        config2.type = DriverTypeEnum.MYSQL;
        config2.user = dorisUser;
        config2.password = dorisPwd;
        config2.jdbcStr = dorisUrl;
        //config2.componentId = "017a1220-4f36-11d6-9384-d2fdf9557105";
        //dto.targetDsConfig = config2;
        //ProcessorConfig processorConfig = new ProcessorConfig();
        //processorConfig.scheduleType = SchedulingStrategyTypeEnum.CRON;
        //processorConfig.scheduleExpression = "0/30 * * * * ?";
        //processorConfig.sourceExecSqlQuery = "select * from tb_test_data where id = ${Increment}";
        //processorConfig.targetTableName = "tb_test_data";
        //processorConfig.fieldName = "$.Increment";
        //dto.processorConfig = processorConfig;
        //return dto;
        //endregion
        ResultEntity<DataAccessConfigDTO> res = client.dataAccessConfig(id, appId);
        res.data.targetDsConfig = config2;
//        res.data.sourceDsConfig.type = DriverTypeEnum.MYSQL;
        return res.data;
    }

    /**
     * 创建app组
     *
     * @param config 数据接入配置
     * @return 组信息
     */
    private ProcessGroupEntity buildAppGroup(DataAccessConfigDTO config) {
        //判断是否需要新建组
        if (config.groupConfig.newApp) {
            BuildProcessGroupDTO dto = new BuildProcessGroupDTO();
            dto.name = config.groupConfig.appName;
            dto.details = config.groupConfig.appDetails;
            //根据组个数，定义坐标
            int count = componentsBuild.getGroupCount(NifiConstants.ApiConstants.ROOT_NODE);
            dto.positionDTO = NifiPositionHelper.buildXPositionDTO(count);
            //创建组件
            BusinessResult<ProcessGroupEntity> res = componentsBuild.buildProcessGroup(dto);
            if (res.success) {
                return res.data;
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
            }
        } else {
            //说明组件已存在，查询组件并返回
            BusinessResult<ProcessGroupEntity> res = componentsBuild.getProcessGroupById(config.groupConfig.componentId);
            if (res.success) {
                return res.data;
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
            }
        }
    }

    /**
     * 创建任务组
     *
     * @param config 数据接入配置
     * @return 组信息
     */
    private ProcessGroupEntity buildTaskGroup(DataAccessConfigDTO config, String groupId) {
        BuildProcessGroupDTO dto = new BuildProcessGroupDTO();
        dto.name = config.taskGroupConfig.appName;
        dto.details = config.taskGroupConfig.appDetails;
        dto.pid = groupId;
        //根据组个数，定义坐标
        int count = componentsBuild.getGroupCount(groupId);
        dto.positionDTO = NifiPositionHelper.buildXPositionDTO(count);
        //创建组件
        BusinessResult<ProcessGroupEntity> res = componentsBuild.buildProcessGroup(dto);
        if (res.success) {
            return res.data;
        } else {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
        }
    }

    /**
     * 创建数据库连接池
     *
     * @param config 数据接入配置
     * @return 控制器服务对象
     */
    private List<ControllerServiceEntity> buildDsConnectionPool(DataAccessConfigDTO config, String groupId) {
        List<ControllerServiceEntity> list = new ArrayList<>();
        if (config.groupConfig.newApp) {
            BuildDbControllerServiceDTO targetDto = buildDbControllerServiceDTO(config, groupId, true);
            BusinessResult<ControllerServiceEntity> targetRes = componentsBuild.buildDbControllerService(targetDto);

            BuildDbControllerServiceDTO sourceDto = buildDbControllerServiceDTO(config, groupId, false);
            BusinessResult<ControllerServiceEntity> sourceRes = componentsBuild.buildDbControllerService(sourceDto);

            if (targetRes.success && sourceRes.success) {
                list.add(sourceRes.data);
                list.add(targetRes.data);
                return list;
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR);
            }
        } else {
            ControllerServiceEntity sourceRes = componentsBuild.getDbControllerService(config.sourceDsConfig.getComponentId());
            ControllerServiceEntity targetRes = componentsBuild.getDbControllerService(config.sourceDsConfig.getComponentId());
            if (targetRes != null && sourceRes != null) {
                list.add(sourceRes);
                list.add(targetRes);
                return list;
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR);
            }
        }
    }

    /**
     * 创建控制器服务对象
     *
     * @param config 数据接入配置
     * @param target 源/目标
     * @return dto
     */
    private BuildDbControllerServiceDTO buildDbControllerServiceDTO(DataAccessConfigDTO config, String groupId, boolean target) {
        DataSourceConfig dsConfig = target ? config.targetDsConfig : config.sourceDsConfig;
        BuildDbControllerServiceDTO dto = new BuildDbControllerServiceDTO();
        dto.conUrl = dsConfig.jdbcStr;
        dto.driverName = dsConfig.type.getName();
        dto.user = dsConfig.user;
        dto.pwd = dsConfig.password;
        dto.enabled = true;
        dto.groupId = groupId;
        dto.name = target ? "Target Data Source Connection" : "Source Data Source Connection";
        dto.details = dto.name;
        switch (dsConfig.type) {
            case MYSQL:
                dto.driverLocation = NifiConstants.DirverConstants.MYSQL_DIRVER_PATH;
                break;
            default:
                break;
        }
        return dto;
    }

    /**
     * 创建nifi流程
     *
     * @param config         数据接入配置
     * @param groupId        组id
     * @param sourceDbPoolId 数据源连接池id
     * @param targetDbPoolId 目标连接池id
     */
    private List<ProcessorEntity> buildProcessor(DataAccessConfigDTO config, String groupId, String sourceDbPoolId, String targetDbPoolId) {
        //读取增量字段组件
        //ProcessorEntity queryField = queryIncrementFieldProcessor(config, groupId, sourceDbPoolId);
        //创建数据转换json组件
        //ProcessorEntity jsonRes = convertJsonProcessor(groupId, 2);
        //连接器
        //componentConnector(groupId, queryField.getId(), jsonRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //字段转换nifi变量
        //ProcessorEntity evaluateJson = evaluateJsonPathProcessor(config, groupId);
        //连接器
        //componentConnector(groupId, jsonRes.getId(), evaluateJson.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //创建执行删除组件
        ProcessorEntity delSqlRes = execDeleteSqlProcessor(config, groupId, targetDbPoolId);
        //连接器
        //componentConnector(groupId, evaluateJson.getId(), delSqlRes.getId(), AutoEndBranchTypeEnum.MATCHED);
        //创建查询组件
        ProcessorEntity querySqlRes = execSqlProcessor(config, groupId, sourceDbPoolId);
        //连接器
        componentConnector(groupId, delSqlRes.getId(), querySqlRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //创建数据转换json组件
        ProcessorEntity toJsonRes = convertJsonProcessor(groupId, 6);
        //连接器
        componentConnector(groupId, querySqlRes.getId(), toJsonRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //创建json转sql组件
        ProcessorEntity toSqlRes = convertJsonToSqlProcessor(config, groupId, targetDbPoolId);
        //连接器
        componentConnector(groupId, toJsonRes.getId(), toSqlRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //创建执行sql组件
        ProcessorEntity putSqlRes = putSqlProcessor(groupId, targetDbPoolId);
        //连接器
        componentConnector(groupId, toSqlRes.getId(), putSqlRes.getId(), AutoEndBranchTypeEnum.SQL);

        List<ProcessorEntity> res = new ArrayList<>();
        //res.add(queryField);
        //res.add(jsonRes);
        //res.add(evaluateJson);
        res.add(delSqlRes);
        res.add(querySqlRes);
        res.add(toJsonRes);
        res.add(toSqlRes);
        res.add(putSqlRes);
        return res;
    }

    /**
     * 组件连接器
     *
     * @param groupId  组id
     * @param sourceId 连接器上方组件id
     * @param targetId 连接器下方组件id
     * @param type     连接类型
     */
    private void componentConnector(String groupId, String sourceId, String targetId, AutoEndBranchTypeEnum type) {
        BusinessResult<ConnectionEntity> sqlToPutRes = componentsBuild.buildConnectProcessors(groupId, sourceId, targetId, type);
        verifyProcessorResult(sqlToPutRes);
    }

    /**
     * 执行sql组件
     *
     * @param groupId        组id
     * @param targetDbPoolId 连接池id
     * @return 组件对象
     */
    private ProcessorEntity putSqlProcessor(String groupId, String targetDbPoolId) {
        BuildPutSqlProcessorDTO putSqlDto = new BuildPutSqlProcessorDTO();
        putSqlDto.name = "Put sql to target data source";
        putSqlDto.details = "Put sql to target data source";
        putSqlDto.groupId = groupId;
        putSqlDto.dbConnectionId = targetDbPoolId;
        putSqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(8);
        BusinessResult<ProcessorEntity> putSqlRes = componentsBuild.buildPutSqlProcess(putSqlDto);
        verifyProcessorResult(putSqlRes);
        return putSqlRes.data;
    }

    /**
     * json转sql组件
     *
     * @param config         数据接入配置
     * @param groupId        组id
     * @param targetDbPoolId 目标数据库连接池id
     * @return 组件对象
     */
    private ProcessorEntity convertJsonToSqlProcessor(DataAccessConfigDTO config, String groupId, String targetDbPoolId) {
        BuildConvertJsonToSqlProcessorDTO toSqlDto = new BuildConvertJsonToSqlProcessorDTO();
        toSqlDto.name = "Convert Json To Sql";
        toSqlDto.details = "Convert data to sql";
        toSqlDto.dbConnectionId = targetDbPoolId;
        toSqlDto.groupId = groupId;
        toSqlDto.tableName = config.processorConfig.targetTableName;
        toSqlDto.sqlType = StatementSqlTypeEnum.INSERT;
        toSqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(7);
        BusinessResult<ProcessorEntity> toSqlRes = componentsBuild.buildConvertJsonToSqlProcess(toSqlDto);
        verifyProcessorResult(toSqlRes);
        return toSqlRes.data;
    }

    /**
     * data转json组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity convertJsonProcessor(String groupId, int level) {
        BuildConvertToJsonProcessorDTO toJsonDto = new BuildConvertToJsonProcessorDTO();
        toJsonDto.name = "Convert Data To Json";
        toJsonDto.details = "Convert data source to json";
        toJsonDto.groupId = groupId;
        toJsonDto.positionDTO = NifiPositionHelper.buildYPositionDTO(level);
        BusinessResult<ProcessorEntity> toJsonRes = componentsBuild.buildConvertToJsonProcess(toJsonDto);
        verifyProcessorResult(toJsonRes);
        return toJsonRes.data;
    }

    /**
     * 执行sql query组件
     *
     * @param config         数据接入配置
     * @param groupId        组id
     * @param sourceDbPoolId 数据源连接池id
     * @return 组件对象
     */
    private ProcessorEntity execSqlProcessor(DataAccessConfigDTO config, String groupId, String sourceDbPoolId) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Exec DataSource Query";
        querySqlDto.details = "Execute SQL query in the data source";
        querySqlDto.groupId = groupId;
        querySqlDto.querySql = config.processorConfig.sourceExecSqlQuery;
        querySqlDto.dbConnectionId = sourceDbPoolId;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(5);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto);
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    /**
     * 执行sql delete组件
     *
     * @param config         数据接入配置
     * @param groupId        组id
     * @param targetDbPoolId ods连接池id
     * @return 组件对象
     */
    private ProcessorEntity execDeleteSqlProcessor(DataAccessConfigDTO config, String groupId, String targetDbPoolId) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Exec Target Delete";
        querySqlDto.details = "Execute Delete SQL in the data target";
        querySqlDto.groupId = groupId;
        querySqlDto.querySql = "TRUNCATE table " + config.processorConfig.targetTableName;
        querySqlDto.dbConnectionId = targetDbPoolId;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(4);
        querySqlDto.scheduleType = config.processorConfig.scheduleType;
        querySqlDto.scheduleExpression = config.processorConfig.scheduleExpression;
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto);
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    /**
     * 执行sql 查询增量字段组件
     *
     * @param config  数据接入配置
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity evaluateJsonPathProcessor(DataAccessConfigDTO config, String groupId) {
        BuildProcessEvaluateJsonPathDTO dto = new BuildProcessEvaluateJsonPathDTO();
        dto.name = "Set Increment Field";
        dto.details = "Set Increment Field to Nifi Data flow";
        dto.groupId = groupId;
        dto.fieldName = config.processorConfig.fieldName;
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(3);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildEvaluateJsonPathProcess(dto);
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    /**
     * 执行sql 查询增量字段组件
     *
     * @param config         数据接入配置
     * @param groupId        组id
     * @param sourceDbPoolId 数据源连接池id
     * @return 组件对象
     */
    private ProcessorEntity queryIncrementFieldProcessor(DataAccessConfigDTO config, String groupId, String sourceDbPoolId) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Query Increment Field";
        querySqlDto.details = "Query Increment Field in the data source";
        querySqlDto.groupId = groupId;
        querySqlDto.querySql = "select max(id) as " + NifiConstants.AttrConstants.INCREMENT_NAME + " from tb_test_data";
        querySqlDto.dbConnectionId = sourceDbPoolId;
        querySqlDto.scheduleExpression = config.processorConfig.scheduleExpression;
        querySqlDto.scheduleType = config.processorConfig.scheduleType;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(1);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto);
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    /**
     * 验证是否执行成功
     *
     * @param result 判断条件
     */
    private void verifyProcessorResult(BusinessResult<?> result) {
        if (!result.success) {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, result.msg);
        }
    }

    /**
     * 启用组件
     *
     * @param groupId    groupId
     * @param processors 需要启用的组件
     */
    private void enabledProcessor(String groupId, List<ProcessorEntity> processors) {
        List<ProcessorEntity> enableRes = componentsBuild.enabledProcessor(groupId, processors);
    }

    /**
     * 回写组件id
     *
     * @param appId            应用id
     * @param appComponentId   应用组id
     * @param tableId          物理表id
     * @param tableComponentId 任务组id
     */
    private void writeBackComponentId(long appId, String appComponentId, long tableId, String tableComponentId) {
        NifiAccessDTO dto = new NifiAccessDTO();
        dto.appid = appId;
        dto.appGroupId = appComponentId;
        dto.tableId = tableId;
        dto.tableGroupId = tableComponentId;
        client.addComponentId(dto);
    }
}
