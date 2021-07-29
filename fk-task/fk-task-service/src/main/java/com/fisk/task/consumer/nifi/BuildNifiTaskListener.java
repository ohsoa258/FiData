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
import com.fisk.common.enums.task.nifi.DbPoolTypeEnum;
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
import org.apache.commons.lang3.StringUtils;
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
        //1. 获取数据接入配置库连接池
        ControllerServiceEntity cfgDbPool = buildCfgDsPool(configDTO);
        //2. 创建应用组
        ProcessGroupEntity groupEntity = buildAppGroup(configDTO);
        //3. 创建jdbc连接池
        List<ControllerServiceEntity> dbPool = buildDsConnectionPool(configDTO, groupEntity.getId());
        //4. 创建任务组
        ProcessGroupEntity taskGroupEntity = buildTaskGroup(configDTO, groupEntity.getId());

        //5. 创建组件
        List<ProcessorEntity> processors = buildProcessor(configDTO, taskGroupEntity.getId(), dbPool.get(0).getId(), dbPool.get(1).getId(), cfgDbPool.getId());
        //6. 启动组件
        enabledProcessor(taskGroupEntity.getId(), processors);
        //7. 回写id
        writeBackComponentId(dto.appId, groupEntity.getId(), dto.id, taskGroupEntity.getId(), dbPool.get(0).getId(), dbPool.get(1).getId(), cfgDbPool.getId());
    }

    /**
     * 获取数据接入的配置
     *
     * @param appId 配置的id
     * @return 数据接入配置
     */
    private DataAccessConfigDTO getConfigData(long id, long appId) {
        ResultEntity<DataAccessConfigDTO> res = client.dataAccessConfig(id, appId);
        if (res.code != ResultEnum.SUCCESS.getCode()) {
            return null;
        }
        //target doris
        DataSourceConfig targetDbPoolConfig = new DataSourceConfig();
        targetDbPoolConfig.type = DriverTypeEnum.MYSQL;
        targetDbPoolConfig.user = dorisUser;
        targetDbPoolConfig.password = dorisPwd;
        targetDbPoolConfig.jdbcStr = dorisUrl;
        if (!res.data.groupConfig.newApp && res.data.targetDsConfig != null) {
            targetDbPoolConfig.componentId = res.data.targetDsConfig.componentId;
        }
        res.data.targetDsConfig = targetDbPoolConfig;

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
            log.info("【应用id】" + config.groupConfig.componentId);
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
            BuildDbControllerServiceDTO targetDto = buildDbControllerServiceDTO(config, groupId, DbPoolTypeEnum.TARGET);
            BusinessResult<ControllerServiceEntity> targetRes = componentsBuild.buildDbControllerService(targetDto);

            BuildDbControllerServiceDTO sourceDto = buildDbControllerServiceDTO(config, groupId, DbPoolTypeEnum.SOURCE);
            BusinessResult<ControllerServiceEntity> sourceRes = componentsBuild.buildDbControllerService(sourceDto);

            if (targetRes.success && sourceRes.success) {
                list.add(sourceRes.data);
                list.add(targetRes.data);
                return list;
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, "【target】" + targetRes.msg + ",【source】" + sourceRes.msg);
            }
        } else {
            ControllerServiceEntity sourceRes = componentsBuild.getDbControllerService(config.sourceDsConfig.getComponentId());
            ControllerServiceEntity targetRes = componentsBuild.getDbControllerService(config.targetDsConfig.getComponentId());
            if (targetRes != null && sourceRes != null) {
                list.add(sourceRes);
                list.add(targetRes);
                return list;
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_NO_COMPONENTS_FOUND);
            }
        }
    }

    /**
     * 创建控制器服务对象
     *
     * @param config  数据接入配置
     * @param groupId 组id
     * @param type    数据源类型
     * @return dto
     */
    private BuildDbControllerServiceDTO buildDbControllerServiceDTO(DataAccessConfigDTO config, String groupId, DbPoolTypeEnum type) {
        DataSourceConfig dsConfig;
        String name;
        switch (type) {
            case SOURCE:
                dsConfig = config.sourceDsConfig;
                name = "Source Data DB Connection";
                break;
            case TARGET:
                dsConfig = config.targetDsConfig;
                name = "Target Data DB Connection";
                break;
            case CONFIG:
                dsConfig = config.cfgDsConfig;
                name = "Config Data DB Connection";
                break;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
        BuildDbControllerServiceDTO dto = new BuildDbControllerServiceDTO();
        dto.conUrl = dsConfig.jdbcStr;
        dto.driverName = dsConfig.type.getName();
        dto.user = dsConfig.user;
        dto.pwd = dsConfig.password;
        dto.enabled = true;
        dto.groupId = groupId;
        dto.name = name;
        dto.details = dto.name;
        switch (dsConfig.type) {
            case MYSQL:
                dto.driverLocation = NifiConstants.DriveConstants.MYSQL_DRIVE_PATH;
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
     * @param cfgDbPoolId    增量配置库id
     */
    private List<ProcessorEntity> buildProcessor(DataAccessConfigDTO config, String groupId, String sourceDbPoolId, String targetDbPoolId, String cfgDbPoolId) {
        //读取增量字段组件
        ProcessorEntity queryField = queryIncrementFieldProcessor(config, groupId, cfgDbPoolId);
        //创建数据转换json组件
        ProcessorEntity jsonRes = convertJsonProcessor(groupId, 2);
        //连接器
        componentConnector(groupId, queryField.getId(), jsonRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //字段转换nifi变量
        ProcessorEntity evaluateJson = evaluateJsonPathProcessor(groupId);
        //连接器
        componentConnector(groupId, jsonRes.getId(), evaluateJson.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //创建log
        ProcessorEntity logProcessor = putLogProcessor(groupId, cfgDbPoolId, config.processorConfig.targetTableName);
        //连接器
        componentConnector(groupId, evaluateJson.getId(), logProcessor.getId(), AutoEndBranchTypeEnum.MATCHED);
        //创建执行删除组件
        ProcessorEntity delSqlRes = execDeleteSqlProcessor(config, groupId, targetDbPoolId);
        //连接器
        componentConnector(groupId, logProcessor.getId(), delSqlRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //创建查询组件
        ProcessorEntity querySqlRes = execSqlProcessor(config, groupId, sourceDbPoolId);
        //连接器
        componentConnector(groupId, delSqlRes.getId(), querySqlRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //创建数据转换json组件
        ProcessorEntity toJsonRes = convertJsonProcessor(groupId, 7);
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
        //清除sql参数组件
        ProcessorEntity clearRes = execClearParameterProcessor(groupId);
        //连接器
        componentConnector(groupId, putSqlRes.getId(), clearRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //创建执行存储过程组件
        ProcessorEntity procedureRes = execProcedureProcessor(config.processorConfig.targetTableName, groupId, cfgDbPoolId);
        //连接器
        componentConnector(groupId, clearRes.getId(), procedureRes.getId(), AutoEndBranchTypeEnum.SUCCESS);


        List<ProcessorEntity> res = new ArrayList<>();
        res.add(queryField);
        res.add(jsonRes);
        res.add(evaluateJson);
        res.add(logProcessor);
        res.add(delSqlRes);
        res.add(querySqlRes);
        res.add(toJsonRes);
        res.add(toSqlRes);
        res.add(putSqlRes);
        res.add(clearRes);
        res.add(procedureRes);
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
     * 清除sql参数组件
     *
     * @param groupId     组id
     * @return 组件对象
     */
    private ProcessorEntity execClearParameterProcessor(String groupId) {
        BuildUpdateAttributeDTO dto = new BuildUpdateAttributeDTO();
        dto.name = "Clear SQL Parameter";
        dto.details = "Clear SQL Parameter";
        dto.groupId = groupId;
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(10);
        BusinessResult<ProcessorEntity> res = componentsBuild.buildUpdateAttribute(dto);
        verifyProcessorResult(res);
        return res.data;
    }

    /**
     * 执行存储过程组件
     *
     * @param tableName   表明
     * @param groupId     组id
     * @param cfgDbPoolId 配置库连接池id
     * @return 组件对象
     */
    private ProcessorEntity execProcedureProcessor(String tableName, String groupId, String cfgDbPoolId) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Exec Mysql Procedure";
        querySqlDto.details = "Execute Mysql Procedure Update Log";
        querySqlDto.groupId = groupId;
        querySqlDto.querySql = " CALL nifi_update_etl_log ('" + tableName + "', 2) ";
        querySqlDto.dbConnectionId = cfgDbPoolId;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(11);
        List<String> autoEnd =  new ArrayList<String>();
        autoEnd.add(AutoEndBranchTypeEnum.SUCCESS.getName());
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, autoEnd);
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    /**
     * 插入日志组件
     *
     * @param groupId  组id
     * @param dbPoolId 连接池id
     * @param tableName 表明
     * @return 组件对象
     */
    private ProcessorEntity putLogProcessor(String groupId, String dbPoolId, String tableName) {
        BuildPutSqlProcessorDTO putSqlDto = new BuildPutSqlProcessorDTO();
        putSqlDto.name = "Put Log to Config Db";
        putSqlDto.details = "Put Log to Config Db";
        putSqlDto.groupId = groupId;
        putSqlDto.dbConnectionId = dbPoolId;
        putSqlDto.sqlStatement = buildLogSql(tableName);
        putSqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(4);

        BusinessResult<ProcessorEntity> putSqlRes = componentsBuild.buildPutSqlProcess(putSqlDto);
        verifyProcessorResult(putSqlRes);
        return putSqlRes.data;
    }

    /**
     * 执行sql组件
     *
     * @param groupId  组id
     * @param dbPoolId 连接池id
     * @return 组件对象
     */
    private ProcessorEntity putSqlProcessor(String groupId, String dbPoolId) {
        BuildPutSqlProcessorDTO putSqlDto = new BuildPutSqlProcessorDTO();
        putSqlDto.name = "Put sql to target data source";
        putSqlDto.details = "Put sql to target data source";
        putSqlDto.groupId = groupId;
        putSqlDto.dbConnectionId = dbPoolId;
        putSqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(9);
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
        toSqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(8);
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
        querySqlDto.querySql = config.processorConfig.sourceExecSqlQuery + " where time >= '${IncrementStart}' and time <= '${IncrementEnd}' ";
        querySqlDto.dbConnectionId = sourceDbPoolId;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(6);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<String>());
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
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(5);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<String>());
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    /**
     * 执行sql 查询增量字段组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity evaluateJsonPathProcessor(String groupId) {
        BuildProcessEvaluateJsonPathDTO dto = new BuildProcessEvaluateJsonPathDTO();
        dto.name = "Set Increment Field";
        dto.details = "Set Increment Field to Nifi Data flow";
        dto.groupId = groupId;
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(3);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildEvaluateJsonPathProcess(dto);
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    /**
     * 执行sql 查询增量字段组件
     *
     * @param config      数据接入配置
     * @param groupId     组id
     * @param cfgDbPoolId 增量配置库连接池id
     * @return 组件对象
     */
    private ProcessorEntity queryIncrementFieldProcessor(DataAccessConfigDTO config, String groupId, String cfgDbPoolId) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Query Increment Field";
        querySqlDto.details = "Query Increment Field in the data source";
        querySqlDto.groupId = groupId;
        querySqlDto.querySql = buildIncrementSql(config.processorConfig.targetTableName);
        querySqlDto.dbConnectionId = cfgDbPoolId;
        querySqlDto.scheduleExpression = config.processorConfig.scheduleExpression;
        querySqlDto.scheduleType = config.processorConfig.scheduleType;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(1);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<String>());
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
        componentsBuild.enabledProcessor(groupId, processors);
    }

    /**
     * 回写组件id
     *
     * @param appId            应用id
     * @param appComponentId   应用组id
     * @param tableId          物理表id
     * @param tableComponentId 任务组id
     */
    private void writeBackComponentId(long appId, String appComponentId, long tableId, String tableComponentId, String sourceDbPoolComponentId, String targetDbPoolComponentId, String cfgDbPoolComponentId) {
        NifiAccessDTO dto = new NifiAccessDTO();
        dto.appid = appId;
        dto.appGroupId = appComponentId;
        dto.tableId = tableId;
        dto.tableGroupId = tableComponentId;
        dto.targetDbPoolComponentId = targetDbPoolComponentId;
        dto.sourceDbPoolComponentId = sourceDbPoolComponentId;
        dto.cfgDbPoolComponentId = cfgDbPoolComponentId;
        client.addComponentId(dto);
    }

    /**
     * 创建增量字段查询sql
     *
     * @param targetDbName 目标表名称
     * @return sql
     */
    private String buildIncrementSql(String targetDbName) {
        StringBuilder str = new StringBuilder();
        str.append("select ");
        str.append(NifiConstants.AttrConstants.INCREMENT_DB_FIELD_START).append(" as ").append(NifiConstants.AttrConstants.INCREMENT_START).append(", ");
        str.append(NifiConstants.AttrConstants.INCREMENT_DB_FIELD_END).append(" as ").append(NifiConstants.AttrConstants.INCREMENT_END).append(", ");
        str.append("uuid()").append(" as ").append(NifiConstants.AttrConstants.LOG_CODE);
        str.append(" from ").append(NifiConstants.AttrConstants.INCREMENT_DB_TABLE_NAME);
        str.append(" where object_name = '").append(targetDbName).append("' and enable_flag = 1");
        return str.toString();
    }

    /**
     * 创建日志sql
     *
     * @return sql
     */
    private String buildLogSql(String tableName) {
        return "INSERT INTO tb_etl_log ( tablename, startdate, `status`, code) VALUES ('" + tableName + "', now(), 1, '${" + NifiConstants.AttrConstants.LOG_CODE + "}')";
    }

    /**
     * 获取/创建数据接入配置库的连接池
     *
     * @param config 数据接入配置
     * @return 组件实体
     */
    private ControllerServiceEntity buildCfgDsPool(DataAccessConfigDTO config) {
        String groupId = NifiConstants.ApiConstants.ROOT_NODE;
        if (StringUtils.isNotEmpty(config.cfgDsConfig.componentId)) {
            ControllerServiceEntity cfgRes = componentsBuild.getDbControllerService(config.cfgDsConfig.getComponentId());
            if (cfgRes == null) {
                throw new FkException(ResultEnum.TASK_NIFI_NO_COMPONENTS_FOUND);
            }
            return cfgRes;
        } else {
            BuildDbControllerServiceDTO targetDto = buildDbControllerServiceDTO(config, groupId, DbPoolTypeEnum.CONFIG);
            BusinessResult<ControllerServiceEntity> targetRes = componentsBuild.buildDbControllerService(targetDto);
            if (targetRes.success) {
                return targetRes.data;
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, targetRes.msg);
            }
        }
    }
}
