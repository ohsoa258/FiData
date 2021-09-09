package com.fisk.task.consumer.nifi;

import com.alibaba.fastjson.JSON;
import com.davis.client.ApiException;
import com.davis.client.model.*;
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
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.NifiPositionHelper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author gy
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_NIFI_FLOW)
@Slf4j
public class BuildNifiTaskListener {

    @Value("${datamodeldorisconstr.url}")
    private String dorisUrl;
    @Value("${datamodeldorisconstr.username}")
    private String dorisUser;
    @Value("${datamodeldorisconstr.password}")
    private String dorisPwd;
    @Value("${pgsql-datainput.url}")
    private String pgsqlDatainputUrl;
    @Value("${pgsql-datainput.username}")
    private String pgsqlDatainputUsername;
    @Value("${pgsql-datainput.password}")
    private String pgsqlDatainputPassword;

    @Value("${spring.rabbitmq.host}")
    private String host;
    @Value("${spring.rabbitmq.port}")
    private String port;
    @Value("${spring.rabbitmq.username}")
    private String username;
    @Value("${spring.rabbitmq.password}")
    private String password;
    @Value("${spring.rabbitmq.virtual-host}")
    private String vhost;
    @Value("${nifi-MaxRowsPerFlowFile}")
    public String MaxRowsPerFlowFile;
    @Value("${nifi-OutputBatchSize}")
    public String OutputBatchSize;
    @Value("${nifi-FetchSize}")
    public String FetchSize;
    @Value("${nifi-ConcurrentTasks}")
    public String ConcurrentTasks;

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
        List<ProcessorEntity> processors = buildProcessorVersion2(configDTO, taskGroupEntity.getId(), dbPool.get(0).getId(), dbPool.get(1).getId(), cfgDbPool.getId());
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
        targetDbPoolConfig.type = DriverTypeEnum.POSTGRESQL;
        targetDbPoolConfig.user = pgsqlDatainputUsername;
        targetDbPoolConfig.password = pgsqlDatainputPassword;
        targetDbPoolConfig.jdbcStr = pgsqlDatainputUrl;
        targetDbPoolConfig.targetTableName=res.data.targetDsConfig.targetTableName;
        targetDbPoolConfig.tableFieldsList=res.data.targetDsConfig.tableFieldsList;
        System.out.println("第一次拿到list长度"+targetDbPoolConfig.tableFieldsList.size());
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
        dto.groupId = groupId;
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
            case SQLSERVER:
                dto.driverLocation = NifiConstants.DriveConstants.SQLSERVER_DRIVE_PATH;
                break;
            case POSTGRESQL:
                dto.driverLocation = NifiConstants.DriveConstants.POSTGRESQL_DRIVE_PATH;
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
        //SplitJson  json拆分
        ProcessorEntity processorEntity = splitJsonProcessor(groupId);
        componentConnector(groupId,toJsonRes.getId(),processorEntity.getId(),AutoEndBranchTypeEnum.SUCCESS);
        //EvaluateJsonPath+ReplaceText 用来转sql语句
        ProcessorEntity sqlParameterProcessor=sqlParameterProcessor(config,groupId);
        componentConnector(groupId,processorEntity.getId(),sqlParameterProcessor.getId(),AutoEndBranchTypeEnum.SPLIT);
        //转sql
        ProcessorEntity assembleSql=assembleSql(config,groupId);
        //连接器
        componentConnector(groupId, sqlParameterProcessor.getId(), assembleSql.getId(), AutoEndBranchTypeEnum.MATCHED);
        //创建执行sql组件
        ProcessorEntity putSqlRes = putSqlProcessor(groupId, targetDbPoolId);
        //连接器
        componentConnector(groupId, assembleSql.getId(), putSqlRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //合并流文件组件
        ProcessorEntity mergeRes = mergeContentProcessor(groupId);
        //连接器
        componentConnector(groupId, putSqlRes.getId(), mergeRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
       //用组件,调存储过程把stg里的数据向ods里面插入
        ProcessorEntity processorEntity1 = CallDbProcedure(config, groupId);
        componentConnector(groupId, mergeRes.getId(), processorEntity1.getId(), AutoEndBranchTypeEnum.MERGED);
        // 用组件然后再调存储过程写日志

        List<ProcessorEntity> res = new ArrayList<>();
        res.add(queryField);
        res.add(jsonRes);
        res.add(evaluateJson);
        res.add(logProcessor);
        res.add(delSqlRes);
        res.add(querySqlRes);
        res.add(toJsonRes);
        res.add(processorEntity);
        res.add(sqlParameterProcessor);
        res.add(assembleSql);
        res.add(putSqlRes);
        res.add(mergeRes);
        res.add(processorEntity1);
        return res;
    }
    private List<ProcessorEntity> buildProcessorVersion2(DataAccessConfigDTO config, String groupId, String sourceDbPoolId, String targetDbPoolId, String cfgDbPoolId) {
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
        //执行查询组件
        ProcessorEntity executeSQLRecord = createExecuteSQLRecord(config,groupId);
        //连接器
        componentConnector(groupId, delSqlRes.getId(), executeSQLRecord.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //数据入库
        ProcessorEntity putDatabaseRecord = createPutDatabaseRecord(config,groupId);
        //连接器
        componentConnector(groupId, executeSQLRecord.getId(), putDatabaseRecord.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //合并流文件组件
        ProcessorEntity mergeRes = mergeContentProcessor(groupId);
        //连接器
        componentConnector(groupId, putDatabaseRecord.getId(), mergeRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //用组件,调存储过程把stg里的数据向ods里面插入
        ProcessorEntity processorEntity1 = CallDbProcedure(config, groupId);
        componentConnector(groupId, mergeRes.getId(), processorEntity1.getId(), AutoEndBranchTypeEnum.MERGED);

        List<ProcessorEntity> res = new ArrayList<>();
        res.add(queryField);
        res.add(jsonRes);
        res.add(evaluateJson);
        res.add(logProcessor);
        res.add(delSqlRes);
        res.add(executeSQLRecord);
        res.add(putDatabaseRecord);
        res.add(mergeRes);
        res.add(processorEntity1);
        return res;
    }

    private ProcessorEntity createExecuteSQLRecord(DataAccessConfigDTO config,String groupId){
        BaseProcessorDTO data=new BaseProcessorDTO();
        data.details="AvroRecordSetWriter";
        data.name="AvroRecordSetWriter";
        data.groupId=groupId;
        String id="";
        //创建buildAvroRecordSetWriterService
        BusinessResult<ControllerServiceEntity> controllerServiceEntityBusinessResult = componentsBuild.buildAvroRecordSetWriterService(data);
        if(controllerServiceEntityBusinessResult.success){
             id = controllerServiceEntityBusinessResult.data.getId();
        } else {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, controllerServiceEntityBusinessResult.msg);
        }

        ExecuteSQLRecordDTO executeSQLRecordDTO = new ExecuteSQLRecordDTO();
        executeSQLRecordDTO.name = "executeSQLRecord";
        executeSQLRecordDTO.details = "executeSQLRecord";
        executeSQLRecordDTO.groupId = groupId;
        executeSQLRecordDTO.FetchSize=FetchSize;
        executeSQLRecordDTO.maxRowsPerFlowFile=MaxRowsPerFlowFile;
        executeSQLRecordDTO.outputBatchSize=OutputBatchSize;
        executeSQLRecordDTO.databaseConnectionPoolingService=config.sourceDsConfig.componentId;
        executeSQLRecordDTO.sqlSelectQuery=config.processorConfig.sourceExecSqlQuery;
        executeSQLRecordDTO.recordwriter=id;
        executeSQLRecordDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(6);
        BusinessResult<ProcessorEntity> res = componentsBuild.buildExecuteSQLRecordProcess(executeSQLRecordDTO);
        verifyProcessorResult(res);
        return res.data;
    }

    private ProcessorEntity createPutDatabaseRecord(DataAccessConfigDTO config,String groupId){
        BaseProcessorDTO data=new BaseProcessorDTO();
        data.details="PutDatabaseRecord";
        data.name="PutDatabaseRecord";
        data.groupId=groupId;
        String id="";
        //创建buildAvroReaderService
        BusinessResult<ControllerServiceEntity> controllerServiceEntityBusinessResult = componentsBuild.buildAvroReaderService(data);
        if(controllerServiceEntityBusinessResult.success){
            id = controllerServiceEntityBusinessResult.data.getId();
        } else {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, controllerServiceEntityBusinessResult.msg);
        }
        PutDatabaseRecordDTO putDatabaseRecordDTO = new PutDatabaseRecordDTO();
        putDatabaseRecordDTO.name = "executeSQLRecord";
        putDatabaseRecordDTO.details = "executeSQLRecord";
        putDatabaseRecordDTO.groupId = groupId;
        putDatabaseRecordDTO.databaseConnectionPoolingService=config.targetDsConfig.componentId;
        putDatabaseRecordDTO.databaseType="MS SQL 2012+";//数据库类型,定义枚举
        putDatabaseRecordDTO.recordReader=id;
        putDatabaseRecordDTO.statementType="INSERT";
        putDatabaseRecordDTO.TableName="stg_"+config.processorConfig.targetTableName.toLowerCase();
        putDatabaseRecordDTO.concurrentTasks=ConcurrentTasks;
        putDatabaseRecordDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(7);
        BusinessResult<ProcessorEntity> res = componentsBuild.buildPutDatabaseRecordProcess(putDatabaseRecordDTO);
        verifyProcessorResult(res);
        return res.data;
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
     * 合并流文件 组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity mergeContentProcessor(String groupId) {
        BuildMergeContentProcessorDTO dto = new BuildMergeContentProcessorDTO();
        dto.name = "Merge Content";
        dto.details = "Merges a Group of FlowFiles together based on a user-defined strategy and packages them into a single FlowFile";
        dto.groupId = groupId;
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(8);

        BusinessResult<ProcessorEntity> res = componentsBuild.buildMergeContentProcess(dto);
        verifyProcessorResult(res);
        return res.data;
    }

    /**
     * 拼接json参数 组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity replaceTextProcessor(DataAccessConfigDTO config,String groupId) {
        BuildReplaceTextProcessorDTO dto = new BuildReplaceTextProcessorDTO();
        dto.name = "Build MQ Message";
        dto.details = "build json string";
        dto.groupId = groupId;
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(12);
        dto.replacementValue = "{ \"code\": \"${" + NifiConstants.AttrConstants.LOG_CODE + "}\" "+","+"\"corn\":\""+config.processorConfig.scheduleExpression+"\"}";

        BusinessResult<ProcessorEntity> res = componentsBuild.buildReplaceTextProcess(dto);
        verifyProcessorResult(res);
        return res.data;
    }

    /**
     * 发送mq消息 组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity pulishMqProcessor(String groupId) {
        BuildPublishMqProcessorDTO dto = new BuildPublishMqProcessorDTO();
        dto.name = "Put Log to Config Db";
        dto.details = "Put Log to Config Db";
        dto.groupId = groupId;
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(13);
        dto.host = host;
        dto.port = port;
        dto.exchange = MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME;
        dto.route = MqConstants.RouterConstants.INCREMENT_RESULT;
        dto.vhost = vhost;
        dto.user = username;
        dto.pwd = password;

        BusinessResult<ProcessorEntity> res = componentsBuild.buildPublishMqProcess(dto);
        verifyProcessorResult(res);
        return res.data;
    }

    /**
     * 插入日志组件
     *
     * @param groupId   组id
     * @param dbPoolId  连接池id
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
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Put sql to target data source";
        querySqlDto.details = "Put sql to target data source";
        querySqlDto.groupId = groupId;
        querySqlDto.dbConnectionId = dbPoolId;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(11);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<String>());
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }
    /*
    SplitJson
    */
    private ProcessorEntity splitJsonProcessor(String groupId){
        BuildSplitJsonProcessorDTO buildSplitJsonProcessorDTO = new BuildSplitJsonProcessorDTO();
        buildSplitJsonProcessorDTO.name = "SplitJson";
        buildSplitJsonProcessorDTO.details = "SplitJson";
        buildSplitJsonProcessorDTO.groupId = groupId;
        buildSplitJsonProcessorDTO.positionDTO=NifiPositionHelper.buildYPositionDTO(8);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildSplitJsonProcess(buildSplitJsonProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }
    private ProcessorEntity sqlParameterProcessor(DataAccessConfigDTO config,String groupId){
        BuildProcessEvaluateJsonPathDTO dto = new BuildProcessEvaluateJsonPathDTO();
        dto.name = "sqlParameterProcessor";
        dto.details = "sqlParameterProcessor";
        dto.groupId = groupId;
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(9);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult =componentsBuild.buildSqlParameterProcess(config,dto);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }
       private ProcessorEntity assembleSql (DataAccessConfigDTO config,String groupId){
           BuildReplaceTextProcessorDTO  dto = new BuildReplaceTextProcessorDTO();
        dto.name = "assembleSql";
        dto.details = "assembleSql";
        dto.groupId = groupId;
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(10);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult =componentsBuild.buildAssembleSqlProcess(config,dto);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    private ProcessorEntity CallDbProcedure(DataAccessConfigDTO config,String groupId){
        BuildCallDbProcedureProcessorDTO callDbProcedureProcessorDTO = new BuildCallDbProcedureProcessorDTO();
        callDbProcedureProcessorDTO.name = "CallDbProcedure";
        callDbProcedureProcessorDTO.details = "CallDbProcedure";
        callDbProcedureProcessorDTO.groupId = groupId;
        String executsql="";
        config.processorConfig.targetTableName="stg_"+config.processorConfig.targetTableName;
        String stg_TableName = config.processorConfig.targetTableName.toLowerCase();
        String ods_TableName = config.processorConfig.targetTableName.replaceAll("stg_","ods_").toLowerCase();
        String syncMode= config.cfgDsConfig.syncMode==1?"full_volume":"timestamp_incremental";
        System.out.println("同步类型为:"+syncMode+config.cfgDsConfig.syncMode);
        executsql="select public.data_stg_to_ods ('"+stg_TableName+"','"+ods_TableName+"','"+syncMode+"','${" + NifiConstants.AttrConstants.LOG_CODE + "}'"+")";
        callDbProcedureProcessorDTO.dbConnectionId=config.targetDsConfig.componentId;
        callDbProcedureProcessorDTO.executsql=executsql;
        callDbProcedureProcessorDTO.positionDTO=NifiPositionHelper.buildYPositionDTO(9);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildCallDbProcedureProcess(callDbProcedureProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }
    private ProcessorEntity  CallDbLogProcedure(DataAccessConfigDTO config,String groupId){
        BuildCallDbProcedureProcessorDTO callDbProcedureProcessorDTO = new BuildCallDbProcedureProcessorDTO();
        callDbProcedureProcessorDTO.name = "CallDbLogProcedure";
        callDbProcedureProcessorDTO.details = "CallDbLogProcedure";
        callDbProcedureProcessorDTO.groupId = groupId;
        //调用存储过程sql,存日志
        String cronNextTime = "";
        CronSequenceGenerator cron = null;
        cron = new CronSequenceGenerator(config.processorConfig.scheduleExpression);
        Date d = new Date();
        Date date = cron.next(d);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cronNextTime = sdf.format(date);
        String executsql="call nifi_update_etl_log_and_Incremental(";
        executsql+=config.targetDsConfig.targetTableName.toLowerCase()+",2,'${"+NifiConstants.AttrConstants.LOG_CODE+"}','${"+NifiConstants.AttrConstants.INCREMENT_END+"}',"+cronNextTime+")";
        callDbProcedureProcessorDTO.dbConnectionId=config.cfgDsConfig.componentId;
        callDbProcedureProcessorDTO.executsql=executsql;
        callDbProcedureProcessorDTO.positionDTO=NifiPositionHelper.buildYPositionDTO(14);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildCallDbProcedureProcess(callDbProcedureProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
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
        toSqlDto.tableName = config.processorConfig.targetTableName.toLowerCase();
        toSqlDto.sqlType = StatementSqlTypeEnum.INSERT;
        toSqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(9);
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
        //+ " where time >= '${IncrementStart}' and time <= '${IncrementEnd}' "先把时间段去掉
        querySqlDto.querySql = config.processorConfig.sourceExecSqlQuery;
        querySqlDto.dbConnectionId = sourceDbPoolId;
        //querySqlDto.fetchSize="2";
        querySqlDto.MaxRowsPerFlowFile="1000000";
        //querySqlDto.outputBatchSize="2";
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
        querySqlDto.querySql = "TRUNCATE table " + "stg_"+config.processorConfig.targetTableName;
        querySqlDto.dbConnectionId = targetDbPoolId;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(5);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<String>());
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    /**
     * 创建变量组件
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
        dto.appId = appId;
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
