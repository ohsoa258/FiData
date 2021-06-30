package com.fisk.task.consumer.nifi;

import com.alibaba.fastjson.JSON;
import com.davis.client.model.ConnectionEntity;
import com.davis.client.model.ControllerServiceEntity;
import com.davis.client.model.ProcessGroupEntity;
import com.davis.client.model.ProcessorEntity;
import com.fisk.common.constants.MQConstants;
import com.fisk.common.constants.NifiConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.nifi.AutoEndBranchTypeEnum;
import com.fisk.common.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.common.enums.task.nifi.StatementSqlTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.daconfig.DataSourceConfig;
import com.fisk.task.dto.nifi.*;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.INifiComponentsBuild;
import com.fisk.task.utils.NifiPositionHelper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gy
 */
@Component
@RabbitListener(queues = MQConstants.QueueConstants.BUILD_NIFI_FLOW)
@Slf4j
public class BuildNifiTaskListener {

    @Resource
    INifiComponentsBuild componentsBuild;

    @RabbitHandler
    @MQConsumerLog
    public void msg(String data, Channel channel, Message message) {
        BuildNifiFlowDTO dto = JSON.parseObject(data, BuildNifiFlowDTO.class);
        //获取数据接入配置项
        DataAccessConfigDTO configDTO = getConfigData(dto.appId);

        //1. 创建应用
        ProcessGroupEntity groupEntity = buildAppGroup(configDTO);
        //2. 创建jdbc连接池
        List<ControllerServiceEntity> dbPool = buildDsConnectionPool(configDTO, groupEntity.getId());
        //3. 创建组件
        List<ProcessorEntity> processors = buildProcessor(configDTO, groupEntity.getId(), dbPool.get(0).getId(), dbPool.get(1).getId());
        //4. 启动组件
        enabledProcessor(groupEntity.getId(), processors);
    }

    /**
     * 获取数据接入的配置
     *
     * @param appId 配置的id
     * @return 数据接入配置
     */
    private DataAccessConfigDTO getConfigData(long appId) {
        DataAccessConfigDTO dto = new DataAccessConfigDTO();
        dto.appName = "Rabbit Consumer Build Nifi Data Flow";
        dto.appDetails = "...";
        dto.newApp = true;
        DataSourceConfig config1 = new DataSourceConfig();
        config1.type = DriverTypeEnum.MYSQL;
        config1.user = "root";
        config1.password = "root123";
        config1.jdbcStr = "jdbc:mysql://192.168.11.130:3306/dmp_chartvisual_db";
        dto.sourceDsConfig = config1;
        DataSourceConfig config2 = new DataSourceConfig();
        config2.type = DriverTypeEnum.MYSQL;
        config2.user = "root";
        config2.password = "Password01!";
        config2.jdbcStr = "jdbc:mysql://192.168.11.134:9030/test_db";
        dto.targetDsConfig = config2;
        dto.scheduleType = SchedulingStrategyTypeEnum.CRON;
        dto.scheduleExpression = "0/30 * * * * ?";
        dto.sourceExecSqlQuery = "select * from tb_test_data";
        dto.targetTableName = "tb_test_data";
        return dto;
    }

    /**
     * 创建app组
     *
     * @param config 数据接入配置
     * @return 组信息
     */
    private ProcessGroupEntity buildAppGroup(DataAccessConfigDTO config) {
        //判断是否需要新建组
        if (config.newApp) {
            BuildProcessGroupDTO dto = new BuildProcessGroupDTO();
            dto.name = config.appName;
            dto.details = config.appDetails;
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
            BusinessResult<ProcessGroupEntity> res = componentsBuild.getProcessGroupById(config.appDetails);
            if (res.success) {
                return res.data;
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
            }
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
        if (config.newApp) {
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
        //创建查询组件
        ProcessorEntity querySqlRes = execSqlProcessor(config, groupId, sourceDbPoolId);
        //创建数据转换json组件
        ProcessorEntity toJsonRes = convertJsonProcessor(groupId);
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
        res.add(querySqlRes);
        res.add(toJsonRes);
        res.add(toSqlRes);
        res.add(putSqlRes);
        return res;
    }

    /**
     * 组件连接器
     * @param groupId 组id
     * @param sourceId 连接器上方组件id
     * @param targetId 连接器下方组件id
     * @param type 连接类型
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
        putSqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(4);
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
        toSqlDto.tableName = config.targetTableName;
        toSqlDto.sqlType = StatementSqlTypeEnum.INSERT;
        toSqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(3);
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
    private ProcessorEntity convertJsonProcessor(String groupId) {
        BuildConvertToJsonProcessorDTO toJsonDto = new BuildConvertToJsonProcessorDTO();
        toJsonDto.name = "Convert Data To Json";
        toJsonDto.details = "Convert data source to json";
        toJsonDto.groupId = groupId;
        toJsonDto.positionDTO = NifiPositionHelper.buildYPositionDTO(2);
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
        querySqlDto.querySql = config.sourceExecSqlQuery;
        querySqlDto.dbConnectionId = sourceDbPoolId;
        querySqlDto.scheduleExpression = config.scheduleExpression;
        querySqlDto.scheduleType = config.scheduleType;
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
        if (enableRes.size() != processors.size()) {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR);
        }
    }
}
