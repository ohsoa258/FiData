package com.fisk.task.listener.nifi.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.davis.client.ApiException;
import com.davis.client.model.ProcessorRunStatusEntity;
import com.davis.client.model.*;
import com.fisk.common.core.baseObject.entity.BusinessResult;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.constants.NifiConstants;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.enums.task.FuncNameEnum;
import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.core.enums.task.nifi.*;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.sftp.SftpUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.consumeserveice.client.ConsumeServeiceClient;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.dataaccess.dto.access.NifiAccessDTO;
import com.fisk.dataaccess.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.dataaccess.dto.table.TableBusinessDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.dataaccess.enums.*;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.enums.TableServicePublicStatusEnum;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckSyncDTO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.syncmode.GetTableBusinessDTO;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.dataservice.dto.tableapi.TableApiSyncDTO;
import com.fisk.dataservice.dto.tableservice.TableServicePublishStatusDTO;
import com.fisk.dataservice.dto.tablesyncmode.TableSyncModeDTO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.daconfig.*;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.task.dto.nifi.*;
import com.fisk.task.dto.task.*;
import com.fisk.task.entity.TBETLIncrementalPO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.enums.PortComponentEnum;
import com.fisk.task.listener.nifi.INifiTaskListener;
import com.fisk.task.listener.postgre.datainput.IbuildTable;
import com.fisk.task.listener.postgre.datainput.impl.BuildFactoryHelper;
import com.fisk.task.mapper.TBETLIncrementalMapper;
import com.fisk.task.po.AppNifiSettingPO;
import com.fisk.task.po.NifiConfigPO;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.service.nifi.impl.AppNifiSettingServiceImpl;
import com.fisk.task.service.nifi.impl.TableNifiSettingServiceImpl;
import com.fisk.task.service.pipeline.ITableTopicService;
import com.fisk.task.service.pipeline.impl.NifiConfigServiceImpl;
import com.fisk.task.utils.*;
import com.fisk.task.utils.nifi.AvroSchemaGeneratorUtils;
import com.fisk.task.utils.nifi.INiFiHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gy
 */
@Component
@Slf4j
public class BuildNifiTaskListener implements INifiTaskListener {

    @Value("${datamodeldorisconstr.url}")
    private String dorisUrl;
    @Value("${datamodeldorisconstr.username}")
    private String dorisUser;
    @Value("${datamodeldorisconstr.password}")
    private String dorisPwd;
    private String pgsqlDatainputDbName;

    @Value("${nifi-MaxRowsPerFlowFile}")
    public String MaxRowsPerFlowFile;
    @Value("${nifi-OutputBatchSize}")
    public String OutputBatchSize;
    @Value("${nifi-FetchSize}")
    public String FetchSize;
    @Value("${nifi-ConcurrentTasks}")
    public String ConcurrentTasks;
    @Value("${spring.kafka.producer.bootstrap-servers}")
    public String KafkaBrokers;
    @Value("${nifi.pipeline.topicName}")
    public String pipelineTopicName;
    @Value("${nifi.token}")
    public String nifiToken;
    @Value("${nifi.pipeline.data-governance-url}")
    public String dataGovernanceUrl;
    @Value("${fiData-data-ods-source}")
    private String dataSourceOdsId;
    @Value("${fiData-data-dw-source}")
    private String dataSourceDwId;

    @Value("${sftp-rsa-upload.userName}")
    private String userName;
    @Value("${sftp-rsa-upload.password}")
    private String password;
    @Value("${sftp-rsa-upload.host}")
    private String host;
    @Value("${sftp-rsa-upload.port}")
    private Integer port;
    @Value("${sftp-rsa-upload.rsaPath}")
    private String rsaPath;

    @Value("${nifi-data-security-enable}")
    private boolean enable;

    @Value("${nifi-data-validation-enable}")
    private boolean dataValidation;


    @Resource
    INiFiHelper componentsBuild;
    @Resource
    DataAccessClient client;
    @Resource
    DataModelClient dataModelClient;
    @Resource
    AppNifiSettingServiceImpl appNifiSettingService;
    @Resource
    NifiConfigServiceImpl nifiConfigService;
    @Resource
    TableNifiSettingServiceImpl tableNifiSettingService;
    @Resource
    private TBETLIncrementalMapper incrementalMapper;
    @Resource
    private ITableTopicService tableTopicService;
    @Resource
    KafkaTemplateHelper kafkaTemplateHelper;
    @Resource
    PublishTaskController pc;
    @Resource
    UserClient userClient;
    @Resource
    ConsumeServeiceClient consumeServeiceClient;

    @Resource
    RestTemplate httpClient;
    @Resource
    PostgreHelper postgreHelper;

    public String appParentGroupId;
    public String appGroupId;
    public String groupEntityId;
    public String taskGroupEntityId;
    public String appInputPortId;
    public String tableInputPortId;
    public String appOutputPortId;
    public String tableOutputPortId;

    @Override
    public ResultEnum buildDataServices(String dataInfo, Acknowledgment acke) {

        log.info("表服务参数:{}", dataInfo);
        BuildTableServiceDTO buildTableService = JSON.parseObject(dataInfo, BuildTableServiceDTO.class);
        // 修改表状态实体
        TableServicePublishStatusDTO dto = new TableServicePublishStatusDTO();
        try {
            TBETLIncrementalPO ETLIncremental = new TBETLIncrementalPO();
            ETLIncremental.objectName = buildTableService.schemaName + "." + buildTableService.targetTable;
            ETLIncremental.enableFlag = "1";
            ETLIncremental.incrementalObjectivescoreBatchno = UUID.randomUUID().toString();
            Map<String, Object> conditionHashMap = new HashMap<>();
            conditionHashMap.put("object_name", ETLIncremental.objectName);
            List<TBETLIncrementalPO> tbetlIncrementalPos = incrementalMapper.selectByMap(conditionHashMap);
            if (tbetlIncrementalPos != null && tbetlIncrementalPos.size() > 0) {
                log.info("此表已有同步记录,无需重复添加");
            } else {
                incrementalMapper.insert(ETLIncremental);
            }
            //先创建大组.创建的时候要判断大组是否存在
            String tableServerGroupId = getGroupId(buildTableService);
            String sourceControllerServiceId = "";
            String targetControllerServiceId = "";
            String cfgControllerServiceId = "";

            //4. 创建任务组创建时要把原任务组删掉,防止重复发布带来影响
            DataModelVO dataModelVO = new DataModelVO();
            dataModelVO.dataClassifyEnum = buildTableService.dataClassifyEnum;
            dataModelVO.delBusiness = false;
            dataModelVO.businessId = String.valueOf(buildTableService.getTableAppId());
            dataModelVO.userId = buildTableService.userId;
            DataModelTableVO dataModelTableVO = new DataModelTableVO();
            dataModelTableVO.type = buildTableService.olapTableEnum;
            List<Long> ids = new ArrayList<>();
            ids.add(buildTableService.id);
            dataModelTableVO.ids = ids;
            dataModelVO.tableServerIdList = dataModelTableVO;
            TableNifiSettingPO tableNifiSettingPO = tableNifiSettingService.query().eq("type", buildTableService.olapTableEnum.getValue()).eq("app_id", buildTableService.getTableAppId()).eq("table_access_id", buildTableService.id).one();

            if (tableNifiSettingPO != null && tableNifiSettingPO.tableComponentId != null) {
                componentsBuild.deleteNifiFlow(dataModelVO);
            }
            // 创建小组
            DataAccessConfigDTO dataAccessConfig = new DataAccessConfigDTO();
            TaskGroupConfig taskGroupConfig = new TaskGroupConfig();
            taskGroupConfig.appName = buildTableService.targetTable;
            taskGroupConfig.appDetails = buildTableService.targetTable;
            dataAccessConfig.taskGroupConfig = taskGroupConfig;
            ProcessGroupEntity processGroupEntity = buildTaskGroup(dataAccessConfig, tableServerGroupId);
            String taskGroupId = processGroupEntity.getId();
            // 依托小组,创建控制器服务
            Integer dataSourceId = buildTableService.dataSourceId;
            Integer targetDbId = buildTableService.targetDbId;
            // 来源
            sourceControllerServiceId = saveDbconfig(dataSourceId);
            // 目标
            targetControllerServiceId = saveDbconfig(targetDbId);
            NifiConfigPO cfgConfigPO = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.CFG_DB_POOL_COMPONENT_ID.getName()).one();
            if (cfgConfigPO != null) {
                cfgControllerServiceId = cfgConfigPO.componentId;
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, "未创建配置库连接池");
            }
            // 依托小组,创建组件
            TableSyncModeDTO syncMode = buildTableService.syncModeDTO;
            AppNifiSettingPO appNifiSetting = new AppNifiSettingPO();
            BuildNifiFlowDTO buildNifiFlow = new BuildNifiFlowDTO();
            buildNifiFlow.synchronousTypeEnum = SynchronousTypeEnum.TOEXTERNALDB;
            buildNifiFlow.id = buildTableService.id;
            buildNifiFlow.type = buildTableService.olapTableEnum;
            buildNifiFlow.selectSql = buildTableService.sqlScript;
            buildNifiFlow.fetchSize = syncMode.fetchSize;
            buildNifiFlow.maxRowsPerFlowFile = syncMode.maxRowsPerFlowFile;
            buildNifiFlow.appId = Long.valueOf(buildTableService.tableAppId);
            DataSourceConfig targetDsConfig = new DataSourceConfig();
            targetDsConfig.targetTableName = buildTableService.targetTable;
            targetDsConfig.tableFieldsList = JSON.parseArray(JSON.toJSONString(buildTableService.fieldDtoList), TableFieldsDTO.class);
            dataAccessConfig.targetDsConfig = targetDsConfig;
            ProcessorConfig processorConfig = new ProcessorConfig();
            //dataAccessConfig.processorConfig.targetTableName
            //关联触发
            if (Objects.equals(syncMode.triggerType, 1)) {
                //timer
                if (!Objects.equals(syncMode.scheduleType, 1)) {
                    processorConfig.scheduleExpression = syncMode.cornExpression;
                    processorConfig.scheduleType = SchedulingStrategyTypeEnum.CRON;
                } else {
                    processorConfig.scheduleExpression = syncMode.timerDriver;
                    processorConfig.scheduleType = SchedulingStrategyTypeEnum.TIMER;
                }
            }
            processorConfig.sourceExecSqlQuery = buildTableService.sqlScript;
            processorConfig.targetTableName = buildTableService.targetTable;
            dataAccessConfig.processorConfig = processorConfig;
            /**
             * 创建表服务组件！！！
             */
            List<ProcessorEntity> processorEntities = buildProcessorVersion3(taskGroupId, dataAccessConfig, taskGroupId, sourceControllerServiceId, targetControllerServiceId, cfgControllerServiceId, buildNifiFlow, buildTableService, targetDbId);

            // 启动,保存
            enabledProcessor(taskGroupId, processorEntities);

            dto.setId((int) buildTableService.id);
            // 发布成功则状态置为1
            dto.setStatus(TableServicePublicStatusEnum.PUBLIC_YES.getValue());
        } catch (Exception e) {
            // 发布失败则状态置为2
            dto.setStatus(TableServicePublicStatusEnum.PUBLIC_FAIL.getValue());
            log.error("表服务同步数据报错:{}", StackTraceHelper.getStackTraceInfo(e));
        } finally {
            if (acke != null) {
                acke.acknowledge();
            }
            try {
                log.info("开始修改表服务发布状态，参数id：[{}]，status：[{}]", dto.id, dto.status);
                consumeServeiceClient.updateTableServiceStatus(dto);
            } catch (Exception e) {
                throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
            }
        }
        return ResultEnum.SUCCESS;
    }

    private String getGroupId(BuildTableServiceDTO buildTableService) {
        NifiConfigPO nifiConfigPO = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.TABLE_SERVICE_NIFI_FLOW_GROUP_ID.getName()).one();
        if (nifiConfigPO != null) {
            AppNifiSettingPO one = appNifiSettingService.query().eq("app_id", buildTableService.tableAppId).eq("type", buildTableService.dataClassifyEnum.getValue()).eq("del_flag", 1).one();
            if (one != null) {
                return one.getAppComponentId();
            } else {
                BuildProcessGroupDTO groupDTO = new BuildProcessGroupDTO();
                groupDTO.name = buildTableService.tableAppName;
                groupDTO.details = buildTableService.tableAppDesc;
                groupDTO.groupId = nifiConfigPO.componentId;
                int count = componentsBuild.getGroupCount(nifiConfigPO.componentId);
                groupDTO.positionDTO = NifiPositionHelper.buildXPositionDTO(count);
                //创建组件
                BusinessResult<ProcessGroupEntity> res = componentsBuild.buildProcessGroup(groupDTO);
                if (res.success) {
                    AppNifiSettingPO appNifiSettingPO = new AppNifiSettingPO();
                    appNifiSettingPO.appId = String.valueOf(buildTableService.tableAppId);
                    appNifiSettingPO.appComponentId = res.data.getId();
                    appNifiSettingPO.type = buildTableService.dataClassifyEnum.getValue();
                    appNifiSettingService.save(appNifiSettingPO);
                    return res.data.getId();
                } else {
                    throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
                }
            }
        } else {
            BuildProcessGroupDTO buildProcessGroupDTO = new BuildProcessGroupDTO();
            buildProcessGroupDTO.name = ComponentIdTypeEnum.TABLE_SERVICE_NIFI_FLOW_GROUP_ID.getName();
            buildProcessGroupDTO.details = ComponentIdTypeEnum.TABLE_SERVICE_NIFI_FLOW_GROUP_ID.getName();
            int groupCount = componentsBuild.getGroupCount(NifiConstants.ApiConstants.ROOT_NODE);
            buildProcessGroupDTO.positionDTO = NifiPositionHelper.buildXPositionDTO(groupCount);
            BusinessResult<ProcessGroupEntity> processGroupEntityBusinessResult = componentsBuild.buildProcessGroup(buildProcessGroupDTO);
            if (processGroupEntityBusinessResult.success) {
                NifiConfigPO nifiConfigPO1 = new NifiConfigPO();
                nifiConfigPO1.componentId = processGroupEntityBusinessResult.data.getId();
                nifiConfigPO1.componentKey = ComponentIdTypeEnum.TABLE_SERVICE_NIFI_FLOW_GROUP_ID.getName();
                nifiConfigService.save(nifiConfigPO1);
                BuildProcessGroupDTO groupDTO = new BuildProcessGroupDTO();
                groupDTO.name = buildTableService.tableAppName;
                groupDTO.details = buildTableService.tableAppDesc;
                groupDTO.groupId = nifiConfigPO.componentId;
                int count = componentsBuild.getGroupCount(nifiConfigPO.componentId);
                groupDTO.positionDTO = NifiPositionHelper.buildXPositionDTO(count);
                //创建组件
                BusinessResult<ProcessGroupEntity> res = componentsBuild.buildProcessGroup(groupDTO);
                if (res.success) {
                    AppNifiSettingPO appNifiSettingPO = new AppNifiSettingPO();
                    appNifiSettingPO.appId = String.valueOf(buildTableService.tableAppId);
                    appNifiSettingPO.appComponentId = res.data.getId();
                    appNifiSettingPO.type = buildTableService.dataClassifyEnum.getValue();
                    appNifiSettingService.save(appNifiSettingPO);
                    return res.data.getId();
                } else {
                    throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
                }
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, processGroupEntityBusinessResult.msg);
            }
        }
    }

    private String getGroupId(BuildTableApiServiceDTO buildTableApiService) {
        NifiConfigPO nifiConfigPO = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.TABLE_SERVICE_NIFI_FLOW_GROUP_ID.getName()).one();
        if (nifiConfigPO != null) {
            AppNifiSettingPO one = appNifiSettingService.query().eq("app_id", buildTableApiService.appId).eq("type", buildTableApiService.dataClassifyEnum.getValue()).eq("del_flag", 1).one();
            if (one != null) {
                return one.getAppComponentId();
            } else {
                BuildProcessGroupDTO groupDTO = new BuildProcessGroupDTO();
                groupDTO.name = buildTableApiService.appName;
                groupDTO.details = buildTableApiService.apiDes;
                groupDTO.groupId = nifiConfigPO.componentId;
                int count = componentsBuild.getGroupCount(nifiConfigPO.componentId);
                groupDTO.positionDTO = NifiPositionHelper.buildXPositionDTO(count);
                //创建组件
                BusinessResult<ProcessGroupEntity> res = componentsBuild.buildProcessGroup(groupDTO);
                if (res.success) {
                    AppNifiSettingPO appNifiSettingPO = new AppNifiSettingPO();
                    appNifiSettingPO.appId = String.valueOf(buildTableApiService.appId);
                    appNifiSettingPO.appComponentId = res.data.getId();
                    appNifiSettingPO.type = buildTableApiService.dataClassifyEnum.getValue();
                    appNifiSettingService.save(appNifiSettingPO);
                    return res.data.getId();
                } else {
                    throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
                }
            }
        } else {
            BuildProcessGroupDTO buildProcessGroupDTO = new BuildProcessGroupDTO();
            buildProcessGroupDTO.name = ComponentIdTypeEnum.TABLE_SERVICE_NIFI_FLOW_GROUP_ID.getName();
            buildProcessGroupDTO.details = ComponentIdTypeEnum.TABLE_SERVICE_NIFI_FLOW_GROUP_ID.getName();
            int groupCount = componentsBuild.getGroupCount(NifiConstants.ApiConstants.ROOT_NODE);
            buildProcessGroupDTO.positionDTO = NifiPositionHelper.buildXPositionDTO(groupCount);
            BusinessResult<ProcessGroupEntity> processGroupEntityBusinessResult = componentsBuild.buildProcessGroup(buildProcessGroupDTO);
            if (processGroupEntityBusinessResult.success) {
                NifiConfigPO nifiConfigPO1 = new NifiConfigPO();
                nifiConfigPO1.componentId = processGroupEntityBusinessResult.data.getId();
                nifiConfigPO1.componentKey = ComponentIdTypeEnum.TABLE_SERVICE_NIFI_FLOW_GROUP_ID.getName();
                nifiConfigService.save(nifiConfigPO1);
                BuildProcessGroupDTO groupDTO = new BuildProcessGroupDTO();
                groupDTO.name = buildTableApiService.appName;
                groupDTO.details = buildTableApiService.appDesc;
                groupDTO.groupId = nifiConfigPO.componentId;
                int count = componentsBuild.getGroupCount(nifiConfigPO.componentId);
                groupDTO.positionDTO = NifiPositionHelper.buildXPositionDTO(count);
                //创建组件
                BusinessResult<ProcessGroupEntity> res = componentsBuild.buildProcessGroup(groupDTO);
                if (res.success) {
                    AppNifiSettingPO appNifiSettingPO = new AppNifiSettingPO();
                    appNifiSettingPO.appId = String.valueOf(buildTableApiService.appId);
                    appNifiSettingPO.appComponentId = res.data.getId();
                    appNifiSettingPO.type = buildTableApiService.dataClassifyEnum.getValue();
                    appNifiSettingService.save(appNifiSettingPO);
                    return res.data.getId();
                } else {
                    throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
                }
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, processGroupEntityBusinessResult.msg);
            }
        }
    }

    @Override
    public ResultEnum buildDeleteDataServices(String dataInfo, Acknowledgment acke) {
        log.info("表服务删除nifi流程参数:{}", dataInfo);
        try {
            BuildDeleteTableServiceDTO buildDeleteTableService = JSON.parseObject(dataInfo, BuildDeleteTableServiceDTO.class);
            DataModelVO dataModelVO = new DataModelVO();
            dataModelVO.delBusiness = buildDeleteTableService.delBusiness;
            DataModelTableVO dataModelTableVO = new DataModelTableVO();
            dataModelTableVO.ids = buildDeleteTableService.ids;
            dataModelTableVO.type = buildDeleteTableService.olapTableEnum;
            dataModelVO.tableServerIdList = dataModelTableVO;
            dataModelVO.businessId = buildDeleteTableService.appId;
            dataModelVO.dataClassifyEnum = DataClassifyEnum.DATASERVICES;
            dataModelVO.userId = buildDeleteTableService.userId;
            return componentsBuild.deleteNifiFlow(dataModelVO);
        } catch (Exception e) {
            log.error("表服务流程删除失败" + StackTraceHelper.getStackTraceInfo(e));
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        } finally {
            acke.acknowledge();
        }
    }

    @Override
    public ResultEnum buildDataApi(String dataInfo, Acknowledgment acke) {
        log.info("数据服务api参数:{}", dataInfo);
        BuildTableApiServiceDTO buildTableApiService = JSON.parseObject(dataInfo, BuildTableApiServiceDTO.class);
        // 修改表状态实体
        TableServicePublishStatusDTO dto = new TableServicePublishStatusDTO();
        try {
            //先创建大组.创建的时候要判断大组是否存在
            String tableServerGroupId = getGroupId(buildTableApiService);
            String cfgControllerServiceId = "";

            //4. 创建任务组创建时要把原任务组删掉,防止重复发布带来影响
            DataModelVO dataModelVO = new DataModelVO();
            dataModelVO.dataClassifyEnum = buildTableApiService.dataClassifyEnum;
            dataModelVO.delBusiness = false;
            dataModelVO.businessId = String.valueOf(buildTableApiService.getAppId());
            dataModelVO.userId = buildTableApiService.userId;
            DataModelTableVO dataModelTableVO = new DataModelTableVO();
            dataModelTableVO.type = buildTableApiService.olapTableEnum;
            List<Long> ids = new ArrayList<>();
            ids.add(buildTableApiService.id);
            dataModelTableVO.ids = ids;
            dataModelVO.tableServerIdList = dataModelTableVO;
            TableNifiSettingPO tableNifiSettingPO = tableNifiSettingService.query().eq("type", buildTableApiService.olapTableEnum.getValue()).eq("app_id", buildTableApiService.getAppId()).eq("table_access_id", buildTableApiService.id).one();

            if (tableNifiSettingPO != null && tableNifiSettingPO.tableComponentId != null) {
                componentsBuild.deleteNifiFlow(dataModelVO);
            }
            // 创建小组
            DataAccessConfigDTO dataAccessConfig = new DataAccessConfigDTO();
            TaskGroupConfig taskGroupConfig = new TaskGroupConfig();
            taskGroupConfig.appName = buildTableApiService.apiName;
            taskGroupConfig.appDetails = buildTableApiService.apiDes;
            dataAccessConfig.taskGroupConfig = taskGroupConfig;
            ProcessGroupEntity processGroupEntity = buildTaskGroup(dataAccessConfig, tableServerGroupId);
            String taskGroupId = processGroupEntity.getId();
            NifiConfigPO cfgConfigPO = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.CFG_DB_POOL_COMPONENT_ID.getName()).one();
            if (cfgConfigPO != null) {
                cfgControllerServiceId = cfgConfigPO.componentId;
            } else {
                ControllerServiceEntity controllerServiceEntity = buildCfgPool();
                savaNifiConfig(controllerServiceEntity.getId(), ComponentIdTypeEnum.CFG_DB_POOL_COMPONENT_ID);
                cfgControllerServiceId = controllerServiceEntity.getId();
            }
            // 依托小组,创建组件
            TableSyncModeDTO syncMode = buildTableApiService.syncModeDTO;
            BuildNifiFlowDTO buildNifiFlow = new BuildNifiFlowDTO();
            buildNifiFlow.synchronousTypeEnum = SynchronousTypeEnum.TOEXTERNALDB;
            buildNifiFlow.id = buildTableApiService.id;
            buildNifiFlow.type = buildTableApiService.olapTableEnum;
            buildNifiFlow.fetchSize = syncMode.fetchSize;
            buildNifiFlow.maxRowsPerFlowFile = syncMode.maxRowsPerFlowFile;
            buildNifiFlow.appId = Long.valueOf(buildTableApiService.appId);
            ProcessorConfig processorConfig = new ProcessorConfig();
            //dataAccessConfig.processorConfig.targetTableName
            //只有定时才给值，不定时则不启动定时组件
            if (Objects.equals(syncMode.triggerType, 1)) {
                //timer
                if (!Objects.equals(syncMode.scheduleType, 1)) {
                    processorConfig.scheduleExpression = syncMode.cornExpression;
                    processorConfig.scheduleType = SchedulingStrategyTypeEnum.CRON;
                } else {
                    processorConfig.scheduleExpression = syncMode.timerDriver;
                    processorConfig.scheduleType = SchedulingStrategyTypeEnum.TIMER;
                }
            }
            processorConfig.targetTableName = buildTableApiService.apiName;
            dataAccessConfig.processorConfig = processorConfig;
            /**
             * 创建数据服务api组件！！！
             */
            List<ProcessorEntity> processorEntities = buildProcessorApi(taskGroupId, dataAccessConfig, taskGroupId, cfgControllerServiceId, buildNifiFlow, buildTableApiService);

            // 启动,保存
            enabledProcessor(taskGroupId, processorEntities);

            dto.setId((int) buildTableApiService.id);
            // 发布成功则状态置为1
            dto.setStatus(TableServicePublicStatusEnum.PUBLIC_YES.getValue());
        } catch (Exception e) {
            // 发布失败则状态置为2
            dto.setStatus(TableServicePublicStatusEnum.PUBLIC_FAIL.getValue());
            log.error("数据服务api同步数据报错:{}", StackTraceHelper.getStackTraceInfo(e));
        } finally {
            if (acke != null) {
                acke.acknowledge();
            }
            try {
                log.info("开始修改数据服务api发布状态，参数id：[{}]，status：[{}]", dto.id, dto.status);
                consumeServeiceClient.updateTableApiStatus(dto);
            } catch (Exception e) {
                throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
            }
        }
        return ResultEnum.SUCCESS;
    }

    private ControllerServiceEntity buildCfgPool() {
        String groupId = NifiConstants.ApiConstants.ROOT_NODE;
        BuildDbControllerServiceDTO targetDto = new BuildDbControllerServiceDTO();
        targetDto.user = "${CFG_DB_POOL_USERNAME}";
        targetDto.pwd = "${CFG_DB_POOL_PASSWORD}";
        targetDto.conUrl = "${CFG_DB_POOL_URL}";
        targetDto.driverName = DriverTypeEnum.MYSQL.getName();
        targetDto.enabled = true;
        targetDto.groupId = groupId;
        targetDto.name = "Config Data DB Connection";
        targetDto.details = "Config Data DB Connection";
        targetDto.driverLocation = NifiConstants.DriveConstants.MYSQL_DRIVE_PATH;
        BusinessResult<ControllerServiceEntity> targetRes = componentsBuild.buildDbControllerService(targetDto);
        if (targetRes.success) {
            return targetRes.data;
        } else {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, targetRes.msg);
        }
    }

    private List<ProcessorEntity> buildProcessorApi(String appGroupId, DataAccessConfigDTO config, String groupId,
                                                    String cfgDbPoolId, BuildNifiFlowDTO dto, BuildTableApiServiceDTO buildTableApiService) {
        List<ProcessorEntity> res = new ArrayList<>();
        TableNifiSettingPO tableNifiSettingPO = new TableNifiSettingPO();
        TableNifiSettingPO tableNifiSettingPO1 = new TableNifiSettingPO();
        tableNifiSettingPO1 = tableNifiSettingService.query().eq("table_access_id", dto.id).eq("type", dto.type.getValue()).one();
        if (tableNifiSettingPO1 != null) {
            tableNifiSettingPO = tableNifiSettingPO1;
        }
        tableNifiSettingPO.tableComponentId = groupId;
        tableNifiSettingPO.appId = buildTableApiService.appId;
        tableNifiSettingPO.tableAccessId = Math.toIntExact(dto.id);
        tableNifiSettingPO.type = dto.type.getValue();
        tableNifiSettingPO.tableName = buildTableApiService.getApiName();
        //日志监控
        List<AutoEndBranchTypeEnum> autoEndBranchTypeEnums = new ArrayList<>();
        autoEndBranchTypeEnums.add(AutoEndBranchTypeEnum.FAILURE2);
        List<ProcessorEntity> processorEntities = pipelineSupervision(groupId, res, cfgDbPoolId, tableNifiSettingPO);
        String supervisionId = processorEntities.get(0).getId();
        res.addAll(processorEntities);
        //调度组件,在数据接入的时候调一次
        String inputPortId = "";

        if (buildTableApiService.syncModeDTO.triggerType == 1) {
            ProcessorEntity dispatchProcessor = queryDispatchApiProcessor(config, groupId, cfgDbPoolId, dto);
            tableNifiSettingPO.dispatchComponentId = dispatchProcessor.getId();
            res.add(dispatchProcessor);
            //发送消息PublishKafka
            ProcessorEntity processorEntity2 = convertJsonProcessor(groupId, 0, 2);
            res.add(processorEntity2);
            ProcessorEntity publishKafkaProcessor = createPublishKafkaProcessor(config, dto, groupId, 3, true);
            res.add(publishKafkaProcessor);
            componentConnector(groupId, dispatchProcessor.getId(), processorEntity2.getId(), AutoEndBranchTypeEnum.SUCCESS);
            componentConnector(groupId, processorEntity2.getId(), publishKafkaProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
        } else {
            //更新topic
            TableTopicDTO tableTopicDTO = new TableTopicDTO();
            tableTopicDTO.tableId = Math.toIntExact(dto.id);
            tableTopicDTO.tableType = dto.type.getValue();
            tableTopicDTO.topicName = MqConstants.TopicPrefix.TOPIC_PREFIX + dto.type.getValue() + "." + dto.appId + "." + dto.id;
            tableTopicDTO.topicType = TopicTypeEnum.DAILY_NIFI_FLOW.getValue();
            if (Objects.equals(dto.type, OlapTableEnum.KPI)) {
                tableTopicDTO.topicName = MqConstants.TopicPrefix.TOPIC_PREFIX + OlapTableEnum.KPI.getValue() + "." + dto.appId + "." + dto.id;
            }
            tableTopicService.updateTableTopic(tableTopicDTO);
        }

        //原变量字段
        ProcessorEntity incrementProcessor = evaluateJson(groupId, 6);
        res.add(incrementProcessor);
        tableNifiSettingPO.setIncrementProcessorId = incrementProcessor.getId();

        //接受消息ConsumeKafka
        ProcessorEntity consumeKafkaProcessor = createConsumeKafkaProcessor(config, dto, groupId, 5);
        List<ProcessorEntity> processorEntityList = new ArrayList<>();
        processorEntityList.add(consumeKafkaProcessor);
        enabledProcessor(groupId, processorEntityList);
        try {
            ProcessorEntity processorEntity = NifiHelper.getProcessorsApi().getProcessor(consumeKafkaProcessor.getId());
            ProcessorRunStatusEntity processorRunStatusEntity = new ProcessorRunStatusEntity();
            processorRunStatusEntity.setDisconnectedNodeAcknowledged(false);
            processorRunStatusEntity.setRevision(processorEntity.getRevision());
            processorRunStatusEntity.setState(ProcessorRunStatusEntity.StateEnum.STOPPED);
            NifiHelper.getProcessorsApi().updateRunStatus(processorEntity.getId(), processorRunStatusEntity);
        } catch (ApiException e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR);
        }

        componentConnector(groupId, consumeKafkaProcessor.getId(), incrementProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);

        tableNifiSettingPO.consumeKafkaProcessorId = consumeKafkaProcessor.getId();
        //读取增量字段组件
        ProcessorEntity generateFlowFileProcessor = replaceTextForJson(groupId, dto);
        res.add(generateFlowFileProcessor);
        componentConnector(groupId, incrementProcessor.getId(), generateFlowFileProcessor.getId(), AutoEndBranchTypeEnum.MATCHED);
        tableNifiSettingPO.generateFlowFileProcessorId = generateFlowFileProcessor.getId();
        //创建数据转换json组件
        ProcessorEntity invokeHTTPToTableApi = invokeHTTPToTableApi(groupId);
        res.add(invokeHTTPToTableApi);
        tableNifiSettingPO.invokeHttpProcessorId = invokeHTTPToTableApi.getId();
        //连接器
        componentConnector(groupId, generateFlowFileProcessor.getId(), invokeHTTPToTableApi.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentConnector(groupId, invokeHTTPToTableApi.getId(), invokeHTTPToTableApi.getId(), AutoEndBranchTypeEnum.RETRY2);
        componentsConnector(groupId, invokeHTTPToTableApi.getId(), supervisionId, autoEndBranchTypeEnums);

        tableNifiSettingPO.type = dto.type.getValue();
        tableNifiSettingService.saveOrUpdate(tableNifiSettingPO);
        return res;
    }

    /**
     * 调用api参数组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity replaceTextForJson(String groupId, BuildNifiFlowDTO dto) {
        BuildReplaceTextProcessorDTO buildReplaceTextProcessorDTO = new BuildReplaceTextProcessorDTO();
        //校验通过修改字段集合
        TableApiSyncDTO tableApiSyncDTO = new TableApiSyncDTO();
        tableApiSyncDTO.apiId = dto.id;
        tableApiSyncDTO.appId = dto.appId;
        tableApiSyncDTO.tableType = dto.type.getValue();
        HashMap<String, Object> checkByFieldMap = new HashMap<>();
        checkByFieldMap.put("pipelTaskTraceId", "${pipelTaskTraceId}");
        checkByFieldMap.put("fidata_batch_code", "${fidata_batch_code}");
        checkByFieldMap.put("sourcesys", "${sourcesys}");
        tableApiSyncDTO.setCheckByFieldMap(checkByFieldMap);
        buildReplaceTextProcessorDTO.name = "GenerateFlowFileProcessor";
        buildReplaceTextProcessorDTO.details = "query_phase";
        buildReplaceTextProcessorDTO.groupId = groupId;
        buildReplaceTextProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(0, 7);
        //替换流文件
        buildReplaceTextProcessorDTO.evaluationMode = "Entire text";
        buildReplaceTextProcessorDTO.maximumBufferSize = "100 MB";
        buildReplaceTextProcessorDTO.replacementValue = JSON.toJSONString(tableApiSyncDTO);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildReplaceTextProcess(buildReplaceTextProcessorDTO, new ArrayList<>());
        return processorEntityBusinessResult.data;
    }

    private ProcessorEntity invokeHTTPToTableApi(String groupId) {
        BuildInvokeHttpProcessorDTO buildInvokeHttpProcessorDTO = new BuildInvokeHttpProcessorDTO();
        buildInvokeHttpProcessorDTO.name = "invokeHTTPProcessor";
        buildInvokeHttpProcessorDTO.details = "query_phase";
        buildInvokeHttpProcessorDTO.groupId = groupId;
        buildInvokeHttpProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(0, 8);
        buildInvokeHttpProcessorDTO.attributesToSend = "(?s)(^.*$)";
        buildInvokeHttpProcessorDTO.contentType = "application/json;charset=UTF-8";
        buildInvokeHttpProcessorDTO.httpMethod = "POST";
        buildInvokeHttpProcessorDTO.remoteUrl = dataGovernanceUrl + "/dataservice/tableApiService/syncTableApi?Content-Type=application/json";
        buildInvokeHttpProcessorDTO.nifiToken = nifiToken;
        buildInvokeHttpProcessorDTO.socketConnectTimeout = "300 secs";
        buildInvokeHttpProcessorDTO.socketReadTimeout = "300 secs";
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildInvokeHTTPProcessor(buildInvokeHttpProcessorDTO, new ArrayList<>());
        return processorEntityBusinessResult.data;
    }

    @Override
    public ResultEnum buildDeleteDataApi(String dataInfo, Acknowledgment acke) {
        log.info("数据分发api删除nifi流程参数:{}", dataInfo);
        try {
            BuildDeleteTableApiServiceDTO buildDeleteTableApiService = JSON.parseObject(dataInfo, BuildDeleteTableApiServiceDTO.class);
            DataModelVO dataModelVO = new DataModelVO();
            dataModelVO.delBusiness = buildDeleteTableApiService.delBusiness;
            DataModelTableVO dataModelTableVO = new DataModelTableVO();
            dataModelTableVO.ids = buildDeleteTableApiService.ids;
            dataModelTableVO.type = buildDeleteTableApiService.olapTableEnum;
            dataModelVO.tableServerIdList = dataModelTableVO;
            dataModelVO.businessId = buildDeleteTableApiService.appId;
            dataModelVO.dataClassifyEnum = DataClassifyEnum.DATA_SERVICE_API;
            dataModelVO.userId = buildDeleteTableApiService.userId;
            return componentsBuild.deleteNifiFlow(dataModelVO);
        } catch (Exception e) {
            log.error("数据分发api流程删除失败" + StackTraceHelper.getStackTraceInfo(e));
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        } finally {
            acke.acknowledge();
        }
    }

    @Override
    public ResultEnum buildAccessNifiProcess(String data, Acknowledgment ack) {
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        ModelPublishStatusDTO modelPublishStatusDTO = new ModelPublishStatusDTO();
        modelPublishStatusDTO.publish = 1;
        log.info("创建nifi流程发布参数:" + data);
        BuildNifiFlowDTO dto = JSON.parseObject(data, BuildNifiFlowDTO.class);
        try {
            modelPublishStatusDTO.tableId = dto.id;
            if (Objects.equals(dto.synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
                client.updateTablePublishStatus(modelPublishStatusDTO);
            }
            //获取数据接入配置项
            DataAccessConfigDTO configDTO = getConfigData(dto.id, dto.appId, dto.synchronousTypeEnum, dto.type, dto.dataClassifyEnum, dto.tableName, dto.selectSql, dto);
            if (configDTO == null) {
                log.error("数据接入配置项获取失败。id: 【" + dto.id + "】, appId: 【" + dto.appId + "】");
                return ResultEnum.NOTFOUND;
            }
            AppNifiSettingPO appNifiSettingPO = new AppNifiSettingPO();
            AppNifiSettingPO appNifiSettingPO1 = new AppNifiSettingPO();
            if (dto.nifiCustomWorkflowId != null) {
                appNifiSettingPO1 = appNifiSettingService.query().eq("app_id", dto.appId).eq("nifi_custom_workflow_id", dto.nifiCustomWorkflowId).eq("type", dto.dataClassifyEnum.getValue()).eq("del_flag", 1).one();

            } else {
                List<AppNifiSettingPO> list = appNifiSettingService.query().eq("app_id", dto.appId).eq("type", dto.dataClassifyEnum.getValue()).eq("del_flag", 1).list();
                if (list != null && list.size() != 0) {
                    for (AppNifiSettingPO appNifiSettingPO2 : list) {
                        if (appNifiSettingPO2.nifiCustomWorkflowId == null) {
                            appNifiSettingPO1 = appNifiSettingPO2;
                        }
                    }
                }

            }
            if (appNifiSettingPO1 != null) {
                appNifiSettingPO = appNifiSettingPO1;
            }
            log.info("【数据接入配置项参数】" + JSON.toJSONString(configDTO));
            //1. 获取数据接入配置库连接池
            ControllerServiceEntity cfgDbPool = buildCfgDsPool(configDTO);

            //2. 创建应用组
            ProcessGroupEntity groupEntity = buildAppGroup(configDTO, dto.groupComponentId);
            appNifiSettingPO.appId = String.valueOf(dto.appId);
            appNifiSettingPO.appComponentId = groupEntity.getId();
            appNifiSettingPO.type = dto.dataClassifyEnum.getValue();
            appGroupId = groupEntity.getId();
            appParentGroupId = groupEntity.getComponent().getParentGroupId();

            if (dto.groupStructureId != null) {
                appGroupId = dto.groupStructureId;
            }
            List<ControllerServiceEntity> dbPool = buildDsConnectionPool(dto.synchronousTypeEnum, configDTO, appGroupId, dto);
            String sourceId = "";
            if (!dto.excelFlow) {

                sourceId = dbPool.get(0).getId();
            }


            //4. 创建任务组创建时要把原任务组删掉,防止重复发布带来影响  dto.id, dto.appId
            DataModelVO dataModelVO = new DataModelVO();
            dataModelVO.dataClassifyEnum = dto.dataClassifyEnum;
            dataModelVO.delBusiness = false;
            dataModelVO.businessId = String.valueOf(dto.appId);
            dataModelVO.userId = dto.userId;
            DataModelTableVO dataModelTableVO = new DataModelTableVO();
            dataModelTableVO.type = dto.type;
            List<Long> ids = new ArrayList<>();
            ids.add(dto.id);
            dataModelTableVO.ids = ids;
            dataModelVO.indicatorIdList = dataModelTableVO;
            TableNifiSettingPO tableNifiSettingPO = new TableNifiSettingPO();
            if (dto.workflowDetailId != null) {
                tableNifiSettingPO = tableNifiSettingService.query().eq("app_id", dto.appId).eq("nifi_custom_workflow_detail_id", dto.workflowDetailId).eq("table_access_id", dto.id).eq("type", dto.type.getValue()).one();

            } else {
                tableNifiSettingPO = tableNifiSettingService.query().eq("app_id", dto.appId).eq("table_access_id", dto.id).eq("type", dto.type.getValue()).one();

            }
            if (tableNifiSettingPO != null && tableNifiSettingPO.tableComponentId != null) {
                componentsBuild.deleteNifiFlow(dataModelVO);
            }
            ProcessGroupEntity taskGroupEntity = buildTaskGroup(configDTO, groupEntity.getId());

            if (dto.nifiCustomWorkflowId != null && dto.nifiCustomWorkflowId != "") {
                // 创建input_port(任务)   (后期入库)
                tableInputPortId = buildPortComponent(configDTO.taskGroupConfig.appName, groupEntity.getId(),
                        groupEntity.getPosition().getX(), groupEntity.getPosition().getY(), PortComponentEnum.TASK_INPUT_PORT_COMPONENT);
                // 创建output_port(任务)   (后期入库)
                tableOutputPortId = buildPortComponent(configDTO.taskGroupConfig.appName, groupEntity.getId(),
                        groupEntity.getPosition().getX(), groupEntity.getPosition().getY(), PortComponentEnum.TASK_OUTPUT_PORT_COMPONENT);
            }

            groupEntityId = groupEntity.getId();
            taskGroupEntityId = taskGroupEntity.getId();

            //5. 创建组件

            List<ProcessorEntity> processors = buildProcessorVersion2(groupEntity.getId(), configDTO, taskGroupEntity.getId(), sourceId, dbPool.get(1).getId(), cfgDbPool.getId(), appNifiSettingPO, dto, null);
            enabledProcessor(taskGroupEntity.getId(), processors);
            //7. 如果是接入,同步一次,然后把调度组件停掉
            if (dto.groupStructureId == null && dto.openTransmission) {
                String topicName = MqConstants.TopicPrefix.TOPIC_PREFIX + dto.type.getValue() + "." + dto.appId + "." + dto.id;
                int value = TopicTypeEnum.DAILY_NIFI_FLOW.getValue();
                if (Objects.equals(value, OlapTableEnum.KPI)) {
                    topicName = MqConstants.TopicPrefix.TOPIC_PREFIX + OlapTableEnum.KPI.getValue() + "." + dto.appId + "." + dto.id;
                }
                KafkaReceiveDTO kafkaRkeceiveDTO = KafkaReceiveDTO.builder().build();
                kafkaRkeceiveDTO.topic = topicName;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                kafkaRkeceiveDTO.start_time = simpleDateFormat.format(new Date());
                kafkaRkeceiveDTO.pipelTaskTraceId = UUID.randomUUID().toString();
                kafkaRkeceiveDTO.fidata_batch_code = kafkaRkeceiveDTO.pipelTaskTraceId;
                kafkaRkeceiveDTO.pipelStageTraceId = UUID.randomUUID().toString();
                kafkaRkeceiveDTO.ifTaskStart = true;
                kafkaRkeceiveDTO.topicType = TopicTypeEnum.DAILY_NIFI_FLOW.getValue();
                //pc.universalPublish(kafkaRkeceiveDTO);
                kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_TASK_PUBLISH_FLOW, JSON.toJSONString(kafkaRkeceiveDTO));
            }

            //7. 回写id
            savaNifiConfig(cfgDbPool.getId(), ComponentIdTypeEnum.CFG_DB_POOL_COMPONENT_ID);
            if (Objects.equals(dto.synchronousTypeEnum.getName(), SynchronousTypeEnum.PGTOPG.getName())) {
                savaNifiConfig(dbPool.get(1).getId(), ComponentIdTypeEnum.PG_DW_DB_POOL_COMPONENT_ID);
            } else if (Objects.equals(dto.synchronousTypeEnum.getName(), SynchronousTypeEnum.PGTODORIS.getName())) {
                savaNifiConfig(dbPool.get(1).getId(), ComponentIdTypeEnum.DORIS_OLAP_DB_POOL_COMPONENT_ID);
                savaNifiConfig(dbPool.get(0).getId(), ComponentIdTypeEnum.PG_DW_DB_POOL_COMPONENT_ID);
            }
            return resultEnum;
        } catch (Exception e) {
            resultEnum = ResultEnum.ERROR;
            modelPublishStatusDTO.publish = 3;
            modelPublishStatusDTO.publishErrorMsg = StackTraceHelper.getStackTraceInfo(e);
            if (Objects.equals(dto.synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
                client.updateTablePublishStatus(modelPublishStatusDTO);
            }
            log.error("nifi流程创建失败" + StackTraceHelper.getStackTraceInfo(e));
            return resultEnum;
        } finally {
            ack.acknowledge();
        }
    }

    @Override
    public ResultEnum buildModelNifiProcess(String dataInfo, Acknowledgment acke) {
        return null;
    }

    @Deprecated
    public void deleteGroup(String tableCompconentId) {
        try {
            NifiHelper.getProcessGroupsApi().createEmptyAllConnectionsRequest(tableCompconentId);
            ScheduleComponentsEntity scheduleComponentsEntity = new ScheduleComponentsEntity();
            scheduleComponentsEntity.setId(tableCompconentId);
            scheduleComponentsEntity.setState(ScheduleComponentsEntity.StateEnum.STOPPED);
            scheduleComponentsEntity.setDisconnectedNodeAcknowledged(false);
            NifiHelper.getFlowApi().scheduleComponents(tableCompconentId, scheduleComponentsEntity);
            scheduleComponentsEntity.setState(ScheduleComponentsEntity.StateEnum.DISABLED);
            NifiHelper.getFlowApi().scheduleComponents(tableCompconentId, scheduleComponentsEntity);
            RemotePortRunStatusEntity remotePortRunStatusEntity = new RemotePortRunStatusEntity();
            remotePortRunStatusEntity.setDisconnectedNodeAcknowledged(false);
            remotePortRunStatusEntity.setState(RemotePortRunStatusEntity.StateEnum.STOPPED);
            //NifiHelper.getRemoteProcessGroupsApi().updateRemoteProcessGroupRunStatus(tableCompconentId, remotePortRunStatusEntity);
            ActivateControllerServicesEntity activateControllerServicesEntity = new ActivateControllerServicesEntity();
            activateControllerServicesEntity.setId(tableCompconentId);
            activateControllerServicesEntity.setDisconnectedNodeAcknowledged(false);
            activateControllerServicesEntity.setState(ActivateControllerServicesEntity.StateEnum.DISABLED);
            NifiHelper.getFlowApi().activateControllerServices(tableCompconentId, activateControllerServicesEntity);
            ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(tableCompconentId);
            RevisionDTO revision = processGroup.getRevision();
            NifiHelper.getProcessGroupsApi().removeProcessGroup(tableCompconentId, String.valueOf(revision.getVersion()), revision.getClientId(), false);
        } catch (ApiException e) {
            log.error("删除组失败:" + StackTraceHelper.getStackTraceInfo(e));
        }
    }


    /**
     * 保存统一数据源控制器服务
     *
     * @param dbId
     * @return
     */
    public String saveDbconfig(Integer dbId) {
        String sourceControllerServiceId = "";
        NifiConfigPO targetNifiConfig = nifiConfigService.query().eq("datasource_config_id", dbId).one();
        if (Objects.nonNull(targetNifiConfig)) {
            sourceControllerServiceId = targetNifiConfig.componentId;
        } else {
            ResultEntity<DataSourceDTO> targetDataSource = userClient.getFiDataDataSourceById(dbId);
            if (targetDataSource.code == ResultEnum.SUCCESS.getCode()) {
                DataSourceDTO dataSource = targetDataSource.data;
                BuildDbControllerServiceDTO sourceControllerService = new BuildDbControllerServiceDTO();
                sourceControllerService.driverLocation = dataSource.conType.getDriverLocation();
                sourceControllerService.driverName = dataSource.conType.getDriverName();

                // 拼接字符串读取配置变量值
                sourceControllerService.conUrl = "${" + ComponentIdTypeEnum.DB_URL.getName() + dbId + "}";
                sourceControllerService.pwd = "${" + ComponentIdTypeEnum.DB_PASSWORD.getName() + dbId + "}";
                sourceControllerService.user = "${" + ComponentIdTypeEnum.DB_USERNAME.getName() + dbId + "}";

                sourceControllerService.name = dataSource.name;
                sourceControllerService.enabled = true;
                sourceControllerService.dbcpMaxIdleConns = "50";
                sourceControllerService.groupId = NifiConstants.ApiConstants.ROOT_NODE;
                sourceControllerService.details = "details" + dataSource.name;
                BusinessResult<ControllerServiceEntity> controllerServiceEntityBusinessResult = componentsBuild.buildDbControllerService(sourceControllerService);
                sourceControllerServiceId = controllerServiceEntityBusinessResult.data.getId();
                NifiConfigPO nifiConfig = new NifiConfigPO();
                nifiConfig.componentId = sourceControllerServiceId;
                nifiConfig.componentKey = dataSource.name;
                nifiConfig.datasourceConfigId = String.valueOf(dbId);
                nifiConfigService.save(nifiConfig);
            } else {
                log.error("userclient无法查询到外部数据源的连接信息");
            }
        }
        return sourceControllerServiceId;
    }

    /**
     * 保存统一数据源控制器服务 MongoDb
     *
     * @param dbId
     * @return
     */
    public String saveMongoDbconfig(Integer dbId) {
        String sourceControllerServiceId = "";
        NifiConfigPO targetNifiConfig = nifiConfigService.query().eq("datasource_config_id", dbId).one();
        if (Objects.nonNull(targetNifiConfig)) {
            sourceControllerServiceId = targetNifiConfig.componentId;
        } else {
            ResultEntity<DataSourceDTO> targetDataSource = userClient.getFiDataDataSourceById(dbId);
            if (targetDataSource.code == ResultEnum.SUCCESS.getCode()) {
                DataSourceDTO dataSource = targetDataSource.data;
                BuildDbControllerServiceDTO sourceControllerService = new BuildDbControllerServiceDTO();
                sourceControllerService.driverLocation = dataSource.conType.getDriverLocation();
                sourceControllerService.driverName = dataSource.conType.getDriverName();

                // 拼接字符串读取配置变量值
                sourceControllerService.conUrl = "${" + ComponentIdTypeEnum.DB_URL.getName() + dbId + "}";
                sourceControllerService.pwd = "${" + ComponentIdTypeEnum.DB_PASSWORD.getName() + dbId + "}";
                sourceControllerService.user = "${" + ComponentIdTypeEnum.DB_USERNAME.getName() + dbId + "}";

                sourceControllerService.name = dataSource.name;
                sourceControllerService.enabled = true;
                sourceControllerService.dbcpMaxIdleConns = "50";
                sourceControllerService.groupId = NifiConstants.ApiConstants.ROOT_NODE;
                sourceControllerService.details = "details" + dataSource.name;
                BusinessResult<ControllerServiceEntity> controllerServiceEntityBusinessResult = componentsBuild.buildMongoDbControllerService(sourceControllerService);
                sourceControllerServiceId = controllerServiceEntityBusinessResult.data.getId();
                NifiConfigPO nifiConfig = new NifiConfigPO();
                nifiConfig.componentId = sourceControllerServiceId;
                nifiConfig.componentKey = dataSource.name;
                nifiConfig.datasourceConfigId = String.valueOf(dbId);
                nifiConfigService.save(nifiConfig);
            } else {
                log.error("userclient无法查询到外部数据源的连接信息");
            }
        }
        return sourceControllerServiceId;
    }

    /**
     * 接入,建模 --- 创建NIFI流程
     *
     * @param data
     * @param ack
     * @return
     */
    @Override
    public ResultEnum msg(String data, Acknowledgment ack) {
        log.info("进入接入，建模的创建NIFI流程！！！");
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        ModelPublishStatusDTO modelPublishStatusDTO = new ModelPublishStatusDTO();
        modelPublishStatusDTO.publish = 1;
        log.info("创建nifi流程发布参数:" + data);
        data = "[" + data + "]";
        List<BuildNifiFlowDTO> buildNifiFlows = JSON.parseArray(data, BuildNifiFlowDTO.class);
        BuildNifiFlowDTO dto = new BuildNifiFlowDTO();
        String subRunId = "";
        try {
            for (BuildNifiFlowDTO buildNifiFlow : buildNifiFlows) {
                //获取到当前发布表引用的数据源 -- 获取数据源类型 -- 决定nifi流程样式
                Integer dataSourceDbId = buildNifiFlow.dataSourceDbId;
                ResultEntity<DataSourceDTO> fiDataDataSourceById = userClient.getFiDataDataSourceById(dataSourceDbId);
                DataSourceDTO sourceType = fiDataDataSourceById.data;

                dto = buildNifiFlow;
                modelPublishStatusDTO.tableId = dto.id;
                //获取数据接入配置项
                DataAccessConfigDTO configDTO = getConfigData(dto.id, dto.appId, dto.synchronousTypeEnum, dto.type, dto.dataClassifyEnum, dto.tableName, dto.selectSql, dto);
                log.info("数据接入配置项：{}", JSON.toJSONString(configDTO));
                if (configDTO == null) {
                    log.error("数据接入配置项获取失败。id: 【" + dto.id + "】, appId: 【" + dto.appId + "】");
                    return ResultEnum.NOTFOUND;
                }
                AppNifiSettingPO appNifiSettingPO = new AppNifiSettingPO();
                AppNifiSettingPO appNifiSettingPO1 = new AppNifiSettingPO();
                if (dto.nifiCustomWorkflowId != null) {
                    appNifiSettingPO1 = appNifiSettingService.query().eq("app_id", dto.appId).eq("nifi_custom_workflow_id", dto.nifiCustomWorkflowId).eq("type", dto.dataClassifyEnum.getValue()).eq("del_flag", 1).one();

                } else {
                    List<AppNifiSettingPO> list = appNifiSettingService.query().eq("app_id", dto.appId).eq("type", dto.dataClassifyEnum.getValue()).eq("del_flag", 1).list();
                    if (list != null && list.size() != 0) {
                        for (AppNifiSettingPO appNifiSettingPO2 : list) {
                            if (appNifiSettingPO2.nifiCustomWorkflowId == null) {
                                appNifiSettingPO1 = appNifiSettingPO2;
                            }
                        }
                    }

                }
                if (appNifiSettingPO1 != null) {
                    appNifiSettingPO = appNifiSettingPO1;
                }
                NifiConfigPO nifiConfigPO = new NifiConfigPO();
                log.info("【数据接入配置项参数】" + JSON.toJSONString(configDTO));
                //1. 获取数据接入配置库连接池
                ControllerServiceEntity cfgDbPool = buildCfgDsPool(configDTO);

                //2. 创建应用组
                ProcessGroupEntity groupEntity = buildAppGroup(configDTO, dto.groupComponentId);
                appNifiSettingPO.appId = String.valueOf(dto.appId);
                appNifiSettingPO.appComponentId = groupEntity.getId();
                appNifiSettingPO.type = dto.dataClassifyEnum.getValue();
                appGroupId = groupEntity.getId();
                appParentGroupId = groupEntity.getComponent().getParentGroupId();

                // 创建input_port组件(应用)  (后期入库)
//        appInputPortId = buildPortComponent(configDTO.groupConfig.appName, appParentGroupId, groupEntity.getPosition().getX(), groupEntity.getPosition().getY(), PortComponentEnum.APP_INPUT_PORT_COMPONENT);
                // 创建output_port组件(应用) (后期入库)
//        appOutputPortId = buildPortComponent(configDTO.groupConfig.appName, appParentGroupId, groupEntity.getPosition().getX(), groupEntity.getPosition().getY(), PortComponentEnum.APP_OUTPUT_PORT_COMPONENT);

                //3. 创建jdbc连接池
                //List<ControllerServiceEntity> dbPool = buildDsConnectionPool(configDTO, groupEntity.getId(),dto);
                if (dto.groupStructureId != null) {
                    appGroupId = dto.groupStructureId;
                }
                List<ControllerServiceEntity> dbPool = buildDsConnectionPool(dto.synchronousTypeEnum, configDTO, appGroupId, dto);
                String sourceId = "";
                if (!dto.excelFlow) {

                    sourceId = dbPool.get(0).getId();
                }

                //4. 创建任务组创建时要把原任务组删掉,防止重复发布带来影响  dto.id, dto.appId
                DataModelVO dataModelVO = new DataModelVO();
                dataModelVO.dataClassifyEnum = dto.dataClassifyEnum;
                dataModelVO.delBusiness = false;
                dataModelVO.businessId = String.valueOf(dto.appId);
                dataModelVO.userId = dto.userId;
                DataModelTableVO dataModelTableVO = new DataModelTableVO();
                dataModelTableVO.type = dto.type;
                List<Long> ids = new ArrayList<>();
                ids.add(dto.id);
                dataModelTableVO.ids = ids;
                dataModelVO.indicatorIdList = dataModelTableVO;
                TableNifiSettingPO tableNifiSettingPO = new TableNifiSettingPO();
                if (dto.workflowDetailId != null) {
                    tableNifiSettingPO = tableNifiSettingService.query().eq("app_id", dto.appId).eq("nifi_custom_workflow_detail_id", dto.workflowDetailId).eq("table_access_id", dto.id).eq("type", dto.type.getValue()).one();

                } else {
                    tableNifiSettingPO = tableNifiSettingService.query().eq("app_id", dto.appId).eq("table_access_id", dto.id).eq("type", dto.type.getValue()).one();

                }

                /**
                 * 删除原流程！！！！！
                 */
                if (tableNifiSettingPO != null && tableNifiSettingPO.tableComponentId != null) {
                    componentsBuild.deleteNifiFlow(dataModelVO);
                }

                /**
                 * 创建大组！！！！！
                 */
                ProcessGroupEntity taskGroupEntity = buildTaskGroup(configDTO, groupEntity.getId());

                if (dto.nifiCustomWorkflowId != null && dto.nifiCustomWorkflowId != "") {
                    // 创建input_port(任务)   (后期入库)
                    tableInputPortId = buildPortComponent(configDTO.taskGroupConfig.appName, groupEntity.getId(),
                            groupEntity.getPosition().getX(), groupEntity.getPosition().getY(), PortComponentEnum.TASK_INPUT_PORT_COMPONENT);
                    // 创建output_port(任务)   (后期入库)
                    tableOutputPortId = buildPortComponent(configDTO.taskGroupConfig.appName, groupEntity.getId(),
                            groupEntity.getPosition().getX(), groupEntity.getPosition().getY(), PortComponentEnum.TASK_OUTPUT_PORT_COMPONENT);
                }

                groupEntityId = groupEntity.getId();
                taskGroupEntityId = taskGroupEntity.getId();

                //5. 创建组件
                DataSourceTypeEnum conType;
                if (sourceType != null) {
                    conType = sourceType.conType;
                } else {
                    conType = null;
                }

                /**
                 * 创建组件！！！！！
                 */
                log.info("创建组件！！！！！");
                List<ProcessorEntity> processors = buildProcessorVersion2(groupEntity.getId(),
                        configDTO,
                        taskGroupEntity.getId(),
                        sourceId,
                        dbPool.get(1).getId(),
                        cfgDbPool.getId(),
                        appNifiSettingPO,
                        dto,
                        conType);

                /**
                 * 启动组件
                 */
                enabledProcessor(taskGroupEntity.getId(), processors);

                /**
                 * 如果数仓或数接页面勾选了立即同步（按照配置同步一次数据），即dto.openTransmission是否为true，则会同步,
                 * 就会调用pc.taskPublish(kafkaRkeceiveDTO)在nifi中进行数据的同步
                 *
                 */
                //7. 同步一次,然后把调度组件停掉
                if (dto.groupStructureId == null && dto.openTransmission) {
                    String topicName = MqConstants.TopicPrefix.TOPIC_PREFIX + dto.type.getValue() + "." + dto.appId + "." + dto.id;
                    int value = TopicTypeEnum.DAILY_NIFI_FLOW.getValue();
                    if (Objects.equals(value, OlapTableEnum.KPI)) {
                        topicName = MqConstants.TopicPrefix.TOPIC_PREFIX + OlapTableEnum.KPI.getValue() + "." + dto.appId + "." + dto.id;
                    }
                    KafkaReceiveDTO kafkaRkeceiveDTO = KafkaReceiveDTO.builder().build();
                    kafkaRkeceiveDTO.topic = topicName;
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    kafkaRkeceiveDTO.start_time = simpleDateFormat.format(new Date());
                    kafkaRkeceiveDTO.pipelTaskTraceId = UUID.randomUUID().toString();
                    subRunId = kafkaRkeceiveDTO.pipelTaskTraceId;
                    kafkaRkeceiveDTO.fidata_batch_code = kafkaRkeceiveDTO.pipelTaskTraceId;
                    kafkaRkeceiveDTO.pipelStageTraceId = UUID.randomUUID().toString();
                    kafkaRkeceiveDTO.ifTaskStart = true;
                    kafkaRkeceiveDTO.topicType = TopicTypeEnum.DAILY_NIFI_FLOW.getValue();
                    kafkaRkeceiveDTO.traceId = dto.traceId;
                    kafkaRkeceiveDTO.userId = dto.userId;
                    pc.taskPublish(kafkaRkeceiveDTO);
                    //kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_TASK_PUBLISH_FLOW, JSON.toJSONString(kafkaRkeceiveDTO));
                }

                if (Objects.equals(dto.synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
                    modelPublishStatusDTO.subRunId = subRunId;
                    modelPublishStatusDTO.tableHistoryId = dto.tableHistoryId;
                    client.updateTablePublishStatus(modelPublishStatusDTO);
                }
                //7. 回写id
                savaNifiConfig(cfgDbPool.getId(), ComponentIdTypeEnum.CFG_DB_POOL_COMPONENT_ID);
                if (Objects.equals(dto.synchronousTypeEnum.getName(), SynchronousTypeEnum.PGTOPG.getName())) {
                    savaNifiConfig(dbPool.get(1).getId(), ComponentIdTypeEnum.PG_DW_DB_POOL_COMPONENT_ID);
                } else if (Objects.equals(dto.synchronousTypeEnum.getName(), SynchronousTypeEnum.PGTODORIS.getName())) {
                    savaNifiConfig(dbPool.get(1).getId(), ComponentIdTypeEnum.DORIS_OLAP_DB_POOL_COMPONENT_ID);
                    savaNifiConfig(dbPool.get(0).getId(), ComponentIdTypeEnum.PG_DW_DB_POOL_COMPONENT_ID);
                }
            }
            return resultEnum;
        } catch (Exception e) {
            resultEnum = ResultEnum.ERROR;
            modelPublishStatusDTO.publish = PublishTypeEnum.FAIL.getValue();
            modelPublishStatusDTO.publishErrorMsg = StackTraceHelper.getStackTraceInfo(e);
            modelPublishStatusDTO.tableHistoryId = dto.tableHistoryId;
            modelPublishStatusDTO.subRunId = subRunId;
            if (Objects.equals(dto.synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
                client.updateTablePublishStatus(modelPublishStatusDTO);
            }
            log.error("接入、建模nifi流程创建失败:" + StackTraceHelper.getStackTraceInfo(e));
            return resultEnum;
        } finally {
            if (ack != null) {
                ack.acknowledge();
            }
        }

    }

    /**
     * 保存全局数据库连接配置
     *
     * @param componentId         配置的id
     * @param componentIdTypeEnum 操作类型
     */
    private void savaNifiConfig(String componentId, ComponentIdTypeEnum componentIdTypeEnum) {
        NifiConfigPO nifiConfigPO = new NifiConfigPO();
        NifiConfigPO one = nifiConfigService.query().eq("component_key", componentIdTypeEnum.getName()).one();
        if (one == null) {
            nifiConfigPO.componentId = componentId;
            nifiConfigPO.componentKey = componentIdTypeEnum.getName();
            nifiConfigService.save(nifiConfigPO);
        }
    }

    /**
     * 获取数据接入的配置
     *
     * @param appId 配置的id
     * @return 数据接入配置
     */
    public DataAccessConfigDTO getConfigData(long id, long appId, SynchronousTypeEnum synchronousTypeEnum, OlapTableEnum type, DataClassifyEnum dataClassifyEnum, String tableName, String selectSql, BuildNifiFlowDTO buildNifiFlowDTO) {
        DataAccessConfigDTO data = new DataAccessConfigDTO();
        GroupConfig groupConfig = new GroupConfig();
        DataSourceConfig targetDbPoolConfig = new DataSourceConfig();
        DataSourceConfig sourceDsConfig = new DataSourceConfig();
        FtpConfig ftpConfig = new FtpConfig();
        DataSourceConfig cfgDsConfig = new DataSourceConfig();
        TaskGroupConfig taskGroupConfig = new TaskGroupConfig();
        ProcessorConfig processorConfig = new ProcessorConfig();
        ResultEntity<DataAccessConfigDTO> res = new ResultEntity<>();
        GetTableBusinessDTO getTableBusinessDTO = new GetTableBusinessDTO();
        ResultEntity<List<ModelPublishFieldDTO>> fieldDetails = new ResultEntity<>();
        if (synchronousTypeEnum == SynchronousTypeEnum.TOPGODS) {
            res = client.dataAccessConfig(id, appId);
            if (res.code != ResultEnum.SUCCESS.getCode()) {
                return null;
            }
            if (res.data != null) {
                data = res.data;
            }
        } else if (synchronousTypeEnum == SynchronousTypeEnum.PGTOPG) {
            if (Objects.equals(type, OlapTableEnum.DIMENSION) || Objects.equals(type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
                fieldDetails = dataModelClient.selectDimensionAttributeList(Math.toIntExact(id));
            } else if (Objects.equals(type, OlapTableEnum.FACT) || Objects.equals(type, OlapTableEnum.CUSTOMWORKFACT)) {
                fieldDetails = dataModelClient.selectAttributeList(Math.toIntExact(id));
            }
            //dw同步,业务主键,逗号分隔
            data.businessKeyAppend = fieldDetails.data.stream().filter(Objects::nonNull).filter(e -> e.isPrimaryKey == 1).map(e -> e.fieldEnName).collect(Collectors.joining(","));
            //添加增量方式的接口
            int tableType = Objects.equals(type, OlapTableEnum.DIMENSION) || Objects.equals(type, OlapTableEnum.CUSTOMWORKDIMENSION) ? 0 : 1;
            ResultEntity<GetTableBusinessDTO> tableBusiness = dataModelClient.getTableBusiness(Math.toIntExact(id), tableType);
            getTableBusinessDTO = tableBusiness.data;
        }
        //拿出来
        AppNifiSettingPO appNifiSettingPO = new AppNifiSettingPO();
        if (buildNifiFlowDTO.nifiCustomWorkflowId != null) {
            appNifiSettingPO = appNifiSettingService.query().eq("app_id", appId).eq("nifi_custom_workflow_id", buildNifiFlowDTO.nifiCustomWorkflowId).eq("type", dataClassifyEnum.getValue()).eq("del_flag", 1).one();

        } else {
            List<AppNifiSettingPO> list = appNifiSettingService.query().eq("app_id", appId).eq("type", dataClassifyEnum.getValue()).eq("del_flag", 1).list();
            if (list != null && list.size() != 0) {
                for (AppNifiSettingPO appNifiSettingPO2 : list) {
                    if (appNifiSettingPO2.nifiCustomWorkflowId == null) {
                        appNifiSettingPO = appNifiSettingPO2;
                    }
                }
            }
        }
        NifiConfigPO nifiConfigPO = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.CFG_DB_POOL_COMPONENT_ID.getName()).one();
        //TableNifiSettingPO tableNifiSettingPO = tableNifiSettingService.query().eq("app_id", appId).eq("table_access_id", id).eq("type",type.getValue()).one();
        if (res.data != null && appNifiSettingPO != null && appNifiSettingPO.appComponentId != null) {
            data.groupConfig.newApp = false;
        } else {
            if (data != null && data.groupConfig != null) {
                data.groupConfig.newApp = true;
            }
        }

        if (appNifiSettingPO != null) {
            if (data.sourceDsConfig != null) {
                data.sourceDsConfig.componentId = appNifiSettingPO.sourceDbPoolComponentId;
                data.targetDsConfig.componentId = appNifiSettingPO.targetDbPoolComponentId;
                data.groupConfig.componentId = appNifiSettingPO.appComponentId;
            } else {
                //赋值对象
                sourceDsConfig.componentId = appNifiSettingPO.sourceDbPoolComponentId;
                targetDbPoolConfig.componentId = appNifiSettingPO.targetDbPoolComponentId;
                groupConfig.componentId = appNifiSettingPO.appComponentId;
                data.sourceDsConfig = sourceDsConfig;
                data.targetDsConfig = targetDbPoolConfig;
                data.groupConfig = groupConfig;
            }
        }
        if (nifiConfigPO != null) {
            if (data.cfgDsConfig != null) {
                data.cfgDsConfig.componentId = nifiConfigPO.componentId;
            } else {
                //赋值对象
                cfgDsConfig.componentId = nifiConfigPO.componentId;
                data.cfgDsConfig = cfgDsConfig;
            }

        }


        //target doris
        //各种数据源,首先入pg_ods
        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
            ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceOdsId));
            if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
                DataSourceDTO dataSource = fiDataDataSource.data;
                //com.microsoft.sqlserver.jdbc.SQLServerDriver
                targetDbPoolConfig.type = DriverTypeEnum.valueOf(dataSource.conType.getName());
                targetDbPoolConfig.user = dataSource.conAccount;
                targetDbPoolConfig.password = dataSource.conPassword;
                targetDbPoolConfig.jdbcStr = dataSource.conStr;
            } else {
                log.error("userclient无法查询到ods库的连接信息");
            }
            targetDbPoolConfig.tableFieldsList = res.data.targetDsConfig.tableFieldsList;
            TableNifiSettingPO tableNifiSettingPO = new TableNifiSettingPO();
            if (buildNifiFlowDTO.workflowDetailId != null) {
                tableNifiSettingPO = tableNifiSettingService.query().eq("app_id", appId).eq("nifi_custom_workflow_detail_id", buildNifiFlowDTO.workflowDetailId).eq("table_access_id", id).eq("type", type.getValue()).one();

            } else {
                tableNifiSettingPO = tableNifiSettingService.query().eq("app_id", appId).eq("table_access_id", id).eq("type", type.getValue()).one();

            }
            if (tableNifiSettingPO != null) {
                targetDbPoolConfig.targetTableName = tableNifiSettingPO.tableName;
                processorConfig.targetTableName = tableNifiSettingPO.tableName;
                if (tableNifiSettingPO.selectSql == null) {
                    tableNifiSettingPO.selectSql = "select 1 as a;";
                }
                processorConfig.sourceExecSqlQuery = tableNifiSettingPO.selectSql;
                targetDbPoolConfig.syncMode = tableNifiSettingPO.syncMode;
            } else {
                targetDbPoolConfig.syncMode = res.data.targetDsConfig.syncMode;
            }
            sourceDsConfig = res.data.sourceDsConfig;
            ftpConfig = res.data.ftpConfig;
            //pg_dw----doris_olap
        } else if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTODORIS)) {
            if (appNifiSettingPO != null && appNifiSettingPO.appComponentId != null) {
                groupConfig.newApp = false;
                groupConfig.componentId = appNifiSettingPO.appComponentId;
            } else {
                groupConfig.newApp = true;
            }
            groupConfig.appName = tableName;
            groupConfig.appDetails = tableName;
            cfgDsConfig.componentId = nifiConfigPO.componentId;
            taskGroupConfig.appName = tableName;
            processorConfig.targetTableName = tableName;
            processorConfig.sourceExecSqlQuery = selectSql;
            ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceDwId));
            if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
                DataSourceDTO dwData = fiDataDataSource.data;
                sourceDsConfig.type = DriverTypeEnum.valueOf(dwData.conType.getName());
                sourceDsConfig.jdbcStr = dwData.conStr;
                sourceDsConfig.user = dwData.conAccount;
                sourceDsConfig.password = dwData.conPassword;
            } else {
                log.error("userclient无法查询到dw库的连接信息");
                throw new FkException(ResultEnum.ERROR);
            }

            targetDbPoolConfig.type = DriverTypeEnum.MYSQL;
            targetDbPoolConfig.user = dorisUser;
            targetDbPoolConfig.password = dorisPwd;
            targetDbPoolConfig.jdbcStr = dorisUrl;
            targetDbPoolConfig.targetTableName = tableName;
            targetDbPoolConfig.tableFieldsList = null;
            data.groupConfig = groupConfig;
            data.cfgDsConfig = cfgDsConfig;
            data.taskGroupConfig = taskGroupConfig;
            data.processorConfig = processorConfig;
            //建模
        } else if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTOPG)) {
            if (appNifiSettingPO != null && appNifiSettingPO.appComponentId != null) {
                groupConfig.newApp = false;
                groupConfig.componentId = appNifiSettingPO.appComponentId;
            } else {
                groupConfig.newApp = true;
            }
            groupConfig.appName = buildNifiFlowDTO.appName;
            groupConfig.appDetails = tableName;
            cfgDsConfig.componentId = nifiConfigPO.componentId;
            taskGroupConfig.appName = tableName;
            processorConfig.targetTableName = tableName;
            processorConfig.sourceExecSqlQuery = selectSql;
            ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceOdsId));
            if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
                DataSourceDTO dataSource = fiDataDataSource.data;
                sourceDsConfig.type = DriverTypeEnum.valueOf(dataSource.conType.getName());
                sourceDsConfig.user = dataSource.conAccount;
                sourceDsConfig.password = dataSource.conPassword;
                sourceDsConfig.jdbcStr = dataSource.conStr;
            } else {
                log.error("userclient无法查询到ods库的连接信息");
            }

            ResultEntity<DataSourceDTO> fiDataDataDwSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceDwId));
            if (fiDataDataDwSource.code == ResultEnum.SUCCESS.getCode()) {
                DataSourceDTO dwData = fiDataDataDwSource.data;
                targetDbPoolConfig.type = DriverTypeEnum.valueOf(dwData.conType.getName());
                targetDbPoolConfig.user = dwData.conAccount;
                targetDbPoolConfig.password = dwData.conPassword;
                targetDbPoolConfig.jdbcStr = dwData.conStr;
            } else {
                log.error("userclient无法查询到dw库的连接信息");
                throw new FkException(ResultEnum.ERROR);
            }

            targetDbPoolConfig.targetTableName = tableName;
            targetDbPoolConfig.tableFieldsList = null;
            targetDbPoolConfig.syncMode = buildNifiFlowDTO.synMode;
            data.groupConfig = groupConfig;
            data.cfgDsConfig = cfgDsConfig;
            data.taskGroupConfig = taskGroupConfig;
            data.processorConfig = processorConfig;
            data.modelPublishFieldDTOList = fieldDetails.data;
            if (getTableBusinessDTO != null && getTableBusinessDTO.details != null) {
                //data.businessDTO=getTableBusinessDTO.details;
                TableBusinessDTO tableBusinessDTO = new TableBusinessDTO();
                BeanUtils.copyProperties(getTableBusinessDTO.details, tableBusinessDTO);
                tableBusinessDTO.accessId = Long.valueOf(getTableBusinessDTO.details.syncId);
                data.businessDTO = tableBusinessDTO;
            }

        }

        data.targetDsConfig = targetDbPoolConfig;
        data.sourceDsConfig = sourceDsConfig;
        data.processorConfig = processorConfig;
        data.ftpConfig = ftpConfig;
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceOdsId));
        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            DataSourceDTO dataSource = fiDataDataSource.data;
            IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(dataSource.conType);
            dbCommand.fieldFormatModification(data);
        } else {
            log.error("userclient无法查询到ods库的连接信息");
            throw new FkException(ResultEnum.ERROR);
        }
        return data;
    }

    /**
     * 创建app组
     *
     * @param config 数据接入配置
     * @return 组信息
     */
    private ProcessGroupEntity buildAppGroup(DataAccessConfigDTO config, String groupComponentId) {
        //判断是否需要新建组
        BuildProcessGroupDTO dto = new BuildProcessGroupDTO();
        int count = 0;
        BusinessResult<ProcessGroupEntity> res = null;
        if (groupComponentId != null) {
            dto.name = config.groupConfig.appName;
            dto.details = config.groupConfig.appDetails;
            dto.groupId = groupComponentId;
            count = componentsBuild.getGroupCount(groupComponentId);
            dto.positionDTO = NifiPositionHelper.buildXPositionDTO(count);
            //创建组件
            res = componentsBuild.buildProcessGroup(dto);
            if (res.success) {
                return res.data;
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
            }
        } else {
            if (config.groupConfig.newApp) {
                NifiConfigPO nifiConfigPO = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.DAILY_NIFI_FLOW_GROUP_ID.getName()).one();
                if (nifiConfigPO != null) {
                    dto.groupId = nifiConfigPO.componentId;
                } else {
                    BuildProcessGroupDTO buildProcessGroupDTO = new BuildProcessGroupDTO();
                    buildProcessGroupDTO.name = ComponentIdTypeEnum.DAILY_NIFI_FLOW_GROUP_ID.getName();
                    buildProcessGroupDTO.details = ComponentIdTypeEnum.DAILY_NIFI_FLOW_GROUP_ID.getName();
                    int groupCount = componentsBuild.getGroupCount(NifiConstants.ApiConstants.ROOT_NODE);
                    buildProcessGroupDTO.positionDTO = NifiPositionHelper.buildXPositionDTO(groupCount);
                    BusinessResult<ProcessGroupEntity> processGroupEntityBusinessResult = componentsBuild.buildProcessGroup(buildProcessGroupDTO);
                    if (processGroupEntityBusinessResult.success) {
                        dto.groupId = processGroupEntityBusinessResult.data.getId();
                        NifiConfigPO nifiConfigPO1 = new NifiConfigPO();
                        nifiConfigPO1.componentId = dto.groupId;
                        nifiConfigPO1.componentKey = ComponentIdTypeEnum.DAILY_NIFI_FLOW_GROUP_ID.getName();
                        nifiConfigService.save(nifiConfigPO1);
                    } else {
                        throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, processGroupEntityBusinessResult.msg);
                    }
                }
                dto.name = config.groupConfig.appName;
                dto.details = config.groupConfig.appDetails;
                //根据组个数，定义坐标
                if (groupComponentId != null) {
                    count = componentsBuild.getGroupCount(groupComponentId);
                } else {
                    count = componentsBuild.getGroupCount(dto.groupId);
                }
                dto.positionDTO = NifiPositionHelper.buildXPositionDTO(count);
                //创建组件
                res = componentsBuild.buildProcessGroup(dto);
                if (res.success) {
                    return res.data;
                } else {
                    throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
                }
            } else {
                //说明组件已存在，查询组件并返回
                log.info("【应用id】" + config.groupConfig.componentId);
                res = componentsBuild.getProcessGroupById(config.groupConfig.componentId);
                if (res.success) {
                    return res.data;
                } else {
                    throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
                }
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
    private List<ControllerServiceEntity> buildDsConnectionPool(SynchronousTypeEnum synchronousTypeEnum, DataAccessConfigDTO config,
                                                                String groupId, BuildNifiFlowDTO buildNifiFlowDTO) {
        //获取来源数据库类型
        DriverTypeEnum type = config.sourceDsConfig.getType();
        List<ControllerServiceEntity> list = new ArrayList<>();
        NifiConfigPO nifiConfigPo = new NifiConfigPO();
        NifiConfigPO nifiSourceConfigPo = null;
        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
            nifiConfigPo = nifiConfigService.query().eq("datasource_config_id", buildNifiFlowDTO.targetDbId).one();
            nifiSourceConfigPo = nifiConfigService.query().eq("datasource_config_id", buildNifiFlowDTO.dataSourceDbId).one();
        } else if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTODORIS)) {
            nifiConfigPo = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.DORIS_OLAP_DB_POOL_COMPONENT_ID.getName()).one();
            nifiSourceConfigPo = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.PG_DW_DB_POOL_COMPONENT_ID.getName()).one();
        } else if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTOPG)) {
            nifiConfigPo = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.PG_DW_DB_POOL_COMPONENT_ID.getName()).one();
            //需注意,建模发布也需要有两个数据源
            nifiSourceConfigPo = nifiConfigService.query().eq("datasource_config_id", buildNifiFlowDTO.dataSourceDbId).one();
        }
        BusinessResult<ControllerServiceEntity> targetRes = new BusinessResult<>(true, "控制器服务创建成功");
        BusinessResult<ControllerServiceEntity> sourceRes = new BusinessResult<>(true, "控制器服务创建成功");
        if (config.groupConfig.newApp || Objects.equals(buildNifiFlowDTO.dataClassifyEnum, DataClassifyEnum.CUSTOMWORKDATAACCESS) ||
                Objects.equals(buildNifiFlowDTO.dataClassifyEnum, DataClassifyEnum.DATAMODELKPL)) {
            if (nifiConfigPo != null) {
                ControllerServiceEntity data = new ControllerServiceEntity();
                data.setId(nifiConfigPo.componentId);
                targetRes.data = data;
            } else {
                // 统一数据源改造
                String componentId = saveDbconfig(buildNifiFlowDTO.targetDbId);
                ControllerServiceEntity entity = new ControllerServiceEntity();
                entity.setId(componentId);
                targetRes.data = entity;
            }
            //来源库
            if (!buildNifiFlowDTO.excelFlow) {
                if (nifiSourceConfigPo != null) {
                    ControllerServiceEntity data = new ControllerServiceEntity();
                    data.setId(nifiSourceConfigPo.componentId);
                    sourceRes.data = data;
                } else {
                    String componentId;
                    if (DriverTypeEnum.MONGODB.equals(type)) {
                        componentId = saveMongoDbconfig(buildNifiFlowDTO.dataSourceDbId);
                    } else {
                        componentId = saveDbconfig(buildNifiFlowDTO.dataSourceDbId);
                    }
                    // 统一数据源改造
                    ControllerServiceEntity entity = new ControllerServiceEntity();
                    entity.setId(componentId);
                    sourceRes.data = entity;
                }
            }
            if (targetRes.success && sourceRes.success) {
                list.add(sourceRes.data);
                list.add(targetRes.data);
                return list;
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, "【target】" + targetRes.msg + ",【source】" + sourceRes.msg);
            }
        } else {
            ControllerServiceEntity sourceControllerService = new ControllerServiceEntity();
            if (!buildNifiFlowDTO.excelFlow) {
                String componentId;
                if (DriverTypeEnum.MONGODB.equals(type)) {
                    componentId = saveMongoDbconfig(buildNifiFlowDTO.dataSourceDbId);
                } else {
                    componentId = saveDbconfig(buildNifiFlowDTO.dataSourceDbId);
                }
                ControllerServiceEntity entity = new ControllerServiceEntity();
                entity.setId(componentId);
                sourceControllerService = entity;
            }
            String componentId = saveDbconfig(buildNifiFlowDTO.targetDbId);
            ControllerServiceEntity entity = new ControllerServiceEntity();
            entity.setId(componentId);
            ControllerServiceEntity targetResControllerService = entity;
            list.add(sourceControllerService);
            list.add(targetResControllerService);
            return list;
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
    private BuildDbControllerServiceDTO buildDbControllerServiceDTO(DataAccessConfigDTO config, String groupId, DbPoolTypeEnum type, SynchronousTypeEnum synchronousTypeEnum) {
        DataSourceConfig dsConfig;
        String name;
        BuildDbControllerServiceDTO dto = new BuildDbControllerServiceDTO();
        HashMap<String, String> configMap = new HashMap<>();
        switch (type) {
            case SOURCE:
                dsConfig = config.sourceDsConfig;
                name = "Source Data DB Connection";
                if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTODORIS)) {
                    dto.conUrl = ComponentIdTypeEnum.PG_DW_DB_POOL_URL.getName();
                    dto.user = ComponentIdTypeEnum.PG_DW_DB_POOL_USERNAME.getName();
                    dto.pwd = ComponentIdTypeEnum.PG_DW_DB_POOL_PASSWORD.getName();
                } else if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTOPG)) {
                    dto.conUrl = ComponentIdTypeEnum.PG_ODS_DB_POOL_URL.getName();
                    dto.user = ComponentIdTypeEnum.PG_ODS_DB_POOL_USERNAME.getName();
                    dto.pwd = ComponentIdTypeEnum.PG_ODS_DB_POOL_PASSWORD.getName();
                }
                break;
            case TARGET:
                dsConfig = config.targetDsConfig;
                name = "Target Data DB Connection";
                if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
                    dto.conUrl = ComponentIdTypeEnum.PG_ODS_DB_POOL_URL.getName();
                    dto.user = ComponentIdTypeEnum.PG_ODS_DB_POOL_USERNAME.getName();
                    dto.pwd = ComponentIdTypeEnum.PG_ODS_DB_POOL_PASSWORD.getName();
                } else if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTODORIS)) {
                    dto.conUrl = ComponentIdTypeEnum.DORIS_OLAP_DB_POOL_URL.getName();
                    dto.user = ComponentIdTypeEnum.DORIS_OLAP_DB_POOL_USERNAME.getName();
                    dto.pwd = ComponentIdTypeEnum.DORIS_OLAP_DB_POOL_PASSWORD.getName();
                } else if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTOPG)) {
                    dto.conUrl = ComponentIdTypeEnum.PG_DW_DB_POOL_URL.getName();
                    dto.user = ComponentIdTypeEnum.PG_DW_DB_POOL_USERNAME.getName();
                    dto.pwd = ComponentIdTypeEnum.PG_DW_DB_POOL_PASSWORD.getName();
                }
                break;
            case CONFIG:
                dsConfig = config.cfgDsConfig;
                name = "Config Data DB Connection";
                dto.conUrl = ComponentIdTypeEnum.CFG_DB_POOL_URL.getName();
                dto.user = ComponentIdTypeEnum.CFG_DB_POOL_USERNAME.getName();
                dto.pwd = ComponentIdTypeEnum.CFG_DB_POOL_PASSWORD.getName();
                break;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }

        configMap.put(dto.pwd, dsConfig.password);
        configMap.put(dto.user, dsConfig.user);
        configMap.put(dto.conUrl, dsConfig.jdbcStr);

        if (Objects.equals(type, DbPoolTypeEnum.SOURCE) && Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
            dto.user = dsConfig.user;
            dto.pwd = dsConfig.password;
            dto.conUrl = dsConfig.jdbcStr;
        } else {
            dto.pwd = "${" + dto.pwd + "}";
            dto.conUrl = "${" + dto.conUrl + "}";
            dto.user = "${" + dto.user + "}";
        }


        componentsBuild.buildNifiGlobalVariable(configMap);
        dto.driverName = dsConfig.type.getName();
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
            case ORACLE:
                dto.driverLocation = NifiConstants.DriveConstants.ORACLE_DRIVE_PATH;
                break;
            case OPENEDGE:
                dto.driverLocation = NifiConstants.DriveConstants.OPENEDGE_DRIVE_PATH;
                break;
            default:
                break;
        }
        return dto;
    }


    /**
     * 创建nifi单个同步流程的组件
     *
     * @param appGroupId
     * @param config
     * @param groupId
     * @param sourceDbPoolId
     * @param targetDbPoolId
     * @param cfgDbPoolId
     * @param appNifiSettingPO
     * @param dto
     * @return
     */
    private List<ProcessorEntity> buildProcessorVersion2(String appGroupId,
                                                         DataAccessConfigDTO config,
                                                         String groupId,
                                                         String sourceDbPoolId,
                                                         String targetDbPoolId,
                                                         String cfgDbPoolId,
                                                         AppNifiSettingPO appNifiSettingPO,
                                                         BuildNifiFlowDTO dto,
                                                         DataSourceTypeEnum conType) {
        //获取数仓类型
        ResultEntity<DataSourceDTO> dwSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceDwId));
        DataSourceTypeEnum conType1 = dwSource.getData().getConType();
        List<ProcessorEntity> res = new ArrayList<>();
        SynchronousTypeEnum synchronousTypeEnum = dto.synchronousTypeEnum;
        TableNifiSettingPO tableNifiSettingPO = new TableNifiSettingPO();
        TableNifiSettingPO tableNifiSettingPO1 = new TableNifiSettingPO();
        if (dto.workflowDetailId != null) {
            tableNifiSettingPO1 = tableNifiSettingService.query().eq("app_id", dto.appId).eq("nifi_custom_workflow_detail_id", dto.workflowDetailId).eq("table_access_id", dto.id).eq("type", dto.type.getValue()).one();

        } else {
            tableNifiSettingPO1 = tableNifiSettingService.query().eq("app_id", dto.appId).eq("table_access_id", dto.id).eq("type", dto.type.getValue()).one();

        }
        if (tableNifiSettingPO1 != null) {
            tableNifiSettingPO = tableNifiSettingPO1;
        }
        tableNifiSettingPO.tableComponentId = groupId;
        tableNifiSettingPO.tableAccessId = Math.toIntExact(dto.id);
        tableNifiSettingPO.appId = Math.toIntExact(dto.appId);
        tableNifiSettingPO.type = dto.type.getValue();
        tableNifiSettingPO.tableName = config.targetDsConfig.targetTableName;
        //日志监控
        List<AutoEndBranchTypeEnum> autoEndBranchTypeEnums = new ArrayList<>();
        //失败的不连
        //autoEndBranchTypeEnums.add(AutoEndBranchTypeEnum.SUCCESS);
        autoEndBranchTypeEnums.add(AutoEndBranchTypeEnum.FAILURE);
        List<ProcessorEntity> processorEntities = pipelineSupervision(groupId, res, cfgDbPoolId, tableNifiSettingPO);
        String supervisionId = processorEntities.get(0).getId();
        //调度组件,在数据接入的时候调一次
       /* ProcessorEntity dispatchProcessor = new ProcessorEntity();
        ProcessorEntity publishKafkaProcessor = new ProcessorEntity();*/
        String inputPortId = "";
        createPublishKafkaProcessor(config, dto, groupId, 2, false);
        /*if (dto.groupStructureId == null) {
            dispatchProcessor = queryDispatchProcessor(config, groupId, cfgDbPoolId);
            //发送消息PublishKafka
            publishKafkaProcessor = createPublishKafkaProcessor(config, dto, groupId, 2);
            componentConnector(groupId, dispatchProcessor.getId(), publishKafkaProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
        }*/
        //原变量字段
        /**
         * Set Increment Field
         */
        ProcessorEntity evaluateJsonPathProcessor = evaluateJsonPathProcessor(groupId, 3);
        tableNifiSettingPO.setIncrementProcessorId = evaluateJsonPathProcessor.getId();

        //接受消息ConsumeKafka
        /**
         * ConsumeKafka
         */
        ProcessorEntity consumeKafkaProcessor = createConsumeKafkaProcessor(config, dto, groupId, 2);
        List<ProcessorEntity> processorEntityList = new ArrayList<>();
        processorEntityList.add(consumeKafkaProcessor);
        enabledProcessor(groupId, processorEntityList);
        try {
            ProcessorEntity processorEntity = NifiHelper.getProcessorsApi().getProcessor(consumeKafkaProcessor.getId());
            ProcessorRunStatusEntity processorRunStatusEntity = new ProcessorRunStatusEntity();
            processorRunStatusEntity.setDisconnectedNodeAcknowledged(false);
            processorRunStatusEntity.setRevision(processorEntity.getRevision());
            processorRunStatusEntity.setState(ProcessorRunStatusEntity.StateEnum.STOPPED);
            NifiHelper.getProcessorsApi().updateRunStatus(processorEntity.getId(), processorRunStatusEntity);
        } catch (ApiException e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR);
        }

        //连接器(中间那条线)
        componentConnector(groupId, consumeKafkaProcessor.getId(), evaluateJsonPathProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);

        tableNifiSettingPO.consumeKafkaProcessorId = consumeKafkaProcessor.getId();
        if (dto.groupStructureId != null) {
            //  创建input_port(组)
            //PositionDTO position = queryField.getComponent().getPosition();
            //inputPortId = buildPortComponent(config.taskGroupConfig.appName, groupId, position.getX(), position.getY(),
            //PortComponentEnum.COMPONENT_INPUT_PORT_COMPONENT);
        }
        //读取增量字段组件
        /**
         * Query Increment Field
         */
        ProcessorEntity queryField = queryIncrementFieldProcessor(config, groupId, cfgDbPoolId, dto);
        componentConnector(groupId, evaluateJsonPathProcessor.getId(), queryField.getId(), AutoEndBranchTypeEnum.MATCHED);
        tableNifiSettingPO.queryIncrementProcessorId = queryField.getId();
        //创建数据转换json组件
        /**
         * Convert Data To Json
         */
        ProcessorEntity jsonRes = convertJsonProcessor(groupId, 0, 5);
        tableNifiSettingPO.convertDataToJsonProcessorId = jsonRes.getId();
        //连接器
        componentConnector(groupId, queryField.getId(), jsonRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //componentsConnector(groupId, queryField.getId(), supervisionId, autoEndBranchTypeEnums);
        //字段转换nifi变量
        List<String> strings = new ArrayList<>();
        strings.add(NifiConstants.AttrConstants.INCREMENTAL_OBJECTIVESCORE_END);
        strings.add(NifiConstants.AttrConstants.INCREMENTAL_OBJECTIVESCORE_START);
        ProcessorEntity evaluateJson = evaluateTimeVariablesProcessor(groupId, strings);
        tableNifiSettingPO.evaluateTimeVariablesProcessorId = evaluateJson.getId();
        res.add(evaluateJsonPathProcessor);
        res.add(queryField);
        res.add(jsonRes);
        //连接器
        componentConnector(groupId, jsonRes.getId(), evaluateJson.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //componentsConnector(groupId, jsonRes.getId(), supervisionId, autoEndBranchTypeEnums);
        //创建log
        ProcessorEntity logProcessor = putLogProcessor(groupId, cfgDbPoolId, dto, config);
        tableNifiSettingPO.putLogToConfigDbProcessorId = logProcessor.getId();
        //连接器
        //这里要换,上面是evaluateJson,下面是logProcessor.接下来的组件赋予的变量值会覆盖上面的
        /**
         * 数据接入 数仓建模 ：数据开始时间 数据结束时间的组件
         */
        List<ProcessorEntity> processorEntities1 = buildDeltaTimeProcessorEntity(dto.deltaTimes, groupId, sourceDbPoolId, res, tableNifiSettingPO, supervisionId, autoEndBranchTypeEnums);
        if (CollectionUtils.isEmpty(processorEntities1)) {
            componentConnector(groupId, evaluateJson.getId(), logProcessor.getId(), AutoEndBranchTypeEnum.MATCHED);
        } else {
            componentConnector(groupId, evaluateJson.getId(), processorEntities1.get(0).getId(), AutoEndBranchTypeEnum.MATCHED);
            componentConnector(groupId, processorEntities1.get(processorEntities1.size() - 1).getId(), logProcessor.getId(), AutoEndBranchTypeEnum.MATCHED);
        }

        //创建执行删除组件
        /**
         * Exec Target Delete
         */
        ProcessorEntity delSqlRes;
        //doris数仓的nifi删除数据组件和别的不同
        if (DataSourceTypeEnum.DORIS.getName().equals(conType1.getName())) {
            delSqlRes = execDeleteSqlProcessorForDoris(config, groupId, targetDbPoolId, synchronousTypeEnum, dto);
        } else {
            delSqlRes = execDeleteSqlProcessor(config, groupId, targetDbPoolId, synchronousTypeEnum, dto);
        }

        tableNifiSettingPO.executeTargetDeleteProcessorId = delSqlRes.getId();
        //------------------------------------------

        //全量快照不新建组件 目前改变sql预览的sql即可
//        List<ProcessorEntity> generateVersions = buildgenerateVersionProcessorEntity(dto.generateVersionSql, groupId, targetDbPoolId, res, tableNifiSettingPO);
//        if (!CollectionUtils.isEmpty(generateVersions)) {
//            componentConnector(groupId, logProcessor.getId(), generateVersions.get(0).getId(), AutoEndBranchTypeEnum.SUCCESS);
//            componentConnector(groupId, generateVersions.get(generateVersions.size() - 1).getId(), delSqlRes.getId(), AutoEndBranchTypeEnum.RESPONSE);
//        } else {
//            componentConnector(groupId, logProcessor.getId(), delSqlRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
//        }

        //------------------------------------------
        componentConnector(groupId, logProcessor.getId(), delSqlRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        log.info("是否调用数据安全接口，{}", enable);

        /**
         * 如果是excel表格（sftp/ftp），他们的nifi流程组件与普通表的不同
         */
        if (dto.excelFlow && enable) {
            //ftp文件拷贝
            /**
             * replaceTextForFtpProcess组件，用于替换字符串为指定的格式
             */
            ProcessorEntity replaceTextForFtpProcess = replaceTextForFtpProcess(config, groupId, dto);
            tableNifiSettingPO.replaceTextForFtpProcessorId = replaceTextForFtpProcess.getId();
            componentConnector(groupId, delSqlRes.getId(), replaceTextForFtpProcess.getId(), AutoEndBranchTypeEnum.SUCCESS);

            /**
             * invokeHTTPForFtpProcessor组件，用于向指定的接口发送请求
             */
            ProcessorEntity invokeHTTPForFtpProcessor = invokeHTTPForFtpProcessor(groupId);
            tableNifiSettingPO.invokeHttpForFtpProcessorId = invokeHTTPForFtpProcessor.getId();
            componentConnector(groupId, replaceTextForFtpProcess.getId(), invokeHTTPForFtpProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
            res.add(replaceTextForFtpProcess);
            res.add(invokeHTTPForFtpProcessor);
        } else {
            tableNifiSettingPO.replaceTextForFtpProcessorId = null;
            tableNifiSettingPO.invokeHttpForFtpProcessorId = null;
        }
        //连接器
        componentsConnector(groupId, logProcessor.getId(), supervisionId, autoEndBranchTypeEnums);
        //执行查询组件
        ProcessorEntity executeSQLRecord = new ProcessorEntity();
        ProcessorEntity convertAvroToJSON = null;

//        //提取关联外键的sql
//        ProcessorEntity processorEntity3 = new ProcessorEntity();

        //pg2doris不需要调用存储过程
        ProcessorEntity processorEntity1 = new ProcessorEntity();
        List<ProcessorEntity> excelProcessorEntity = new ArrayList<>();

        //如果是 DWTOOLAP  则创建执行查询的组件：ExecuteSQLRecord
        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTODORIS)) {
            executeSQLRecord = createExecuteSQLRecordDoris(config, groupId, dto, targetDbPoolId);
        } else {
            // 如果是sapbw的流程
            if (dto.sapBwFlow) {
                /*
                 * createSapBwProcessorEntity会返回一个集合，
                 * 集合里面第一个是replaceTextForSapBwProcess组件
                 * 第二个是invokeHTTPProcessorForSapBw组件
                 */
                // 虽然名字叫这个 但是只是为了下面流程连线逻辑不大改，其实还是sapbw的流程
                excelProcessorEntity = createSapBwProcessorEntity(appGroupId, groupId, config, tableNifiSettingPO, supervisionId, autoEndBranchTypeEnums, dto);

                res.addAll(excelProcessorEntity);

                /*
                 * 如果是sftp/ftp的excel表格
                 */
            } else if (dto.excelFlow) {
                //excelProcessorEntity = createExcelProcessorEntity(appGroupId, groupId, config, tableNifiSettingPO, supervisionId, autoEndBranchTypeEnums, dto);
                /*
                 * createExcelProcessorEntity2会返回一个集合，
                 * 集合里面第一个是replaceTextForFtpProcess组件
                 * 第二个是invokeHTTPForFtpProcessor组件
                 */
                excelProcessorEntity = createExcelProcessorEntity2(appGroupId, groupId, config, tableNifiSettingPO, supervisionId, autoEndBranchTypeEnums, dto);

                res.addAll(excelProcessorEntity);

                /*
                 * 如果是powerbi类型
                 */
            } else if (Objects.equals(DataSourceTypeEnum.POWERBI_DATASETS, conType)) {
                //excelProcessorEntity = createExcelProcessorEntity(appGroupId, groupId, config, tableNifiSettingPO, supervisionId, autoEndBranchTypeEnums, dto);
                /*
                 * createExcelProcessorEntity2会返回一个集合，
                 * 集合里面第一个是replaceTextForFtpProcess组件
                 * 第二个是invokeHTTPForFtpProcessor组件
                 */
                excelProcessorEntity = createSapBwProcessorEntity3(appGroupId, groupId, config, tableNifiSettingPO, supervisionId, autoEndBranchTypeEnums, dto);

                res.addAll(excelProcessorEntity);

                /*
                 * 如果是api类型
                 */
            } else if (conType != null && Objects.equals(DataSourceTypeEnum.API, conType)) {
                /*
                 * createExcelProcessorEntity2会返回一个集合，
                 * 集合里面第一个是replaceTextForFtpProcess组件
                 * 第二个是invokeHTTPForFtpProcessor组件
                 */
                excelProcessorEntity = createApiProcessorEntity(appGroupId, groupId, config, tableNifiSettingPO, supervisionId, autoEndBranchTypeEnums, dto);

                res.addAll(excelProcessorEntity);

                /*
                 * 如果是普通表（数接的物理表、数仓的事实表和维度表）的nifi流程，则创建这个执行查询的组件：ExecuteSQLRecord
                 */
            } else {
                //根据dmp_dw类型 判断执行查询的组件的样式
                if (DataSourceTypeEnum.DORIS.getName().equals(conType1.getName())) {
                    executeSQLRecord = createExecuteSQLRecordForDoris(appGroupId, config, groupId, dto, sourceDbPoolId, tableNifiSettingPO);
                } else {
                    //mongodb的查询组件和别的不一样  这个conType是数据接入-应用引用的数据源的类型
                    if (DataSourceTypeEnum.MONGODB.getName().equals(conType.getName())) {
                        //如果是mongodb任务，则需要在GetMongo和 执行删除临时表数据的组件之间，安插一个ConvertAvroToJSON组件
                        //将Exec Target Delete组件流出的avro结构转换成json结构 不然GetMongo组件会报错
                        convertAvroToJSON = createConvertAvroToJSON(groupId, dto);
                        executeSQLRecord = createExecuteSQLRecordForMongoDB(appGroupId, config, groupId, dto, sourceDbPoolId, tableNifiSettingPO);
                    } else {
                        executeSQLRecord = createExecuteSQLRecord(appGroupId, config, groupId, dto, sourceDbPoolId, tableNifiSettingPO);
                    }
                }

                componentsConnector(groupId, executeSQLRecord.getId(), supervisionId, autoEndBranchTypeEnums);
            }
        }

        if (executeSQLRecord.getId() == null) {
            //连接器
            ProcessorEntity processorEntity = excelProcessorEntity.get(0);
            componentConnector(groupId, delSqlRes.getId(), processorEntity.getId(), AutoEndBranchTypeEnum.SUCCESS);
            processorEntity1 = excelProcessorEntity.get(excelProcessorEntity.size() - 1);

            //2023-06-28李世纪新增
            //新增自己连自己 为了解决管道调度该组件时，偶发性的失败问题，失败就retry
            componentConnector(groupId, processorEntity1.getId(), processorEntity1.getId(), AutoEndBranchTypeEnum.RETRY2);

        } else {
            tableNifiSettingPO.executeSqlRecordProcessorId = executeSQLRecord.getId();
            if (convertAvroToJSON != null) {
                componentConnector(groupId, delSqlRes.getId(), convertAvroToJSON.getId(), AutoEndBranchTypeEnum.SUCCESS);
                componentConnector(groupId, convertAvroToJSON.getId(), executeSQLRecord.getId(), AutoEndBranchTypeEnum.SUCCESS);
            } else {
                //连接器
                componentConnector(groupId, delSqlRes.getId(), executeSQLRecord.getId(), AutoEndBranchTypeEnum.SUCCESS);
            }

            processorEntity1 = executeSQLRecord;
        }

        componentsConnector(groupId, delSqlRes.getId(), supervisionId, autoEndBranchTypeEnums);
        //supervisionId指的是nifi组件中报错后连接的第一个组件：queryForPipelineSupervision组件的id
        componentConnector(groupId, processorEntity1.getId(), supervisionId, AutoEndBranchTypeEnum.FAILURE);

        String lastId = "";
        Boolean isLastId = true;
        List<ProcessorEntity> entities = new ArrayList<>();
        if (!Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTODORIS)) {
            isLastId = false;
            ProcessorEntity putDatabaseRecord = null;

            // 如果是数据接入excel || 数据接入sapbw || 数据接入api || 数据接入powerbi
            if (dto.excelFlow
                    || dto.sapBwFlow
                    || (conType != null && Objects.equals(DataSourceTypeEnum.API, conType))
                    || (conType != null && Objects.equals(DataSourceTypeEnum.POWERBI_DATASETS, conType))
            ) {
                ProcessorEntity IHP = new ProcessorEntity();
                //如果开启数据校验，则新建两个组件
                if (dataValidation) {
                    ProcessorEntity generateFlowFile = new ProcessorEntity();

                    if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
                        generateFlowFile = replaceTextProcessv1(config, groupId, dto);
                    } else if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTOPG)) {
                        generateFlowFile = replaceTextForDwProcessv1(config, groupId, dto);
                    }

                    tableNifiSettingPO.generateFlowFileProcessorId = generateFlowFile.getId();
                    componentConnector(groupId, processorEntity1.getId(), generateFlowFile.getId(), AutoEndBranchTypeEnum.RESPONSE);
                    IHP = invokeHTTPProcessorv1(groupId);

                    componentConnector(groupId, IHP.getId(), supervisionId, AutoEndBranchTypeEnum.FAILURE2);
                    //2024-01-09李世纪新增
                    //新增自己连自己 为了解决管道调度该组件时，偶发性的失败问题，失败就retry
                    componentConnector(groupId, IHP.getId(), IHP.getId(), AutoEndBranchTypeEnum.RETRY2);
                    componentConnector(groupId, generateFlowFile.getId(), IHP.getId(), AutoEndBranchTypeEnum.SUCCESS);
                    tableNifiSettingPO.invokeHttpProcessorId = IHP.getId();
                    res.add(generateFlowFile);
                    res.add(IHP);
                }

//                processorEntity3 = executeSqlRecordForKey(config, groupId, targetDbPoolId, synchronousTypeEnum, dto);

                tableNifiSettingPO.saveTargetDbProcessorId = null;
                /**
                 * CallDbProcedure 执行sql预览的sql
                 */
                ProcessorEntity processorEntity2 = CallDbProcedurev1(config, groupId, targetDbPoolId, synchronousTypeEnum, dto);
                if (IHP.getId() != null) {
                    componentConnector(groupId, IHP.getId(), processorEntity2.getId(), AutoEndBranchTypeEnum.RESPONSE);
                } else {
                    componentConnector(groupId, processorEntity1.getId(), processorEntity2.getId(), AutoEndBranchTypeEnum.RESPONSE);
                }

                processorEntity1 = processorEntity2;
                tableNifiSettingPO.updateFieldProcessorId = null;
                tableNifiSettingPO.updateFieldForCodeProcessorId = null;
                tableNifiSettingPO.saveTargetDbProcessorId = null;
            } else {
                //mongodb来源的表流程和其他的普通表不一样
                if (DataSourceTypeEnum.MONGODB.equals(conType)) {
                    /**
                     * ConvertMongoJsonToAvro 将GetMongo组件查询道德数据从Json格式转换为Avro格式
                     */
                    ProcessorEntity updateField = createConvertRecord(appGroupId, config, groupId, dto, tableNifiSettingPO);
                    tableNifiSettingPO.updateFieldProcessorId = updateField.getId();
                    componentConnector(groupId, processorEntity1.getId(), updateField.getId(), AutoEndBranchTypeEnum.SUCCESS);
                    /**
                     * UpdateRecordForMongo  加字段  大小批次号
                     */
                    //加批量字段值
                    ProcessorEntity updateField1 = createUpdateRecordForMongo(appGroupId, config, groupId, dto, tableNifiSettingPO);
                    componentConnector(groupId, updateField.getId(), updateField1.getId(), AutoEndBranchTypeEnum.SUCCESS);
                    tableNifiSettingPO.updateFieldForCodeProcessorId = updateField1.getId();
                    //数据入库
                    /**
                     * PutRecordToStgForMongo 将获取到的mongo数据插入到stg临时表
                     */
                    putDatabaseRecord = createPutDatabaseRecordForMongo(appGroupId, config, groupId, dto, targetDbPoolId, synchronousTypeEnum, tableNifiSettingPO);
                    tableNifiSettingPO.saveTargetDbProcessorId = putDatabaseRecord.getId();
                    //连接器c
                    componentConnectorForNifiClusterEnable(groupId, updateField1.getId(), putDatabaseRecord.getId(), AutoEndBranchTypeEnum.SUCCESS);
                    componentsConnector(groupId, updateField1.getId(), supervisionId, autoEndBranchTypeEnums);
                    res.add(updateField);
                    res.add(updateField1);
                } else {
                    /**
                     * UpdateRecord 1 字段映射转换
                     */
                    //字段映射转换
                    ProcessorEntity updateField = createUpdateField(appGroupId, config, groupId, dto, tableNifiSettingPO);
                    tableNifiSettingPO.updateFieldProcessorId = updateField.getId();
                    componentConnector(groupId, processorEntity1.getId(), updateField.getId(), AutoEndBranchTypeEnum.SUCCESS);
                    //componentsConnector(groupId, processorEntity1.getId(), supervisionId, autoEndBranchTypeEnums);
                    /**
                     * UpdateRecord 2 加字段  大小批次号
                     */
                    //加批量字段值
                    ProcessorEntity updateField1 = createUpdateField1(appGroupId, config, groupId, dto, tableNifiSettingPO);
                    componentConnector(groupId, updateField.getId(), updateField1.getId(), AutoEndBranchTypeEnum.SUCCESS);
                    tableNifiSettingPO.updateFieldForCodeProcessorId = updateField1.getId();
                    //数据入库
                    /**
                     * executeSQLRecord 数据插入到临时表
                     */
                    putDatabaseRecord = createPutDatabaseRecord(appGroupId, config, groupId, dto, targetDbPoolId, synchronousTypeEnum, tableNifiSettingPO);
                    tableNifiSettingPO.saveTargetDbProcessorId = putDatabaseRecord.getId();
                    //连接器c
                    componentConnectorForNifiClusterEnable(groupId, updateField1.getId(), putDatabaseRecord.getId(), AutoEndBranchTypeEnum.SUCCESS);
                    componentsConnector(groupId, updateField1.getId(), supervisionId, autoEndBranchTypeEnums);
                    res.add(updateField);
                    res.add(updateField1);
                }

//                //todo:当数仓数据库类型是doris的时候,并且数仓的关联外键语句是多个update,则使用多个nifi执行sql组件去执行拆分后的关联外键sql
//                String updateSql = dto.getUpdateSql();
//                log.info("数仓建模关联外键语句！！：" + updateSql);
//                if (StringUtils.isNotEmpty(updateSql)) {
//
//                    //如果是doris
//                    if (DataSourceTypeEnum.DORIS.getName().equals(conType1.getName())) {
//                        String[] split = updateSql.split(";");
//                        //如果关联了多个外键  则使用多个nifi执行sql组件去执行拆分后的关联外键sql
//                        if (split.length > 1) {
//                            for (int i = 0; i < split.length; i++) {
//                                if ("".equals(split[i])) {
//                                    continue;
//                                }
//
//                                ProcessorEntity updateDorisKeySqlEntity = new ProcessorEntity();
//                                //替换要执行的关联外键语句
//                                dto.updateSql = split[i] + ";";
//                                /**
//                                 * UpdateDorisKeySql 专门用于执行doris关联外键时的多个update语句
//                                 */
//                                updateDorisKeySqlEntity = CallDbProcedureForDorisKeySql(config, groupId, targetDbPoolId, synchronousTypeEnum, dto, i);
//                                //连接报错处理组件
//                                componentConnector(groupId, updateDorisKeySqlEntity.getId(), supervisionId, AutoEndBranchTypeEnum.FAILURE);
//                                entities.add(updateDorisKeySqlEntity);
//
//                            }
//                            //doris关联外键组件群内部自己连接
//                            for (int i = 0; i < entities.size(); i++) {
//                                if (i < entities.size() - 1) {
//                                    componentConnector(groupId, entities.get(i).getId(), entities.get(i + 1).getId(), AutoEndBranchTypeEnum.SUCCESS);
//                                }
//                            }
//
//                        }
//
//                    }
//                }
//                //doris关联外键更新组件群的第一个要跟前面的执行插入数据到临时表的组件进行连接
//                if (!CollectionUtils.isEmpty(entities)) {
//                    String id = entities.get(0).getId();
//                    //连接器
//                    componentConnector(groupId, putDatabaseRecord.getId(), id, AutoEndBranchTypeEnum.SUCCESS);
//                }
//                if (!CollectionUtils.isEmpty(entities)) {
//                    dto.updateSql = "";
//                }

                //如果是doris
                if (DataSourceTypeEnum.DORIS.getName().equals(conType1.getName())) {
                    /**
                     * CallDbProcedure 该组件执行sql预览的sql
                     */
                    processorEntity1 = CallDbProcedureForDoris(config, groupId, targetDbPoolId, synchronousTypeEnum, dto);
                } else {
                    /**
                     * CallDbProcedure 该组件执行sql预览的sql
                     */
                    processorEntity1 = CallDbProcedure(config, groupId, targetDbPoolId, synchronousTypeEnum, dto);
                }

            }

            //合并流文件组件
            //ProcessorEntity mergeRes = mergeContentProcessor(groupId);
            //tableNifiSettingPO.mergeContentProcessorId = mergeRes.getId();
            //连接器
            //componentConnector(groupId, putDatabaseRecord.getId(), mergeRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
            //用组件,调存储过程把stg里的数据向ods里面插入
            ProcessorEntity invokeHTTP = new ProcessorEntity();

            /**
             * 20230706 sjli
             * 是否开启数据校验
             */
            if (
                    !dto.excelFlow
                            && !dto.sapBwFlow
                            && !(conType != null && Objects.equals(DataSourceTypeEnum.API, conType))
                            && !(conType != null && Objects.equals(DataSourceTypeEnum.POWERBI_DATASETS, conType))
            ) {
                if (dataValidation) {
                    if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.TOPGODS) || Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTOPG)) {
                        //---------------------------------
                        // todo 数据验证
                        ProcessorEntity generateFlowFile = new ProcessorEntity();

                        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
                            generateFlowFile = replaceTextProcess(config, groupId, dto);
                        } else if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTOPG)) {
                            generateFlowFile = replaceTextForDwProcess(config, groupId, dto);
                        }

                        tableNifiSettingPO.generateFlowFileProcessorId = generateFlowFile.getId();
                        componentConnector(groupId, putDatabaseRecord.getId(), generateFlowFile.getId(), AutoEndBranchTypeEnum.SUCCESS);
                        invokeHTTP = invokeHTTPProcessor(groupId);
                        componentConnector(groupId, generateFlowFile.getId(), invokeHTTP.getId(), AutoEndBranchTypeEnum.SUCCESS);
                        tableNifiSettingPO.invokeHttpProcessorId = invokeHTTP.getId();
                        res.add(generateFlowFile);
                        res.add(invokeHTTP);
                        //-----------------------------------
                    }
                }
            }
            tableNifiSettingPO.odsToStgProcessorId = processorEntity1.getId();
            //连接器
            if (invokeHTTP.getId() != null) {
                //新增自己连自己 为了解决管道调度该组件时，偶发性的失败问题，失败就retry
                componentConnector(groupId, invokeHTTP.getId(), invokeHTTP.getId(), AutoEndBranchTypeEnum.RETRY2);
                componentConnector(groupId, invokeHTTP.getId(), supervisionId, AutoEndBranchTypeEnum.FAILURE2);
                componentConnector(groupId, invokeHTTP.getId(), processorEntity1.getId(), AutoEndBranchTypeEnum.RESPONSE);
                componentConnector(groupId, invokeHTTP.getId(), processorEntity1.getId(), AutoEndBranchTypeEnum.NO_RETRY2);
            } else {
                //如果doris关联外键的语句执行组件有多个  则将最后一个组件连线到执行sql预览sql的组件上
                if (!CollectionUtils.isEmpty(entities)) {
                    String id = entities.get(entities.size() - 1).getId();
                    //连接器
                    componentConnector(groupId, id, processorEntity1.getId(), AutoEndBranchTypeEnum.SUCCESS);
                } else if (putDatabaseRecord != null && putDatabaseRecord.getId() != null) {
                    componentConnector(groupId, putDatabaseRecord.getId(), processorEntity1.getId(), AutoEndBranchTypeEnum.SUCCESS);
                }
            }
            if (putDatabaseRecord != null && putDatabaseRecord.getId() != null) {
                componentsConnector(groupId, putDatabaseRecord.getId(), supervisionId, autoEndBranchTypeEnums);
                res.add(putDatabaseRecord);
            }

        }
        //查询条数
        ProcessorEntity queryNumbers;
        if (conType1.getName().equals(DataSourceTypeEnum.DORIS.getName())) {
            queryNumbers = queryNumbersProcessorForDoris(dto, config, groupId, cfgDbPoolId);
        } else {
            queryNumbers = queryNumbersProcessor(dto, config, groupId, targetDbPoolId);
        }

        tableNifiSettingPO.queryNumbersProcessorId = queryNumbers.getId();
        //连接器
        componentConnector(groupId, processorEntity1.getId(), queryNumbers.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, processorEntity1.getId(), supervisionId, autoEndBranchTypeEnums);
        //转json
        ProcessorEntity numberToJsonRes = convertJsonProcessor(groupId, 0, 15);
        tableNifiSettingPO.convertNumbersToJsonProcessorId = numberToJsonRes.getId();
        //连接器
        componentConnector(groupId, queryNumbers.getId(), numberToJsonRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, queryNumbers.getId(), supervisionId, autoEndBranchTypeEnums);
        //定义占位符
        ProcessorEntity evaluateJsons = evaluateNumbersProcessor(groupId);
        tableNifiSettingPO.setNumbersProcessorId = evaluateJsons.getId();
        //连接器
        componentConnector(groupId, numberToJsonRes.getId(), evaluateJsons.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, numberToJsonRes.getId(), supervisionId, autoEndBranchTypeEnums);
        //更新日志
        ProcessorEntity processorEntity = CallDbLogProcedure(config, groupId, cfgDbPoolId);
        tableNifiSettingPO.saveNumbersProcessorId = processorEntity.getId();
        //连接器
        componentConnector(groupId, evaluateJsons.getId(), processorEntity.getId(), AutoEndBranchTypeEnum.MATCHED);
        //componentsConnector(groupId, processorEntity.getId(), supervisionId, autoEndBranchTypeEnums);
        ProcessorEntity publishKafkaForPipelineProcessor = createPublishKafkaForPipelineProcessor(config, dto, groupId, 1, 17);
        tableNifiSettingPO.publishKafkaPipelineProcessorId = publishKafkaForPipelineProcessor.getId();
        //连接器
        componentConnector(groupId, evaluateJsons.getId(), publishKafkaForPipelineProcessor.getId(), AutoEndBranchTypeEnum.MATCHED);
        componentsConnector(groupId, publishKafkaForPipelineProcessor.getId(), supervisionId, autoEndBranchTypeEnums);
        lastId = processorEntity.getId();
        //res.add(mergeRes);

        //组件放这里面 启动用！
        res.add(processorEntity1);
        res.add(queryNumbers);
        res.add(numberToJsonRes);
        res.add(evaluateJsons);
        res.add(processorEntity);
        res.add(publishKafkaForPipelineProcessor);
        if (!CollectionUtils.isEmpty(entities)) {
            res.addAll(entities);
        }
        if (convertAvroToJSON != null) {
            res.add(convertAvroToJSON);
        }

        ProcessorEntity processor = null;
        Double processorX = 0d;
        Double processorY = 0d;
        try {
            processor = NifiHelper.getProcessorsApi().getProcessor(lastId);
            processorX = processor.getPosition().getX();
            processorY = processor.getPosition().getY();
            System.out.println("processorX = " + processorX);
            System.out.println("processorY = " + processorY);
        } catch (ApiException e) {
            log.error("获取任务组的组件失败,【返回信息为：】,{}", processor);
        }

        if (dto.groupStructureId != null) {

            // TODO 创建output_port(组)   (后期入库)
            String outputPortId = buildPortComponent(config.taskGroupConfig.appName, groupId, processorX, processorY,
                    PortComponentEnum.COMPONENT_OUTPUT_PORT_COMPONENT);


            // 创建output_port connection(组)
            String componentOutputPortConnectionId = "";
            if (isLastId) {
                componentOutputPortConnectionId = buildPortConnection(groupId,
                        groupId, outputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
                        groupId, lastId, ConnectableDTO.TypeEnum.PROCESSOR,
                        3, PortComponentEnum.COMPONENT_OUTPUT_PORT_CONNECTION);
            } else {
                componentOutputPortConnectionId = buildPortConnection(groupId,
                        groupId, outputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
                        groupId, lastId, ConnectableDTO.TypeEnum.PROCESSOR,
                        3, PortComponentEnum.COMPONENT_OUTPUT_PORT_CONNECTION);
            }


            // 创建output connection(任务)
            String taskOutputPortConnectionId = buildPortConnection(groupEntityId,
                    groupEntityId, tableOutputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
                    taskGroupEntityId, outputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
                    2, PortComponentEnum.TASK_OUTPUT_PORT_CONNECTION);

            // 创建output connection(应用)
//        String appOutputPortConnectionId = buildPortConnection(appParentGroupId,
//                appParentGroupId, appOutputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
//                appGroupId, tableOutputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
//                1, PortComponentEnum.APP_OUTPUT_PORT_CONNECTION);

            tableNifiSettingPO.processorOutputPortConnectId = componentOutputPortConnectionId;
            tableNifiSettingPO.tableOutputPortConnectId = taskOutputPortConnectionId;
            tableNifiSettingPO.processorOutputPortId = outputPortId;
        }
        tableNifiSettingPO.tableInputPortId = tableInputPortId;
        tableNifiSettingPO.tableOutputPortId = tableOutputPortId;
        tableNifiSettingPO.processorInputPortId = inputPortId;
        tableNifiSettingPO.nifiCustomWorkflowDetailId = dto.workflowDetailId;
        tableNifiSettingPO.selectSql = config.processorConfig.sourceExecSqlQuery;
        tableNifiSettingPO.type = dto.type.getValue();
        tableNifiSettingPO.syncMode = config.targetDsConfig.syncMode;
        appNifiSettingPO.nifiCustomWorkflowId = dto.nifiCustomWorkflowId;
        appNifiSettingService.saveOrUpdate(appNifiSettingPO);
        //res.add(queryField);

        //res.add(jsonRes);
        res.add(evaluateJson);
        res.add(logProcessor);
        res.add(delSqlRes);
        if (executeSQLRecord.getId() != null) {
            res.add(executeSQLRecord);
        }
        res.addAll(processorEntities);
        if (dto.groupStructureId == null) {
            //tableNifiSettingPO.dispatchComponentId = dispatchProcessor.getId();
            //tableNifiSettingPO.publishKafkaProcessorId = publishKafkaProcessor.getId();
            //res.add(publishKafkaProcessor);
            res.add(consumeKafkaProcessor);
            //res.add(dispatchProcessor);
        }
        tableNifiSettingService.saveOrUpdate(tableNifiSettingPO);
        return res;
    }


    private List<ProcessorEntity> buildProcessorVersion3(String appGroupId, DataAccessConfigDTO config, String groupId,
                                                         String sourceDbPoolId, String targetDbPoolId, String cfgDbPoolId,
                                                         BuildNifiFlowDTO dto, BuildTableServiceDTO buildTableService, Integer targetDbId) {
        List<ProcessorEntity> res = new ArrayList<>();
        SynchronousTypeEnum synchronousTypeEnum = dto.synchronousTypeEnum;
        TableNifiSettingPO tableNifiSettingPO = new TableNifiSettingPO();
        TableNifiSettingPO tableNifiSettingPO1 = new TableNifiSettingPO();
        tableNifiSettingPO1 = tableNifiSettingService.query().eq("table_access_id", dto.id).eq("type", dto.type.getValue()).one();
        if (tableNifiSettingPO1 != null) {
            tableNifiSettingPO = tableNifiSettingPO1;
        }
        tableNifiSettingPO.tableComponentId = groupId;
        tableNifiSettingPO.appId = buildTableService.tableAppId;
        tableNifiSettingPO.tableAccessId = Math.toIntExact(dto.id);
        tableNifiSettingPO.type = dto.type.getValue();
        tableNifiSettingPO.tableName = config.targetDsConfig.targetTableName;
        //日志监控
        List<AutoEndBranchTypeEnum> autoEndBranchTypeEnums = new ArrayList<>();
        autoEndBranchTypeEnums.add(AutoEndBranchTypeEnum.FAILURE);
        List<ProcessorEntity> processorEntities = pipelineSupervision(groupId, res, cfgDbPoolId, tableNifiSettingPO);
        String supervisionId = processorEntities.get(0).getId();
        //调度组件,在数据接入的时候调一次
        String inputPortId = "";
        ProcessorEntity dispatchProcessor = queryDispatchProcessor(config, groupId, cfgDbPoolId, dto);
        //发送消息PublishKafka
        ProcessorEntity processorEntity2 = convertJsonProcessor(groupId, 0, 2);
        res.add(processorEntity2);
        ProcessorEntity publishKafkaProcessor = createPublishKafkaProcessor(config, dto, groupId, 2, true);
        componentConnector(groupId, dispatchProcessor.getId(), processorEntity2.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentConnector(groupId, processorEntity2.getId(), publishKafkaProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //原变量字段
        ProcessorEntity evaluateJsonPathProcessor = evaluateJsonPathProcessor(groupId, 3);
        tableNifiSettingPO.setIncrementProcessorId = evaluateJsonPathProcessor.getId();

        //接受消息ConsumeKafka
        ProcessorEntity consumeKafkaProcessor = createConsumeKafkaProcessor(config, dto, groupId, 2);
        List<ProcessorEntity> processorEntityList = new ArrayList<>();
        processorEntityList.add(consumeKafkaProcessor);
        enabledProcessor(groupId, processorEntityList);
        try {
            ProcessorEntity processorEntity = NifiHelper.getProcessorsApi().getProcessor(consumeKafkaProcessor.getId());
            ProcessorRunStatusEntity processorRunStatusEntity = new ProcessorRunStatusEntity();
            processorRunStatusEntity.setDisconnectedNodeAcknowledged(false);
            processorRunStatusEntity.setRevision(processorEntity.getRevision());
            processorRunStatusEntity.setState(ProcessorRunStatusEntity.StateEnum.STOPPED);
            NifiHelper.getProcessorsApi().updateRunStatus(processorEntity.getId(), processorRunStatusEntity);
        } catch (ApiException e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR);
        }

        componentConnector(groupId, consumeKafkaProcessor.getId(), evaluateJsonPathProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);

        tableNifiSettingPO.consumeKafkaProcessorId = consumeKafkaProcessor.getId();
        //读取增量字段组件
        config.processorConfig.targetTableName = buildTableService.schemaName + "." + buildTableService.targetTable;
        ProcessorEntity queryField = queryIncrementFieldProcessor(config, groupId, cfgDbPoolId, dto);
        config.processorConfig.targetTableName = buildTableService.targetTable;
        componentConnector(groupId, evaluateJsonPathProcessor.getId(), queryField.getId(), AutoEndBranchTypeEnum.MATCHED);
        tableNifiSettingPO.queryIncrementProcessorId = queryField.getId();
        //创建数据转换json组件
        ProcessorEntity jsonRes = convertJsonProcessor(groupId, 0, 5);
        tableNifiSettingPO.convertDataToJsonProcessorId = jsonRes.getId();
        //连接器
        componentConnector(groupId, queryField.getId(), jsonRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //componentsConnector(groupId, queryField.getId(), supervisionId, autoEndBranchTypeEnums);
        //字段转换nifi变量
        List<String> strings = new ArrayList<>();
        strings.add(NifiConstants.AttrConstants.INCREMENTAL_OBJECTIVESCORE_END);
        strings.add(NifiConstants.AttrConstants.INCREMENTAL_OBJECTIVESCORE_START);
        ProcessorEntity evaluateJson = evaluateTimeVariablesProcessor(groupId, strings);
        tableNifiSettingPO.evaluateTimeVariablesProcessorId = evaluateJson.getId();
        res.add(evaluateJsonPathProcessor);
        res.add(queryField);
        res.add(jsonRes);
        //连接器
        componentConnector(groupId, jsonRes.getId(), evaluateJson.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //componentsConnector(groupId, jsonRes.getId(), supervisionId, autoEndBranchTypeEnums);
        //创建log
        ProcessorEntity logProcessor = putLogProcessor(groupId, cfgDbPoolId, dto, config);
        tableNifiSettingPO.putLogToConfigDbProcessorId = logProcessor.getId();
        //连接器
        // 这里要换,上面是evaluateJson,下面是logProcessor.接下来的组件赋予的变量值会覆盖上面的
        List<ProcessorEntity> processorEntities1 = buildDeltaTimeProcessorEntity(dto.deltaTimes, groupId, sourceDbPoolId, res, tableNifiSettingPO, supervisionId, autoEndBranchTypeEnums);
        if (CollectionUtils.isEmpty(processorEntities1)) {
            componentConnector(groupId, evaluateJson.getId(), logProcessor.getId(), AutoEndBranchTypeEnum.MATCHED);
        } else {
            componentConnector(groupId, evaluateJson.getId(), processorEntities1.get(0).getId(), AutoEndBranchTypeEnum.MATCHED);
            componentConnector(groupId, processorEntities1.get(processorEntities1.size() - 1).getId(), logProcessor.getId(), AutoEndBranchTypeEnum.MATCHED);
        }


        //创建执行删除组件
        ProcessorEntity delSqlRes = execBeforeSqlProcessor(groupId, targetDbPoolId, buildTableService.syncModeDTO.customScriptBefore);
        tableNifiSettingPO.executeTargetDeleteProcessorId = delSqlRes.getId();
        //------------------------------------------
        List<ProcessorEntity> generateVersions = buildgenerateVersionProcessorEntity(dto.generateVersionSql, groupId, targetDbPoolId, res, tableNifiSettingPO);
        if (!CollectionUtils.isEmpty(generateVersions)) {
            componentConnector(groupId, logProcessor.getId(), generateVersions.get(0).getId(), AutoEndBranchTypeEnum.SUCCESS);
            componentConnector(groupId, generateVersions.get(generateVersions.size() - 1).getId(), delSqlRes.getId(), AutoEndBranchTypeEnum.RESPONSE);
        } else {
            componentConnector(groupId, logProcessor.getId(), delSqlRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        }
        //------------------------------------------

        //连接器
        componentsConnector(groupId, logProcessor.getId(), supervisionId, autoEndBranchTypeEnums);
        //执行查询组件
        //pg2doris不需要调用存储过程
        ProcessorEntity executeSQLRecord = createExecuteSQLRecord(appGroupId, config, groupId, dto, sourceDbPoolId, tableNifiSettingPO);
        tableNifiSettingPO.executeSqlRecordProcessorId = executeSQLRecord.getId();
        //连接器
        componentConnector(groupId, delSqlRes.getId(), executeSQLRecord.getId(), AutoEndBranchTypeEnum.SUCCESS);
        ProcessorEntity processorEntity1 = executeSQLRecord;
        componentsConnector(groupId, delSqlRes.getId(), supervisionId, autoEndBranchTypeEnums);
        componentsConnector(groupId, processorEntity1.getId(), supervisionId, autoEndBranchTypeEnums);

        //字段映射转换
        ProcessorEntity updateField = createUpdateField(appGroupId, config, groupId, dto, tableNifiSettingPO);
        tableNifiSettingPO.updateFieldProcessorId = updateField.getId();
        componentConnector(groupId, processorEntity1.getId(), updateField.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //componentsConnector(groupId, processorEntity1.getId(), supervisionId, autoEndBranchTypeEnums);
        //加批量字段值
        ProcessorEntity updateField1 = createUpdateField1(appGroupId, config, groupId, dto, tableNifiSettingPO);
        componentConnector(groupId, updateField.getId(), updateField1.getId(), AutoEndBranchTypeEnum.SUCCESS);
        tableNifiSettingPO.updateFieldForCodeProcessorId = updateField1.getId();
        //数据入库
        ProcessorEntity putDatabaseRecord = createPutDatabase(appGroupId, config, groupId, dto, targetDbPoolId, synchronousTypeEnum, tableNifiSettingPO, buildTableService);
        tableNifiSettingPO.saveTargetDbProcessorId = putDatabaseRecord.getId();
        //连接器
        componentConnector(groupId, updateField1.getId(), putDatabaseRecord.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, updateField1.getId(), supervisionId, autoEndBranchTypeEnums);
        componentsConnector(groupId, putDatabaseRecord.getId(), supervisionId, autoEndBranchTypeEnums);
        res.add(putDatabaseRecord);
        res.add(updateField);
        res.add(updateField1);

        //查询条数
        String fullTableName = buildTableService.schemaName + "." + buildTableService.targetTable;
        config.processorConfig.targetTableName = fullTableName;
        ProcessorEntity queryNumbers = queryNumbers(dto, config, groupId, targetDbPoolId, buildTableService.syncModeDTO.customScriptAfter, targetDbId);
        tableNifiSettingPO.queryNumbersProcessorId = queryNumbers.getId();
        //连接器
        componentConnector(groupId, putDatabaseRecord.getId(), queryNumbers.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, processorEntity1.getId(), supervisionId, autoEndBranchTypeEnums);
        //转json
        ProcessorEntity numberToJsonRes = convertJsonProcessor(groupId, 0, 14);
        tableNifiSettingPO.convertNumbersToJsonProcessorId = numberToJsonRes.getId();
        //连接器
        componentConnector(groupId, queryNumbers.getId(), numberToJsonRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, queryNumbers.getId(), supervisionId, autoEndBranchTypeEnums);
        //定义占位符
        ProcessorEntity evaluateJsons = evaluateNumbersProcessor(groupId);
        tableNifiSettingPO.setNumbersProcessorId = evaluateJsons.getId();
        //连接器
        componentConnector(groupId, numberToJsonRes.getId(), evaluateJsons.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, numberToJsonRes.getId(), supervisionId, autoEndBranchTypeEnums);
        //更新日志
        config.targetDsConfig.targetTableName = fullTableName;
        ProcessorEntity processorEntity = CallDbLogProcedure(config, groupId, cfgDbPoolId);
        tableNifiSettingPO.saveNumbersProcessorId = processorEntity.getId();
        //连接器
        componentConnector(groupId, evaluateJsons.getId(), processorEntity.getId(), AutoEndBranchTypeEnum.MATCHED);
        //componentsConnector(groupId, processorEntity.getId(), supervisionId, autoEndBranchTypeEnums);
        ProcessorEntity publishKafkaForPipelineProcessor = createPublishKafkaForPipelineProcessor(config, dto, groupId, 1, 16);
        tableNifiSettingPO.publishKafkaPipelineProcessorId = publishKafkaForPipelineProcessor.getId();
        //连接器
        componentConnector(groupId, evaluateJsons.getId(), publishKafkaForPipelineProcessor.getId(), AutoEndBranchTypeEnum.MATCHED);
        componentsConnector(groupId, publishKafkaForPipelineProcessor.getId(), supervisionId, autoEndBranchTypeEnums);
        //res.add(mergeRes);
        res.add(processorEntity1);
        res.add(queryNumbers);
        res.add(numberToJsonRes);
        res.add(evaluateJsons);
        res.add(processorEntity);
        res.add(publishKafkaForPipelineProcessor);

        tableNifiSettingPO.tableInputPortId = tableInputPortId;
        tableNifiSettingPO.tableOutputPortId = tableOutputPortId;
        tableNifiSettingPO.processorInputPortId = inputPortId;
        tableNifiSettingPO.nifiCustomWorkflowDetailId = dto.workflowDetailId;
        tableNifiSettingPO.selectSql = config.processorConfig.sourceExecSqlQuery;
        tableNifiSettingPO.type = dto.type.getValue();
        tableNifiSettingPO.syncMode = config.targetDsConfig.syncMode;
        res.add(evaluateJson);
        res.add(logProcessor);
        res.add(delSqlRes);
        if (executeSQLRecord.getId() != null) {
            res.add(executeSQLRecord);
        }
        res.addAll(processorEntities);
        if (dto.groupStructureId == null) {
            tableNifiSettingPO.dispatchComponentId = dispatchProcessor.getId();
            tableNifiSettingPO.publishKafkaProcessorId = publishKafkaProcessor.getId();
            res.add(publishKafkaProcessor);
            res.add(consumeKafkaProcessor);
            if (!StringUtils.isEmpty(config.processorConfig.scheduleExpression)) {
                res.add(dispatchProcessor);
            }
        }
        tableNifiSettingPO.tableName = fullTableName;
        tableNifiSettingService.saveOrUpdate(tableNifiSettingPO);
        return res;
    }

    public List<ProcessorEntity> pipelineSupervision(String groupId, List<ProcessorEntity> processorEntities, String cfgDbPoolId, TableNifiSettingPO tableNifiSettingPO) {
        List<ProcessorEntity> processorEntityList = new ArrayList<>();
        //查询
        ProcessorEntity processorEntity = queryForPipelineSupervision(groupId, cfgDbPoolId);
        tableNifiSettingPO.queryForSupervisionProcessorId = processorEntity.getId();
        processorEntityList.add(processorEntity);
        //转string convertJsonForSupervision
        ProcessorEntity convertJsonForSupervision = specificSymbolProcessor(groupId, null);
        tableNifiSettingPO.convertJsonForSupervisionProcessorId = convertJsonForSupervision.getId();
        componentConnector(groupId, processorEntity.getId(), convertJsonForSupervision.getId(), AutoEndBranchTypeEnum.SUCCESS);
        processorEntityList.add(convertJsonForSupervision);
        //发消息
        ProcessorEntity publishKafkaForSupervisionProcessor = createPublishKafkaForSupervisionProcessor(groupId, 6);
        tableNifiSettingPO.publishKafkaForSupervisionProcessorId = publishKafkaForSupervisionProcessor.getId();
        componentConnector(groupId, convertJsonForSupervision.getId(), publishKafkaForSupervisionProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
        processorEntityList.add(publishKafkaForSupervisionProcessor);
        return processorEntityList;
    }

    public List<ProcessorEntity> createExcelProcessorEntity(String appGroupId, String groupId, DataAccessConfigDTO config, TableNifiSettingPO tableNifiSettingPO, String supervisionId, List<AutoEndBranchTypeEnum> autoEndBranchTypeEnums, BuildNifiFlowDTO dto) {
        config.targetDsConfig.tableFieldsList.stream().filter(Objects::nonNull)
                .forEach(e -> {
                    e.fieldName = e.fieldName.replaceAll(" ", "_");
                    e.sourceFieldName = e.fieldName;
                });
        List<ProcessorEntity> processorEntities = new ArrayList<>();
        ProcessorEntity getFTPProcessor = null;
        //getftp组件
        FtpConfig ftpConfig = config.ftpConfig;
        if (dto.sftpFlow) {
            if (ftpConfig != null && StringUtils.isNotEmpty(ftpConfig.fileBinary)) {
                // sftp上传二进制字符串rsa密钥文件
                SftpUtils.uploadRsaFile(ftpConfig.fileBinary, ftpConfig.linuxPath, ftpConfig.fileName, userName, password, rsaPath, host, port);
            }
            getFTPProcessor = createFetchSFTPProcessor(groupId, ftpConfig);
        } else {
            getFTPProcessor = createFetchFTPProcessor(groupId, ftpConfig);
        }

        //componentsConnector(groupId, getFTPProcessor.getId(), supervisionId, autoEndBranchTypeEnums);
        tableNifiSettingPO.getFtpProcessorId = getFTPProcessor.getId();
        //ToCSV
        ProcessorEntity convertExcelToCSVProcessor = createConvertExcelToCSVProcessor(groupId, ftpConfig);
        componentsConnector(groupId, convertExcelToCSVProcessor.getId(), supervisionId, autoEndBranchTypeEnums);
        tableNifiSettingPO.convertExcelToCsvProcessorId = convertExcelToCSVProcessor.getId();
        //连接器
        componentConnector(groupId, getFTPProcessor.getId(), convertExcelToCSVProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //创建csvreader
        ControllerServiceEntity csvReaderProcessor = createCSVReaderControllerService(appGroupId, config);
        tableNifiSettingPO.csvReaderId = csvReaderProcessor.getId();
        //创建AvroRecordSetWriter
        ControllerServiceEntity avroRecordSetWriterControllerService = createAvroRecordSetWriterControllerService(appGroupId, config);
        tableNifiSettingPO.avroRecordSetWriterId = avroRecordSetWriterControllerService.getId();
        //创建ConvertRecord
        ProcessorEntity convertRecordProcessor = createConvertRecordProcessor(groupId, csvReaderProcessor.getId(), avroRecordSetWriterControllerService.getId());
        componentsConnector(groupId, convertRecordProcessor.getId(), supervisionId, autoEndBranchTypeEnums);
        tableNifiSettingPO.convertRecordProcessorId = convertRecordProcessor.getId();
        //连接器
        componentConnector(groupId, convertExcelToCSVProcessor.getId(), convertRecordProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
        ProcessorEntity updateAttributeProcessor = createUpdateAttributeProcessor(groupId);
        tableNifiSettingPO.mergeContentProcessorId = updateAttributeProcessor.getId();
        //连接器
        componentConnector(groupId, convertRecordProcessor.getId(), updateAttributeProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
        processorEntities.add(getFTPProcessor);
        processorEntities.add(convertExcelToCSVProcessor);
        processorEntities.add(convertRecordProcessor);
        processorEntities.add(updateAttributeProcessor);
        return processorEntities;
    }

    public List<ProcessorEntity> createExcelProcessorEntity2(String appGroupId, String groupId, DataAccessConfigDTO config, TableNifiSettingPO tableNifiSettingPO, String supervisionId, List<AutoEndBranchTypeEnum> autoEndBranchTypeEnums, BuildNifiFlowDTO dto) {

        List<ProcessorEntity> processorEntities = new ArrayList<>();
        /**
         * 流程中的第二个replaceTextForFtpProcess组件
         */
        ProcessorEntity replaceTextForFtpProcess = replaceTextForFtpDataUploadProcess(config, groupId, dto);
        tableNifiSettingPO.getFtpProcessorId = replaceTextForFtpProcess.getId();
        //componentConnector(groupId, delSqlRes.getId(), replaceTextForFtpProcess.getId(), AutoEndBranchTypeEnum.SUCCESS);
        /**
         * 流程中的第二个invokeHTTPProcessor组件
         */
        ProcessorEntity invokeHTTPForFtpProcessor = invokeHTTPForFtpDataUploadProcessor(groupId);
        tableNifiSettingPO.convertExcelToCsvProcessorId = invokeHTTPForFtpProcessor.getId();
        componentConnector(groupId, replaceTextForFtpProcess.getId(), invokeHTTPForFtpProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
        processorEntities.add(replaceTextForFtpProcess);
        processorEntities.add(invokeHTTPForFtpProcessor);
        tableNifiSettingPO.csvReaderId = null;
        tableNifiSettingPO.avroRecordSetWriterId = null;
        tableNifiSettingPO.convertRecordProcessorId = null;
        tableNifiSettingPO.mergeContentProcessorId = null;

        return processorEntities;
    }

    public List<ProcessorEntity> createSapBwProcessorEntity(String appGroupId, String groupId, DataAccessConfigDTO config, TableNifiSettingPO tableNifiSettingPO, String supervisionId, List<AutoEndBranchTypeEnum> autoEndBranchTypeEnums, BuildNifiFlowDTO dto) {

        List<ProcessorEntity> processorEntities = new ArrayList<>();
        /**
         * 流程中的第1个replaceTextForSapBwProcess组件
         */
        ProcessorEntity replaceTextForSapBwProcess = replaceTextForSapBwProcess(config, groupId, dto);
        tableNifiSettingPO.getFtpProcessorId = replaceTextForSapBwProcess.getId();
        //componentConnector(groupId, delSqlRes.getId(), replaceTextForFtpProcess.getId(), AutoEndBranchTypeEnum.SUCCESS);
        /**
         * 流程中的第1个invokeHTTPProcessorForSapBw组件
         */
        ProcessorEntity invokeHTTPForSapBwProcessor = invokeHTTPForSapBwProcessor(groupId);
        tableNifiSettingPO.convertExcelToCsvProcessorId = invokeHTTPForSapBwProcessor.getId();
        componentConnector(groupId, replaceTextForSapBwProcess.getId(), invokeHTTPForSapBwProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
        processorEntities.add(replaceTextForSapBwProcess);
        processorEntities.add(invokeHTTPForSapBwProcessor);
        tableNifiSettingPO.csvReaderId = null;
        tableNifiSettingPO.avroRecordSetWriterId = null;
        tableNifiSettingPO.convertRecordProcessorId = null;
        tableNifiSettingPO.mergeContentProcessorId = null;

        return processorEntities;
    }

    public List<ProcessorEntity> createSapBwProcessorEntity3(String appGroupId, String groupId, DataAccessConfigDTO config, TableNifiSettingPO tableNifiSettingPO, String supervisionId, List<AutoEndBranchTypeEnum> autoEndBranchTypeEnums, BuildNifiFlowDTO dto) {

        List<ProcessorEntity> processorEntities = new ArrayList<>();
        /**
         * 流程中的第1个replaceTextForPowerBIProcess组件
         */
        ProcessorEntity replaceTextForPowerBIProcess = replaceTextForPowerBIProcess(config, groupId, dto);
        tableNifiSettingPO.getFtpProcessorId = replaceTextForPowerBIProcess.getId();
        //componentConnector(groupId, delSqlRes.getId(), replaceTextForFtpProcess.getId(), AutoEndBranchTypeEnum.SUCCESS);
        /**
         * 流程中的第1个invokeHTTPForPowerBIProcessor组件
         */
        ProcessorEntity invokeHTTPForPowerBIProcessor = invokeHTTPForPowerBIProcessor(groupId);
        tableNifiSettingPO.convertExcelToCsvProcessorId = invokeHTTPForPowerBIProcessor.getId();
        componentConnector(groupId, replaceTextForPowerBIProcess.getId(), invokeHTTPForPowerBIProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
        processorEntities.add(replaceTextForPowerBIProcess);
        processorEntities.add(invokeHTTPForPowerBIProcessor);
        tableNifiSettingPO.csvReaderId = null;
        tableNifiSettingPO.avroRecordSetWriterId = null;
        tableNifiSettingPO.convertRecordProcessorId = null;
        tableNifiSettingPO.mergeContentProcessorId = null;

        return processorEntities;
    }

    public List<ProcessorEntity> createApiProcessorEntity(String appGroupId, String groupId, DataAccessConfigDTO config, TableNifiSettingPO tableNifiSettingPO, String supervisionId, List<AutoEndBranchTypeEnum> autoEndBranchTypeEnums, BuildNifiFlowDTO dto) {

        List<ProcessorEntity> processorEntities = new ArrayList<>();
        /**
         * 流程中的第1个replaceTextForSapBwProcess组件
         */
        ProcessorEntity replaceTextForSapBwProcess = replaceTextForApiProcess(config, groupId, dto);
        tableNifiSettingPO.getFtpProcessorId = replaceTextForSapBwProcess.getId();
        //componentConnector(groupId, delSqlRes.getId(), replaceTextForFtpProcess.getId(), AutoEndBranchTypeEnum.SUCCESS);
        /**
         * 流程中的第1个invokeHTTPProcessorForSapBw组件
         */
        ProcessorEntity invokeHTTPForSapBwProcessor = invokeHTTPForApiProcessor(groupId);
        tableNifiSettingPO.convertExcelToCsvProcessorId = invokeHTTPForSapBwProcessor.getId();
        componentConnector(groupId, replaceTextForSapBwProcess.getId(), invokeHTTPForSapBwProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
        processorEntities.add(replaceTextForSapBwProcess);
        processorEntities.add(invokeHTTPForSapBwProcessor);
        tableNifiSettingPO.csvReaderId = null;
        tableNifiSettingPO.avroRecordSetWriterId = null;
        tableNifiSettingPO.convertRecordProcessorId = null;
        tableNifiSettingPO.mergeContentProcessorId = null;

        return processorEntities;
    }

    public ProcessorEntity createGetFTPProcessor(String groupId, FtpConfig ftpConfig) {
        BuildGetFTPProcessorDTO buildGetFTPProcessorDTO = new BuildGetFTPProcessorDTO();
        buildGetFTPProcessorDTO.groupId = groupId;
        buildGetFTPProcessorDTO.name = "GETFTP";
        buildGetFTPProcessorDTO.details = "query_phase";
        buildGetFTPProcessorDTO.fileFilterRegex = ftpConfig.fileFilterRegex;
        buildGetFTPProcessorDTO.ftpUseUtf8 = ftpConfig.ftpUseUtf8;
        buildGetFTPProcessorDTO.hostname = ftpConfig.hostname;
        buildGetFTPProcessorDTO.password = ftpConfig.password;
        buildGetFTPProcessorDTO.port = ftpConfig.port;
        buildGetFTPProcessorDTO.remotePath = ftpConfig.remotePath;
        buildGetFTPProcessorDTO.username = ftpConfig.username;
        buildGetFTPProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(-1, 9);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildGetFTPProcess(buildGetFTPProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    public ProcessorEntity createFetchFTPProcessor(String groupId, FtpConfig ftpConfig) {
        BuildFetchFTPProcessorDTO buildFetchFTPProcessorDTO = new BuildFetchFTPProcessorDTO();
        buildFetchFTPProcessorDTO.groupId = groupId;
        buildFetchFTPProcessorDTO.name = "FetchFTP";
        buildFetchFTPProcessorDTO.details = "query_phase";
        buildFetchFTPProcessorDTO.ftpUseUtf8 = ftpConfig.ftpUseUtf8;
        buildFetchFTPProcessorDTO.hostname = ftpConfig.hostname;
        buildFetchFTPProcessorDTO.password = ftpConfig.password;
        buildFetchFTPProcessorDTO.port = ftpConfig.port;
        buildFetchFTPProcessorDTO.remoteFile = ftpConfig.remotePath + "/" + ftpConfig.fileFilterRegex;
        buildFetchFTPProcessorDTO.username = ftpConfig.username;
        buildFetchFTPProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(-1, 9);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildFetchFTPProcess(buildFetchFTPProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    public ProcessorEntity createFetchSFTPProcessor(String groupId, FtpConfig ftpConfig) {
        BuildFetchSFTPProcessorDTO buildFetchSFTPProcessor = new BuildFetchSFTPProcessorDTO();
        buildFetchSFTPProcessor.groupId = groupId;
        buildFetchSFTPProcessor.name = "FetchSFTP";
        buildFetchSFTPProcessor.details = "query_phase";
        buildFetchSFTPProcessor.hostname = ftpConfig.hostname;
        buildFetchSFTPProcessor.password = ftpConfig.password;
        buildFetchSFTPProcessor.privateKeyPath = StringUtils.isEmpty(ftpConfig.fileName) ? null : (ftpConfig.linuxPath + ftpConfig.fileName);
        buildFetchSFTPProcessor.port = ftpConfig.port;
        buildFetchSFTPProcessor.remoteFile = ftpConfig.remotePath + "/" + ftpConfig.fileFilterRegex;
        buildFetchSFTPProcessor.username = ftpConfig.username;
        buildFetchSFTPProcessor.sendKeepAliveOnTimeout = "false";
        buildFetchSFTPProcessor.connectionTimeout = "100 sec";
        buildFetchSFTPProcessor.dataTimeout = "100 sec";
        buildFetchSFTPProcessor.positionDTO = NifiPositionHelper.buildXYPositionDTO(-1, 9);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildFetchSFTPProcess(buildFetchSFTPProcessor);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    public ProcessorEntity createConvertExcelToCSVProcessor(String groupId, FtpConfig ftpConfig) {
        log.info("Ftp连接信息配置，{}", JSON.toJSONString(ftpConfig));
        BuildConvertExcelToCSVProcessorDTO buildConvertExcelToCSVrocessorDTO = new BuildConvertExcelToCSVProcessorDTO();
        buildConvertExcelToCSVrocessorDTO.csvFormat = "excel";
        buildConvertExcelToCSVrocessorDTO.formatCellValues = true;
        buildConvertExcelToCSVrocessorDTO.includeHeaderLine = true;
//        buildConvertExcelToCSVrocessorDTO.numberOfRowsToSkip = 1;
        buildConvertExcelToCSVrocessorDTO.details = "transition_phase";
        buildConvertExcelToCSVrocessorDTO.groupId = groupId;
        buildConvertExcelToCSVrocessorDTO.name = "ConvertExcelToCSV";
        buildConvertExcelToCSVrocessorDTO.sheetName = ftpConfig.sheetName;
        buildConvertExcelToCSVrocessorDTO.startLine = String.valueOf(ftpConfig.startLine);
        buildConvertExcelToCSVrocessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(-2, 9);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildConvertExcelToCSVProcess(buildConvertExcelToCSVrocessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    public ControllerServiceEntity createCSVReaderControllerService(String groupId, DataAccessConfigDTO config) {
        BuildCSVReaderProcessorDTO buildCSVReaderProcessorDTO = new BuildCSVReaderProcessorDTO();
        buildCSVReaderProcessorDTO.schemaAccessStrategy = "schema-text-property";
        buildCSVReaderProcessorDTO.groupId = groupId;
        buildCSVReaderProcessorDTO.name = "CSVReader";
        buildCSVReaderProcessorDTO.details = "CSVReader";
        buildCSVReaderProcessorDTO.csvFormat = "excel";
        buildCSVReaderProcessorDTO.skipHeaderLine = "false";
        List<String> sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
        String schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);
        buildCSVReaderProcessorDTO.schemaText = schemaArchitecture;
        BusinessResult<ControllerServiceEntity> controllerServiceEntityBusinessResult = componentsBuild.buildCSVReaderService(buildCSVReaderProcessorDTO);
        if (controllerServiceEntityBusinessResult.success) {
            ControllerServiceEntity data = controllerServiceEntityBusinessResult.data;
            return data;
        } else {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, controllerServiceEntityBusinessResult.msg);
        }
    }

    public ControllerServiceEntity createAvroRecordSetWriterControllerService(String groupId, DataAccessConfigDTO config) {
        BuildAvroRecordSetWriterServiceDTO buildCSVReaderProcessorDTO = new BuildAvroRecordSetWriterServiceDTO();
        buildCSVReaderProcessorDTO.groupId = groupId;
        buildCSVReaderProcessorDTO.name = "AvroRecordSetWriter";
        buildCSVReaderProcessorDTO.details = "AvroRecordSetWriter";
        List<String> sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
        String schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);
        buildCSVReaderProcessorDTO.schemaArchitecture = schemaArchitecture;
        buildCSVReaderProcessorDTO.schemaAccessStrategy = "schema-text-property";
        buildCSVReaderProcessorDTO.schemaWriteStrategy = "avro-embedded";
        BusinessResult<ControllerServiceEntity> controllerServiceEntityBusinessResult = componentsBuild.buildAvroRecordSetWriterService(buildCSVReaderProcessorDTO);
        if (controllerServiceEntityBusinessResult.success) {
            ControllerServiceEntity data = controllerServiceEntityBusinessResult.data;
            return data;
        } else {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, controllerServiceEntityBusinessResult.msg);
        }
    }

    public ProcessorEntity createConvertRecordProcessor(String groupId, String csvId, String avroId) {
        BuildConvertRecordProcessorDTO buildConvertRecordProcessorDTO = new BuildConvertRecordProcessorDTO();
        buildConvertRecordProcessorDTO.recordReader = csvId;
        buildConvertRecordProcessorDTO.recordWriter = avroId;
        buildConvertRecordProcessorDTO.name = "ConvertRecord";
        buildConvertRecordProcessorDTO.details = "transition_phase";
        buildConvertRecordProcessorDTO.groupId = groupId;
        buildConvertRecordProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(-3, 9);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildConvertRecordProcess(buildConvertRecordProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    public ProcessorEntity createUpdateAttributeProcessor(String groupId) {
        BuildUpdateAttributeDTO buildUpdateAttribute = new BuildUpdateAttributeDTO();
        buildUpdateAttribute.details = "buildUpdateAttribute";
        buildUpdateAttribute.name = "buildUpdateAttribute";
        buildUpdateAttribute.groupId = groupId;
        buildUpdateAttribute.positionDTO = NifiPositionHelper.buildXYPositionDTO(-4, 9);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildUpdateAttribute(buildUpdateAttribute);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    private String buildSchemaArchitecture(List<String> tableFieldsList, String schemaName) {
        String architecture = "{\"namespace\": \"nifi\",\"name\": \"" + schemaName + "\",\"type\": \"record\",\"fields\": [";
        for (String tableFields : tableFieldsList) {
            if (StringUtils.isNotEmpty(tableFields)) {
                tableFields = tableFields.replace(")", "_");
                tableFields = tableFields.replace("(", "_");
                tableFields = tableFields.replace("）", "_");
                tableFields = tableFields.replace("（", "_");
                architecture += "{ \"name\": \"" + tableFields + "\",\"type\": [\"null\",\"string\"] },";
            }
        }
        architecture = architecture.substring(0, architecture.length() - 1) + "]}";
        return architecture;
    }

    private String buildSchemaArchitectureForMongo(String mongoDocTypeMap, String schemaName) throws IOException {
        return AvroSchemaGeneratorUtils.getAvroSchema(mongoDocTypeMap, schemaName);
    }


    private ProcessorEntity createExecuteSQLRecordForMongoDB(String appGroupId, DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto, String sourceDbPoolId, TableNifiSettingPO tableNifiSettingPO) {
        //-------------------------------------------------CREATE MongoDBJsonReader JsonTreeReader  START
        BuildAvroRecordSetWriterServiceDTO data = new BuildAvroRecordSetWriterServiceDTO();
        String groupStructureId = dto.groupStructureId;
        data.details = "MongoDBJsonReader";
        data.name = "MongoDBJsonReader";
        if (groupStructureId != null) {
            data.groupId = groupStructureId;
        } else {
            data.groupId = appGroupId;
        }
        String id = "";
        //Schema Text 结构 修改成mongodb JsonTreeReader所需的结构
        try {
            data.schemaArchitecture = buildSchemaArchitectureForMongo(dto.mongoDocTypeMap, config.processorConfig.targetTableName);
        } catch (IOException e) {
            log.error("mongodb trans json schema error", e);
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, "mongodb trans json schema error");
        }
        data.schemaAccessStrategy = "schema-text-property";
        //创建JsonTreeReader
        BusinessResult<ControllerServiceEntity> controllerServiceEntityBusinessResult = componentsBuild.buildJsonTreeReaderService(data);
        if (controllerServiceEntityBusinessResult.success) {
            id = controllerServiceEntityBusinessResult.data.getId();
        } else {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, controllerServiceEntityBusinessResult.msg);
        }
        tableNifiSettingPO.convertPutDatabaseRecordId = id;
        //-------------------------------------------------CREATE MongoDBJsonReader JsonTreeReader  end

        GetMongoDTO mongoDTO = new GetMongoDTO();
        // 组件并发数量
        mongoDTO.concurrencyNums = dto.concurrencyNums;
        mongoDTO.JSONType = "Standard";
        mongoDTO.name = "GetMongo";
        mongoDTO.details = "query_phase";
        mongoDTO.groupId = groupId;
        //拿接入配置,如果没有拿默认配置
        if (dto.maxRowsPerFlowFile != 0) {
            mongoDTO.resultsPerFlowFile = String.valueOf(dto.maxRowsPerFlowFile);
        } else {
            mongoDTO.resultsPerFlowFile = MaxRowsPerFlowFile;
        }

        //executeSQLRecordDTO.databaseConnectionPoolingService=config.sourceDsConfig.componentId;
        mongoDTO.clientService = sourceDbPoolId;
        mongoDTO.query = dto.mongoQueryCondition;
        mongoDTO.projection = dto.mongoNeededFileds;
        mongoDTO.mongoDatabaseName = dto.mongoDatabaseName;
        mongoDTO.mongoCollectionName = dto.mongoCollectionName;

        mongoDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(9);
        BusinessResult<ProcessorEntity> res = componentsBuild.buildGetMongoProcess(mongoDTO);
        verifyProcessorResult(res);
        return res.data;
    }

    private ProcessorEntity createConvertAvroToJSON(String groupId, BuildNifiFlowDTO dto) {
        GetMongoDTO mongoDTO = new GetMongoDTO();
        // 组件并发数量
        mongoDTO.concurrencyNums = dto.concurrencyNums;
        mongoDTO.name = "ConvertAvroToJSON";
        mongoDTO.groupId = groupId;
        //拿接入配置,如果没有拿默认配置
        if (dto.maxRowsPerFlowFile != 0) {
            mongoDTO.resultsPerFlowFile = String.valueOf(dto.maxRowsPerFlowFile);
        } else {
            mongoDTO.resultsPerFlowFile = MaxRowsPerFlowFile;
        }

        mongoDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(-2, 9);
        BusinessResult<ProcessorEntity> res = componentsBuild.buildConvertAvroToJSON(mongoDTO);
        verifyProcessorResult(res);
        return res.data;
    }

    private ProcessorEntity createExecuteSQLRecord(String appGroupId, DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto, String sourceDbPoolId, TableNifiSettingPO tableNifiSettingPO) {
        BuildAvroRecordSetWriterServiceDTO data = new BuildAvroRecordSetWriterServiceDTO();
        String groupStructureId = dto.groupStructureId;
        data.details = "AvroRecordSetWriter";
        data.name = "AvroRecordSetWriter";
        if (groupStructureId != null) {
            data.groupId = groupStructureId;
        } else {
            data.groupId = appGroupId;
        }
        String id = "";
        List<String> sourceFieldName = new ArrayList<>();
        //--------------------------------------------
        if (Objects.equals(dto.type, OlapTableEnum.PHYSICS) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKPHYSICS) || Objects.equals(dto.type, OlapTableEnum.DATASERVICES)) {
            sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
        } else if (Objects.equals(dto.type, OlapTableEnum.FACT) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT)
                || Objects.equals(dto.type, OlapTableEnum.DIMENSION) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
            sourceFieldName = config.modelPublishFieldDTOList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
        }
        String schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);
        data.schemaArchitecture = schemaArchitecture;
        data.schemaWriteStrategy = "avro-embedded";
        data.schemaAccessStrategy = "schema-text-property";
        //--------------------------------------------
        //创建buildAvroRecordSetWriterService
        BusinessResult<ControllerServiceEntity> controllerServiceEntityBusinessResult = componentsBuild.buildAvroRecordSetWriterService(data);
        if (controllerServiceEntityBusinessResult.success) {
            id = controllerServiceEntityBusinessResult.data.getId();
        } else {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, controllerServiceEntityBusinessResult.msg);
        }
        tableNifiSettingPO.avroRecordSetWriterId = id;
        ExecuteSQLRecordDTO executeSQLRecordDTO = new ExecuteSQLRecordDTO();
        // 组件并发数量
        executeSQLRecordDTO.concurrencyNums = dto.concurrencyNums;
        executeSQLRecordDTO.name = "executeSQLRecord";
        executeSQLRecordDTO.details = "query_phase";
        executeSQLRecordDTO.groupId = groupId;
        //拿接入配置,如果没有拿默认配置
        if (dto.maxRowsPerFlowFile != 0) {
            executeSQLRecordDTO.maxRowsPerFlowFile = String.valueOf(dto.maxRowsPerFlowFile);
        } else {
            executeSQLRecordDTO.maxRowsPerFlowFile = MaxRowsPerFlowFile;
        }
        if (dto.fetchSize != 0) {
            executeSQLRecordDTO.FetchSize = String.valueOf(dto.fetchSize);
        } else {
            executeSQLRecordDTO.FetchSize = FetchSize;
        }
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceOdsId));
        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            DataSourceDTO dataSource = fiDataDataSource.data;
            IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(dataSource.conType);
            executeSQLRecordDTO.esqlAutoCommit = dbCommand.getEsqlAutoCommit();
        } else {
            log.error("userclient无法查询到ods库的连接信息");
        }

        executeSQLRecordDTO.outputBatchSize = OutputBatchSize;
        //executeSQLRecordDTO.databaseConnectionPoolingService=config.sourceDsConfig.componentId;
        executeSQLRecordDTO.databaseConnectionPoolingService = sourceDbPoolId;
        log.info("原始接入查询语句:{}", config.processorConfig.sourceExecSqlQuery);
        String sql = config.processorConfig.sourceExecSqlQuery.replaceAll(SystemVariableTypeEnum.START_TIME.getValue(), "'\\${" + SystemVariableTypeEnum.START_TIME.getName() + "}'");
        sql = sql.replaceAll(SystemVariableTypeEnum.END_TIME.getValue(), "'\\${" + SystemVariableTypeEnum.END_TIME.getName() + "}'");
        executeSQLRecordDTO.sqlSelectQuery = sql;
        executeSQLRecordDTO.recordwriter = id;
        executeSQLRecordDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(9);
        BusinessResult<ProcessorEntity> res = componentsBuild.buildExecuteSQLRecordProcess(executeSQLRecordDTO);
        verifyProcessorResult(res);
        return res.data;
    }

    private ProcessorEntity createExecuteSQLRecordForDoris(String appGroupId, DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto, String sourceDbPoolId, TableNifiSettingPO tableNifiSettingPO) {
        BuildAvroRecordSetWriterServiceDTO data = new BuildAvroRecordSetWriterServiceDTO();
        String groupStructureId = dto.groupStructureId;
        data.details = "AvroRecordSetWriter";
        data.name = "AvroRecordSetWriter";
        if (groupStructureId != null) {
            data.groupId = groupStructureId;
        } else {
            data.groupId = appGroupId;
        }
        String id = "";
        List<String> sourceFieldName = new ArrayList<>();
        //--------------------------------------------
        if (Objects.equals(dto.type, OlapTableEnum.PHYSICS) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKPHYSICS) || Objects.equals(dto.type, OlapTableEnum.DATASERVICES)) {
            sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
        } else if (Objects.equals(dto.type, OlapTableEnum.FACT) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT)
                || Objects.equals(dto.type, OlapTableEnum.DIMENSION) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
            sourceFieldName = config.modelPublishFieldDTOList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
        }
        String schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);
        data.schemaArchitecture = schemaArchitecture;
        data.schemaWriteStrategy = "avro-embedded";
        data.schemaAccessStrategy = "schema-text-property";
        //--------------------------------------------
        //创建buildAvroRecordSetWriterService
        BusinessResult<ControllerServiceEntity> controllerServiceEntityBusinessResult = componentsBuild.buildAvroRecordSetWriterService(data);
        if (controllerServiceEntityBusinessResult.success) {
            id = controllerServiceEntityBusinessResult.data.getId();
        } else {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, controllerServiceEntityBusinessResult.msg);
        }
        tableNifiSettingPO.avroRecordSetWriterId = id;
        ExecuteSQLRecordDTO executeSQLRecordDTO = new ExecuteSQLRecordDTO();
        // 组件并发数量
        executeSQLRecordDTO.concurrencyNums = dto.concurrencyNums;
        executeSQLRecordDTO.name = "executeSQLRecordForDoris";
        executeSQLRecordDTO.details = "query_phase";
        executeSQLRecordDTO.groupId = groupId;
        //拿接入配置,如果没有拿默认配置
        if (dto.maxRowsPerFlowFile != 0) {
            executeSQLRecordDTO.maxRowsPerFlowFile = String.valueOf(dto.maxRowsPerFlowFile);
        } else {
            executeSQLRecordDTO.maxRowsPerFlowFile = MaxRowsPerFlowFile;
        }
        if (dto.fetchSize != 0) {
            executeSQLRecordDTO.FetchSize = String.valueOf(dto.fetchSize);
        } else {
            executeSQLRecordDTO.FetchSize = FetchSize;
        }
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceOdsId));
        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            DataSourceDTO dataSource = fiDataDataSource.data;
            IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(dataSource.conType);
            executeSQLRecordDTO.esqlAutoCommit = dbCommand.getEsqlAutoCommit();
        } else {
            log.error("userclient无法查询到ods库的连接信息");
        }

        executeSQLRecordDTO.outputBatchSize = OutputBatchSize;
        //executeSQLRecordDTO.databaseConnectionPoolingService=config.sourceDsConfig.componentId;
        executeSQLRecordDTO.databaseConnectionPoolingService = sourceDbPoolId;
        log.info("原始接入查询语句:{}", config.processorConfig.sourceExecSqlQuery);
        String sql = config.processorConfig.sourceExecSqlQuery.replaceAll(SystemVariableTypeEnum.START_TIME.getValue(), "'\\${" + SystemVariableTypeEnum.START_TIME.getName() + "}'");
        sql = sql.replaceAll(SystemVariableTypeEnum.END_TIME.getValue(), "'\\${" + SystemVariableTypeEnum.END_TIME.getName() + "}'");
        executeSQLRecordDTO.sqlSelectQuery = sql;
        executeSQLRecordDTO.recordwriter = id;
        executeSQLRecordDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(9);
        BusinessResult<ProcessorEntity> res = componentsBuild.buildExecuteSQLRecordProcessForDoris(executeSQLRecordDTO);
        verifyProcessorResult(res);
        return res.data;
    }

    private ProcessorEntity createExecuteSQLRecordDoris(DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto, String targetDbPoolId) {
        BuildCallDbProcedureProcessorDTO buildCallDbProcedureProcessorDTO = new BuildCallDbProcedureProcessorDTO();
        buildCallDbProcedureProcessorDTO.haveNextOne = true;
        buildCallDbProcedureProcessorDTO.dbConnectionId = targetDbPoolId;
        buildCallDbProcedureProcessorDTO.executsql = config.processorConfig.sourceExecSqlQuery;
        buildCallDbProcedureProcessorDTO.details = "query_phase";
        buildCallDbProcedureProcessorDTO.name = "executeSql";
        buildCallDbProcedureProcessorDTO.groupId = groupId;
        buildCallDbProcedureProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(9);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildCallDbProcedureProcess(buildCallDbProcedureProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    private ProcessorEntity createConvertRecord(String appGroupId, DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto, TableNifiSettingPO tableNifiSettingPO) {
        //两个控制器服务,和一个组件,先把配置搞出来
        BuildConvertRecordDTO buildConvertRecordDTO = new BuildConvertRecordDTO();
        List<TableFieldsDTO> tableFieldsList = new ArrayList<>();
        Map<String, String> buildParameter = new HashMap<>();
        String groupStructureId = dto.groupStructureId;
        BuildAvroRecordSetWriterServiceDTO buildAvroRecordSetWriterServiceDTO = new BuildAvroRecordSetWriterServiceDTO();
        buildAvroRecordSetWriterServiceDTO.details = "transition_phase";
        buildAvroRecordSetWriterServiceDTO.name = "MongoAvroRecordSetWriter";
        if (groupStructureId != null) {
            buildAvroRecordSetWriterServiceDTO.groupId = groupStructureId;
        } else {
            buildAvroRecordSetWriterServiceDTO.groupId = appGroupId;
        }
        List<String> sourceFieldName = new ArrayList<>();
        if (Objects.equals(dto.type, OlapTableEnum.PHYSICS) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKPHYSICS) || Objects.equals(dto.type, OlapTableEnum.DATASERVICES)) {
            sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.fieldName).collect(Collectors.toList());
        } else if (Objects.equals(dto.type, OlapTableEnum.FACT) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT)
                || Objects.equals(dto.type, OlapTableEnum.DIMENSION) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
            sourceFieldName = config.modelPublishFieldDTOList.stream().map(e -> e.fieldEnName).collect(Collectors.toList());
        }
        String schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);
        buildAvroRecordSetWriterServiceDTO.schemaArchitecture = schemaArchitecture;
        buildAvroRecordSetWriterServiceDTO.schemaWriteStrategy = "avro-embedded";
        buildAvroRecordSetWriterServiceDTO.schemaAccessStrategy = "schema-text-property";
        BusinessResult<ControllerServiceEntity> avroRecordSetWriterService = componentsBuild.buildAvroRecordSetWriterService(buildAvroRecordSetWriterServiceDTO);

        /**
         * MongoAvroRecordSetWriter
         */
        tableNifiSettingPO.convertAvroRecordSetWriterId = avroRecordSetWriterService.data.getId();
        //--------------------------------------

        if (Objects.equals(dto.type, OlapTableEnum.PHYSICS) ||
                Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKPHYSICS) ||
                Objects.equals(dto.type, OlapTableEnum.DATASERVICES)) {
            sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
            tableFieldsList = config.targetDsConfig.tableFieldsList;
            for (TableFieldsDTO tableFieldsDTO : tableFieldsList) {
                if (!Objects.equals(tableFieldsDTO.fieldName, tableFieldsDTO.sourceFieldName) && StringUtils.isNotEmpty(tableFieldsDTO.sourceFieldName)) {
                    buildParameter.put("/" + tableFieldsDTO.fieldName, "/" + tableFieldsDTO.sourceFieldName);
                }
            }
            buildParameter.put("/" + tableFieldsList.get(0).fieldName, "/" + tableFieldsList.get(0).sourceFieldName);
        } else if (Objects.equals(dto.type, OlapTableEnum.FACT) ||
                Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT) ||
                Objects.equals(dto.type, OlapTableEnum.DIMENSION) ||
                Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
            List<ModelPublishFieldDTO> modelPublishFieldDTOS = config.modelPublishFieldDTOList;
            for (ModelPublishFieldDTO modelPublishFieldDTO : modelPublishFieldDTOS) {
                if (!Objects.equals(modelPublishFieldDTO.fieldEnName, modelPublishFieldDTO.sourceFieldName) && StringUtils.isNotEmpty(modelPublishFieldDTO.sourceFieldName)) {
                    buildParameter.put("/" + modelPublishFieldDTO.fieldEnName, "/" + modelPublishFieldDTO.sourceFieldName);
                }
            }
            buildParameter.put("/" + modelPublishFieldDTOS.get(0).fieldEnName, "/" + modelPublishFieldDTOS.get(0).sourceFieldName);
            sourceFieldName = config.modelPublishFieldDTOList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
        }
        schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);

        //--------------------------------------
        buildConvertRecordDTO.groupId = groupId;
        buildConvertRecordDTO.details = "transition_phase";
        buildConvertRecordDTO.name = "ConvertMongoJsonToAvro";


        buildConvertRecordDTO.recordReader = tableNifiSettingPO.convertPutDatabaseRecordId;
        buildConvertRecordDTO.recordWriter = avroRecordSetWriterService.data.getId();
        buildConvertRecordDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(10);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildConvertMongoJsonToAvro(buildConvertRecordDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    private ProcessorEntity createUpdateRecordForMongo(String appGroupId, DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto, TableNifiSettingPO tableNifiSettingPO) {
        //两个控制器服务,和一个组件,先把配置搞出来
        BuildUpdateRecordDTO buildUpdateRecordDTO = new BuildUpdateRecordDTO();
        List<TableFieldsDTO> tableFieldsList = new ArrayList<>();
        Map<String, String> buildParameter = new HashMap<>();
        String groupStructureId = dto.groupStructureId;
        BuildAvroRecordSetWriterServiceDTO buildAvroRecordSetWriterServiceDTO = new BuildAvroRecordSetWriterServiceDTO();
        buildAvroRecordSetWriterServiceDTO.details = "transition_phase";
        buildAvroRecordSetWriterServiceDTO.name = "AvroRecordSetWriterServiceTransition";
        if (groupStructureId != null) {
            buildAvroRecordSetWriterServiceDTO.groupId = groupStructureId;
        } else {
            buildAvroRecordSetWriterServiceDTO.groupId = appGroupId;
        }
        List<String> sourceFieldName = new ArrayList<>();
        if (Objects.equals(dto.type, OlapTableEnum.PHYSICS) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKPHYSICS) || Objects.equals(dto.type, OlapTableEnum.DATASERVICES)) {
            sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.fieldName).collect(Collectors.toList());
        } else if (Objects.equals(dto.type, OlapTableEnum.FACT) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT)
                || Objects.equals(dto.type, OlapTableEnum.DIMENSION) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
            sourceFieldName = config.modelPublishFieldDTOList.stream().map(e -> e.fieldEnName).collect(Collectors.toList());
        }
        if (StringUtils.isNotEmpty(dto.generateVersionSql)) {
            sourceFieldName.add(NifiConstants.AttrConstants.FI_VERSION);
        }
        sourceFieldName.add("fidata_batch_code");
        sourceFieldName.add("fidata_flow_batch_code");

        String schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);
        buildAvroRecordSetWriterServiceDTO.schemaArchitecture = schemaArchitecture;
        buildAvroRecordSetWriterServiceDTO.schemaWriteStrategy = "avro-embedded";
        buildAvroRecordSetWriterServiceDTO.schemaAccessStrategy = "schema-text-property";
        BusinessResult<ControllerServiceEntity> avroRecordSetWriterService = componentsBuild.buildAvroRecordSetWriterService(buildAvroRecordSetWriterServiceDTO);
        tableNifiSettingPO.convertAvroRecordSetWriterForCodeId = avroRecordSetWriterService.data.getId();
        //--------------------------------------
        BuildAvroReaderServiceDTO buildAvroReaderServiceDTO = new BuildAvroReaderServiceDTO();
        buildAvroReaderServiceDTO.details = "transition_phase";
        buildAvroReaderServiceDTO.name = "PutDatabaseRecordTransition";
        if (groupStructureId != null) {
            buildAvroReaderServiceDTO.groupId = groupStructureId;
        } else {
            buildAvroReaderServiceDTO.groupId = appGroupId;
        }
/*        if (Objects.equals(dto.type, OlapTableEnum.PHYSICS) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKPHYSICS)) {
            sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
        } else if (Objects.equals(dto.type, OlapTableEnum.FACT) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT)
                || Objects.equals(dto.type, OlapTableEnum.DIMENSION) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
            sourceFieldName = config.modelPublishFieldDTOList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
        }*/
        if (StringUtils.isNotEmpty(dto.generateVersionSql)) {
            schemaArchitecture = buildSchemaArchitecture(sourceFieldName.subList(0, sourceFieldName.size() - 3), config.processorConfig.targetTableName);
        } else {
            schemaArchitecture = buildSchemaArchitecture(sourceFieldName.subList(0, sourceFieldName.size() - 2), config.processorConfig.targetTableName);
        }

        buildAvroReaderServiceDTO.schemaText = schemaArchitecture;
        BusinessResult<ControllerServiceEntity> avroReaderService = componentsBuild.buildAvroReaderService(buildAvroReaderServiceDTO);
        tableNifiSettingPO.convertPutDatabaseRecordForCodeId = avroReaderService.data.getId();
        //--------------------------------------
        buildUpdateRecordDTO.groupId = groupId;
        buildUpdateRecordDTO.details = "transition_phase";
        buildUpdateRecordDTO.name = "UpdateRecordForMongo";

        //至少有一个属性
        //nifi的三元运算,如果pipelTraceId是空的,取pipelTaskTraceId当作fidata_batch_code的值
//        buildParameter.put("/fidata_batch_code", "${pipelTraceId:isEmpty():ifElse(${pipelTaskTraceId},${pipelTraceId})}");

        //2023-05-25 李世纪修改  大批次号直接拿${fidata_batch_code}，存入临时表
        buildParameter.put("/fidata_batch_code", "${fidata_batch_code}");
        buildParameter.put("/fidata_flow_batch_code", "${fragment.index}");
        if (StringUtils.isNotEmpty(dto.generateVersionSql)) {
            buildParameter.put("/fi_version", "${fi_version}");
        }
        buildUpdateRecordDTO.filedMap = buildParameter;

        buildUpdateRecordDTO.recordReader = avroReaderService.data.getId();
        buildUpdateRecordDTO.recordWriter = avroRecordSetWriterService.data.getId();
        buildUpdateRecordDTO.replacementValueStrategy = "literal-value";
        buildUpdateRecordDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(11);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildUpdateRecord(buildUpdateRecordDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    private ProcessorEntity createUpdateField(String appGroupId, DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto, TableNifiSettingPO tableNifiSettingPO) {
        //两个控制器服务,和一个组件,先把配置搞出来
        BuildUpdateRecordDTO buildUpdateRecordDTO = new BuildUpdateRecordDTO();
        List<TableFieldsDTO> tableFieldsList = new ArrayList<>();
        Map<String, String> buildParameter = new HashMap<>();
        String groupStructureId = dto.groupStructureId;
        BuildAvroRecordSetWriterServiceDTO buildAvroRecordSetWriterServiceDTO = new BuildAvroRecordSetWriterServiceDTO();
        buildAvroRecordSetWriterServiceDTO.details = "transition_phase";
        buildAvroRecordSetWriterServiceDTO.name = "AvroRecordSetWriterServiceTransition";
        if (groupStructureId != null) {
            buildAvroRecordSetWriterServiceDTO.groupId = groupStructureId;
        } else {
            buildAvroRecordSetWriterServiceDTO.groupId = appGroupId;
        }
        List<String> sourceFieldName = new ArrayList<>();
        if (Objects.equals(dto.type, OlapTableEnum.PHYSICS) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKPHYSICS) || Objects.equals(dto.type, OlapTableEnum.DATASERVICES)) {
            sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.fieldName).collect(Collectors.toList());
        } else if (Objects.equals(dto.type, OlapTableEnum.FACT) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT)
                || Objects.equals(dto.type, OlapTableEnum.DIMENSION) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
            sourceFieldName = config.modelPublishFieldDTOList.stream().map(e -> e.fieldEnName).collect(Collectors.toList());
        }
        String schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);
        buildAvroRecordSetWriterServiceDTO.schemaArchitecture = schemaArchitecture;
        buildAvroRecordSetWriterServiceDTO.schemaWriteStrategy = "avro-embedded";
        buildAvroRecordSetWriterServiceDTO.schemaAccessStrategy = "schema-text-property";
        BusinessResult<ControllerServiceEntity> avroRecordSetWriterService = componentsBuild.buildAvroRecordSetWriterService(buildAvroRecordSetWriterServiceDTO);
        tableNifiSettingPO.convertAvroRecordSetWriterId = avroRecordSetWriterService.data.getId();
        //--------------------------------------
        BuildAvroReaderServiceDTO buildAvroReaderServiceDTO = new BuildAvroReaderServiceDTO();
        buildAvroReaderServiceDTO.details = "transition_phase";
        buildAvroReaderServiceDTO.name = "PutDatabaseRecordTransition";
        if (groupStructureId != null) {
            buildAvroReaderServiceDTO.groupId = groupStructureId;
        } else {
            buildAvroReaderServiceDTO.groupId = appGroupId;
        }
        if (Objects.equals(dto.type, OlapTableEnum.PHYSICS) ||
                Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKPHYSICS) ||
                Objects.equals(dto.type, OlapTableEnum.DATASERVICES)) {
            sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
            tableFieldsList = config.targetDsConfig.tableFieldsList;
            for (TableFieldsDTO tableFieldsDTO : tableFieldsList) {
                if (!Objects.equals(tableFieldsDTO.fieldName, tableFieldsDTO.sourceFieldName) && StringUtils.isNotEmpty(tableFieldsDTO.sourceFieldName)) {
                    buildParameter.put("/" + tableFieldsDTO.fieldName, "/" + tableFieldsDTO.sourceFieldName);
                }
            }
            buildParameter.put("/" + tableFieldsList.get(0).fieldName, "/" + tableFieldsList.get(0).sourceFieldName);
        } else if (Objects.equals(dto.type, OlapTableEnum.FACT) ||
                Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT) ||
                Objects.equals(dto.type, OlapTableEnum.DIMENSION) ||
                Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
            List<ModelPublishFieldDTO> modelPublishFieldDTOS = config.modelPublishFieldDTOList;
            for (ModelPublishFieldDTO modelPublishFieldDTO : modelPublishFieldDTOS) {
                if (!Objects.equals(modelPublishFieldDTO.fieldEnName, modelPublishFieldDTO.sourceFieldName) && StringUtils.isNotEmpty(modelPublishFieldDTO.sourceFieldName)) {
                    buildParameter.put("/" + modelPublishFieldDTO.fieldEnName, "/" + modelPublishFieldDTO.sourceFieldName);
                }
            }
            buildParameter.put("/" + modelPublishFieldDTOS.get(0).fieldEnName, "/" + modelPublishFieldDTOS.get(0).sourceFieldName);
            sourceFieldName = config.modelPublishFieldDTOList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
        }
        schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);
        buildAvroReaderServiceDTO.schemaText = schemaArchitecture;
        BusinessResult<ControllerServiceEntity> avroReaderService = componentsBuild.buildAvroReaderService(buildAvroReaderServiceDTO);
        tableNifiSettingPO.convertPutDatabaseRecordId = avroReaderService.data.getId();
        //--------------------------------------
        buildUpdateRecordDTO.groupId = groupId;
        buildUpdateRecordDTO.details = "transition_phase";
        buildUpdateRecordDTO.name = "UpdateRecord";

        if (buildParameter.size() != 0) {
            String avro = JSON.toJSONString(buildParameter);
            avro = avro.replaceAll("）", "_");
            avro = avro.replaceAll("（", "_");
            avro = avro.replaceAll("\\)", "_");
            avro = avro.replaceAll("\\(", "_");
            buildUpdateRecordDTO.filedMap = JSON.parseObject(avro, Map.class);
        }
        buildUpdateRecordDTO.recordReader = avroReaderService.data.getId();
        buildUpdateRecordDTO.recordWriter = avroRecordSetWriterService.data.getId();
        buildUpdateRecordDTO.replacementValueStrategy = "record-path-value";
        buildUpdateRecordDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(10);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildUpdateRecord(buildUpdateRecordDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    private ProcessorEntity createUpdateField1(String appGroupId, DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto, TableNifiSettingPO tableNifiSettingPO) {
        //两个控制器服务,和一个组件,先把配置搞出来
        BuildUpdateRecordDTO buildUpdateRecordDTO = new BuildUpdateRecordDTO();
        List<TableFieldsDTO> tableFieldsList = new ArrayList<>();
        Map<String, String> buildParameter = new HashMap<>();
        String groupStructureId = dto.groupStructureId;
        BuildAvroRecordSetWriterServiceDTO buildAvroRecordSetWriterServiceDTO = new BuildAvroRecordSetWriterServiceDTO();
        buildAvroRecordSetWriterServiceDTO.details = "transition_phase";
        buildAvroRecordSetWriterServiceDTO.name = "AvroRecordSetWriterServiceTransition";
        if (groupStructureId != null) {
            buildAvroRecordSetWriterServiceDTO.groupId = groupStructureId;
        } else {
            buildAvroRecordSetWriterServiceDTO.groupId = appGroupId;
        }
        List<String> sourceFieldName = new ArrayList<>();
        if (Objects.equals(dto.type, OlapTableEnum.PHYSICS) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKPHYSICS) || Objects.equals(dto.type, OlapTableEnum.DATASERVICES)) {
            sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.fieldName).collect(Collectors.toList());
        } else if (Objects.equals(dto.type, OlapTableEnum.FACT) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT)
                || Objects.equals(dto.type, OlapTableEnum.DIMENSION) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
            sourceFieldName = config.modelPublishFieldDTOList.stream().map(e -> e.fieldEnName).collect(Collectors.toList());
        }
        if (StringUtils.isNotEmpty(dto.generateVersionSql)) {
            sourceFieldName.add(NifiConstants.AttrConstants.FI_VERSION);
        }
        sourceFieldName.add("fidata_batch_code");
        sourceFieldName.add("fidata_flow_batch_code");

        String schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);
        buildAvroRecordSetWriterServiceDTO.schemaArchitecture = schemaArchitecture;
        buildAvroRecordSetWriterServiceDTO.schemaWriteStrategy = "avro-embedded";
        buildAvroRecordSetWriterServiceDTO.schemaAccessStrategy = "schema-text-property";
        BusinessResult<ControllerServiceEntity> avroRecordSetWriterService = componentsBuild.buildAvroRecordSetWriterService(buildAvroRecordSetWriterServiceDTO);
        tableNifiSettingPO.convertAvroRecordSetWriterForCodeId = avroRecordSetWriterService.data.getId();
        //--------------------------------------
        BuildAvroReaderServiceDTO buildAvroReaderServiceDTO = new BuildAvroReaderServiceDTO();
        buildAvroReaderServiceDTO.details = "transition_phase";
        buildAvroReaderServiceDTO.name = "PutDatabaseRecordTransition";
        if (groupStructureId != null) {
            buildAvroReaderServiceDTO.groupId = groupStructureId;
        } else {
            buildAvroReaderServiceDTO.groupId = appGroupId;
        }
/*        if (Objects.equals(dto.type, OlapTableEnum.PHYSICS) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKPHYSICS)) {
            sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
        } else if (Objects.equals(dto.type, OlapTableEnum.FACT) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT)
                || Objects.equals(dto.type, OlapTableEnum.DIMENSION) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
            sourceFieldName = config.modelPublishFieldDTOList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
        }*/
        if (StringUtils.isNotEmpty(dto.generateVersionSql)) {
            schemaArchitecture = buildSchemaArchitecture(sourceFieldName.subList(0, sourceFieldName.size() - 3), config.processorConfig.targetTableName);
        } else {
            schemaArchitecture = buildSchemaArchitecture(sourceFieldName.subList(0, sourceFieldName.size() - 2), config.processorConfig.targetTableName);
        }

        buildAvroReaderServiceDTO.schemaText = schemaArchitecture;
        BusinessResult<ControllerServiceEntity> avroReaderService = componentsBuild.buildAvroReaderService(buildAvroReaderServiceDTO);
        tableNifiSettingPO.convertPutDatabaseRecordForCodeId = avroReaderService.data.getId();
        //--------------------------------------
        buildUpdateRecordDTO.groupId = groupId;
        buildUpdateRecordDTO.details = "transition_phase";
        buildUpdateRecordDTO.name = "UpdateRecord";

        //至少有一个属性
        //nifi的三元运算,如果pipelTraceId是空的,取pipelTaskTraceId当作fidata_batch_code的值
//        buildParameter.put("/fidata_batch_code", "${pipelTraceId:isEmpty():ifElse(${pipelTaskTraceId},${pipelTraceId})}");

        //2023-05-25 李世纪修改  大批次号直接拿${fidata_batch_code}，存入临时表
        buildParameter.put("/fidata_batch_code", "${fidata_batch_code}");
        buildParameter.put("/fidata_flow_batch_code", "${fragment.index}");
        if (StringUtils.isNotEmpty(dto.generateVersionSql)) {
            buildParameter.put("/fi_version", "${fi_version}");
        }
        buildUpdateRecordDTO.filedMap = buildParameter;

        buildUpdateRecordDTO.recordReader = avroReaderService.data.getId();
        buildUpdateRecordDTO.recordWriter = avroRecordSetWriterService.data.getId();
        buildUpdateRecordDTO.replacementValueStrategy = "literal-value";
        buildUpdateRecordDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(11);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildUpdateRecord(buildUpdateRecordDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    private ProcessorEntity createPutDatabaseRecordForMongo(String appGroupId, DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto, String targetDbPoolId, SynchronousTypeEnum synchronousTypeEnum, TableNifiSettingPO tableNifiSettingPO) {
        BuildAvroReaderServiceDTO data = new BuildAvroReaderServiceDTO();
        String groupStructureId = dto.groupStructureId;
        data.details = "insert_phase";
        data.name = "PutRecordToStgForMongo";
        if (groupStructureId != null) {
            data.groupId = groupStructureId;
        } else {
            data.groupId = appGroupId;
        }
        List<String> sourceFieldName = new ArrayList<>();
        if (Objects.equals(dto.type, OlapTableEnum.PHYSICS) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKPHYSICS) || Objects.equals(dto.type, OlapTableEnum.DATASERVICES)) {
            sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.fieldName).collect(Collectors.toList());
        } else if (Objects.equals(dto.type, OlapTableEnum.FACT) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT)
                || Objects.equals(dto.type, OlapTableEnum.DIMENSION) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
            sourceFieldName = config.modelPublishFieldDTOList.stream().map(e -> e.fieldEnName).collect(Collectors.toList());
        }
        if (StringUtils.isNotEmpty(dto.generateVersionSql)) {
            sourceFieldName.add(NifiConstants.AttrConstants.FI_VERSION);
        }
        sourceFieldName.add("fidata_batch_code");
        sourceFieldName.add("fidata_flow_batch_code");

        String schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);
        data.schemaText = schemaArchitecture;

        String id = "";
        //创建buildAvroReaderService
        BusinessResult<ControllerServiceEntity> controllerServiceEntityBusinessResult = componentsBuild.buildAvroReaderService(data);
        if (controllerServiceEntityBusinessResult.success) {
            id = controllerServiceEntityBusinessResult.data.getId();
        } else {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, controllerServiceEntityBusinessResult.msg);
        }
        tableNifiSettingPO.putDatabaseRecordId = id;
        PutDatabaseRecordDTO putDatabaseRecordDTO = new PutDatabaseRecordDTO();
        putDatabaseRecordDTO.name = "PutRecordToStgForMongo";
        putDatabaseRecordDTO.details = "PutRecordToStgForMongo";
        putDatabaseRecordDTO.groupId = groupId;
        //putDatabaseRecordDTO.databaseConnectionPoolingService=config.targetDsConfig.componentId;
        putDatabaseRecordDTO.databaseConnectionPoolingService = targetDbPoolId;
        //数据库类型,定义枚举
        switch (config.targetDsConfig.type) {
            case MYSQL:
            case DORIS:
                putDatabaseRecordDTO.databaseType = "MySQL";
                break;
            case SQLSERVER:
                putDatabaseRecordDTO.databaseType = "MS SQL 2012+";
                break;
            default:
                putDatabaseRecordDTO.databaseType = "MS SQL 2012+";
                break;
        }
        putDatabaseRecordDTO.recordReader = id;
        putDatabaseRecordDTO.statementType = "INSERT";
        putDatabaseRecordDTO.putDbRecordTranslateFieldNames = "false";
        //得到stg表名
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(dto.targetDbId);
        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            DataSourceDTO dataSource = fiDataDataSource.data;
            IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(dataSource.conType);
            String stgTableName = dbCommand.getStgAndTableName(config.processorConfig.targetTableName).get(0);
            //pg不需要这个配置,默认true,SQL server是false
            if (Objects.equals(dataSource.conType, DataSourceTypeEnum.SQLSERVER)) {
                putDatabaseRecordDTO.putDbRecordTranslateFieldNames = "false";
            } else if (Objects.equals(dataSource.conType, DataSourceTypeEnum.POSTGRESQL)) {
                putDatabaseRecordDTO.putDbRecordTranslateFieldNames = "true";
            }
            if (stgTableName.contains(".")) {
                String[] split = stgTableName.split("\\.");
                putDatabaseRecordDTO.TableName = split[1];
                putDatabaseRecordDTO.schemaName = split[0];
            } else {
                if (Objects.equals(dataSource.conType, DataSourceTypeEnum.SQLSERVER)) {
                    putDatabaseRecordDTO.TableName = stgTableName;
                    putDatabaseRecordDTO.schemaName = "dbo";
                } else if (Objects.equals(dataSource.conType, DataSourceTypeEnum.POSTGRESQL)) {
                    putDatabaseRecordDTO.TableName = stgTableName;
                    putDatabaseRecordDTO.schemaName = "public";
                } else if (Objects.equals(dataSource.conType, DataSourceTypeEnum.DORIS)) {
                    putDatabaseRecordDTO.TableName = stgTableName;
                }

            }
            if (Objects.equals(dto.type, OlapTableEnum.FACT) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT)
                    || Objects.equals(dto.type, OlapTableEnum.DIMENSION) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
                putDatabaseRecordDTO.TableName = stgTableName.replaceFirst("stg_", dto.prefixTempName);
            }

        } else {
            log.error("userclient无法查询到ods库的连接信息");
            throw new FkException(ResultEnum.ERROR);
        }
        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTODORIS)) {
            putDatabaseRecordDTO.TableName = config.processorConfig.targetTableName;
        }
        putDatabaseRecordDTO.concurrentTasks = ConcurrentTasks;
        putDatabaseRecordDTO.synchronousTypeEnum = synchronousTypeEnum;
        putDatabaseRecordDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(12);
        BusinessResult<ProcessorEntity> res = componentsBuild.buildPutDatabaseRecordProcess(putDatabaseRecordDTO);
        verifyProcessorResult(res);
        return res.data;
    }

    private ProcessorEntity createPutDatabaseRecord(String appGroupId, DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto, String targetDbPoolId, SynchronousTypeEnum synchronousTypeEnum, TableNifiSettingPO tableNifiSettingPO) {
        BuildAvroReaderServiceDTO data = new BuildAvroReaderServiceDTO();
        String groupStructureId = dto.groupStructureId;
        data.details = "insert_phase";
        data.name = "PutDatabaseRecord";
        if (groupStructureId != null) {
            data.groupId = groupStructureId;
        } else {
            data.groupId = appGroupId;
        }
        List<String> sourceFieldName = new ArrayList<>();
        if (Objects.equals(dto.type, OlapTableEnum.PHYSICS) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKPHYSICS) || Objects.equals(dto.type, OlapTableEnum.DATASERVICES)) {
            sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.fieldName).collect(Collectors.toList());
        } else if (Objects.equals(dto.type, OlapTableEnum.FACT) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT)
                || Objects.equals(dto.type, OlapTableEnum.DIMENSION) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
            sourceFieldName = config.modelPublishFieldDTOList.stream().map(e -> e.fieldEnName).collect(Collectors.toList());
        }
        if (StringUtils.isNotEmpty(dto.generateVersionSql)) {
            sourceFieldName.add(NifiConstants.AttrConstants.FI_VERSION);
        }
        sourceFieldName.add("fidata_batch_code");
        sourceFieldName.add("fidata_flow_batch_code");

        String schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);
        data.schemaText = schemaArchitecture;

        String id = "";
        //创建buildAvroReaderService
        BusinessResult<ControllerServiceEntity> controllerServiceEntityBusinessResult = componentsBuild.buildAvroReaderService(data);
        if (controllerServiceEntityBusinessResult.success) {
            id = controllerServiceEntityBusinessResult.data.getId();
        } else {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, controllerServiceEntityBusinessResult.msg);
        }
        tableNifiSettingPO.putDatabaseRecordId = id;
        PutDatabaseRecordDTO putDatabaseRecordDTO = new PutDatabaseRecordDTO();
        putDatabaseRecordDTO.name = "executeSQLRecord";
        putDatabaseRecordDTO.details = "executeSQLRecord";
        putDatabaseRecordDTO.groupId = groupId;
        //putDatabaseRecordDTO.databaseConnectionPoolingService=config.targetDsConfig.componentId;
        putDatabaseRecordDTO.databaseConnectionPoolingService = targetDbPoolId;
        //数据库类型,定义枚举
        switch (config.targetDsConfig.type) {
            case MYSQL:
            case DORIS:
                putDatabaseRecordDTO.databaseType = "MySQL";
                break;
            case SQLSERVER:
                putDatabaseRecordDTO.databaseType = "MS SQL 2012+";
                break;
            default:
                putDatabaseRecordDTO.databaseType = "MS SQL 2012+";
                break;
        }
        putDatabaseRecordDTO.recordReader = id;
        putDatabaseRecordDTO.statementType = "INSERT";
        putDatabaseRecordDTO.putDbRecordTranslateFieldNames = "false";
        //得到stg表名
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(dto.targetDbId);
        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            DataSourceDTO dataSource = fiDataDataSource.data;
            IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(dataSource.conType);
            String stgTableName = dbCommand.getStgAndTableName(config.processorConfig.targetTableName).get(0);
            //pg不需要这个配置,默认true,SQL server是false
            if (Objects.equals(dataSource.conType, DataSourceTypeEnum.SQLSERVER)) {
                putDatabaseRecordDTO.putDbRecordTranslateFieldNames = "false";
            } else if (Objects.equals(dataSource.conType, DataSourceTypeEnum.POSTGRESQL)) {
                putDatabaseRecordDTO.putDbRecordTranslateFieldNames = "true";
            }
            if (stgTableName.contains(".")) {
                String[] split = stgTableName.split("\\.");
                putDatabaseRecordDTO.TableName = split[1];
                putDatabaseRecordDTO.schemaName = split[0];
            } else {
                if (Objects.equals(dataSource.conType, DataSourceTypeEnum.SQLSERVER)) {
                    putDatabaseRecordDTO.TableName = stgTableName;
                    putDatabaseRecordDTO.schemaName = "dbo";
                } else if (Objects.equals(dataSource.conType, DataSourceTypeEnum.POSTGRESQL)) {
                    putDatabaseRecordDTO.TableName = stgTableName;
                    putDatabaseRecordDTO.schemaName = "public";
                } else if (Objects.equals(dataSource.conType, DataSourceTypeEnum.DORIS)) {
                    putDatabaseRecordDTO.TableName = stgTableName;
                }

            }
            if (Objects.equals(dto.type, OlapTableEnum.FACT) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT)
                    || Objects.equals(dto.type, OlapTableEnum.DIMENSION) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
                putDatabaseRecordDTO.TableName = stgTableName.replaceFirst("stg_", dto.prefixTempName);
            }

        } else {
            log.error("userclient无法查询到ods库的连接信息");
            throw new FkException(ResultEnum.ERROR);
        }
        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTODORIS)) {
            putDatabaseRecordDTO.TableName = config.processorConfig.targetTableName;
        }
        putDatabaseRecordDTO.concurrentTasks = ConcurrentTasks;
        putDatabaseRecordDTO.synchronousTypeEnum = synchronousTypeEnum;
        putDatabaseRecordDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(12);
        BusinessResult<ProcessorEntity> res = componentsBuild.buildPutDatabaseRecordProcess(putDatabaseRecordDTO);
        verifyProcessorResult(res);
        return res.data;
    }

    public static void main(String[] args) {
        String dd = "stg_ffff";
        dd = dd.replaceFirst("stg_", "temp_");
        System.out.println(dd);
    }

    private ProcessorEntity createPutDatabase(String appGroupId, DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto, String targetDbPoolId, SynchronousTypeEnum synchronousTypeEnum, TableNifiSettingPO tableNifiSettingPO, BuildTableServiceDTO buildTableService) {
        BuildAvroReaderServiceDTO data = new BuildAvroReaderServiceDTO();
        String groupStructureId = dto.groupStructureId;
        data.details = "insert_phase";
        data.name = "PutDatabaseRecord";
        if (groupStructureId != null) {
            data.groupId = groupStructureId;
        } else {
            data.groupId = appGroupId;
        }
        List<String> sourceFieldName = new ArrayList<>();
        if (Objects.equals(dto.type, OlapTableEnum.PHYSICS) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKPHYSICS) || Objects.equals(dto.type, OlapTableEnum.DATASERVICES)) {
            sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.fieldName).collect(Collectors.toList());
        } else if (Objects.equals(dto.type, OlapTableEnum.FACT) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT)
                || Objects.equals(dto.type, OlapTableEnum.DIMENSION) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
            sourceFieldName = config.modelPublishFieldDTOList.stream().map(e -> e.fieldEnName).collect(Collectors.toList());
        }
        if (StringUtils.isNotEmpty(dto.generateVersionSql)) {
            sourceFieldName.add(NifiConstants.AttrConstants.FI_VERSION);
        }
        sourceFieldName.add("fidata_batch_code");
        sourceFieldName.add("fidata_flow_batch_code");

        String schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);
        data.schemaText = schemaArchitecture;

        String id = "";
        //创建buildAvroReaderService
        BusinessResult<ControllerServiceEntity> controllerServiceEntityBusinessResult = componentsBuild.buildAvroReaderService(data);
        if (controllerServiceEntityBusinessResult.success) {
            id = controllerServiceEntityBusinessResult.data.getId();
        } else {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, controllerServiceEntityBusinessResult.msg);
        }
        tableNifiSettingPO.putDatabaseRecordId = id;
        PutDatabaseRecordDTO putDatabaseRecordDTO = new PutDatabaseRecordDTO();
        putDatabaseRecordDTO.name = "executeSQLRecord";
        putDatabaseRecordDTO.details = "executeSQLRecord";
        putDatabaseRecordDTO.groupId = groupId;
        //putDatabaseRecordDTO.databaseConnectionPoolingService=config.targetDsConfig.componentId;
        putDatabaseRecordDTO.databaseConnectionPoolingService = targetDbPoolId;
        putDatabaseRecordDTO.databaseType = "MS SQL 2012+";//数据库类型,定义枚举
        putDatabaseRecordDTO.recordReader = id;
        putDatabaseRecordDTO.statementType = "INSERT";
        putDatabaseRecordDTO.putDbRecordTranslateFieldNames = "false";
        //得到stg表名
        putDatabaseRecordDTO.TableName = buildTableService.targetTable;
        putDatabaseRecordDTO.schemaName = buildTableService.schemaName;
        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTODORIS)) {
            putDatabaseRecordDTO.TableName = config.processorConfig.targetTableName;
        }
        putDatabaseRecordDTO.concurrentTasks = ConcurrentTasks;
        putDatabaseRecordDTO.synchronousTypeEnum = synchronousTypeEnum;
        putDatabaseRecordDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(11);
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
     * 组件连接器 是否集群
     *
     * @param groupId  组id
     * @param sourceId 连接器上方组件id
     * @param targetId 连接器下方组件id
     * @param type     连接类型
     */
    private void componentConnectorForNifiClusterEnable(String groupId, String sourceId, String targetId, AutoEndBranchTypeEnum type) {
        BusinessResult<ConnectionEntity> sqlToPutRes = componentsBuild.buildConnectProcessorsForNifiClusterEnable(groupId, sourceId, targetId, type);
        verifyProcessorResult(sqlToPutRes);
    }

    /**
     * 组件连接器,多种路由
     *
     * @param groupId  组id
     * @param sourceId 连接器上方组件id
     * @param targetId 连接器下方组件id
     * @param type     连接类型
     */
    private void componentsConnector(String groupId, String sourceId, String targetId, List<AutoEndBranchTypeEnum> type) {
        BusinessResult<ConnectionEntity> sqlToPutRes = componentsBuild.buildConnectProcessor(groupId, sourceId, targetId, type);
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
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(7);

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
    private ProcessorEntity replaceTextProcessor(DataAccessConfigDTO config, String groupId) {
        BuildReplaceTextProcessorDTO dto = new BuildReplaceTextProcessorDTO();
        dto.name = "Build MQ Message";
        dto.details = "build json string";
        dto.groupId = groupId;
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(12);
        dto.replacementValue = "{ \"code\": \"${" + NifiConstants.AttrConstants.LOG_CODE + "}\" " + "," + "\"corn\":\"" + config.processorConfig.scheduleExpression + "\"}";

        BusinessResult<ProcessorEntity> res = componentsBuild.buildReplaceTextProcess(dto, new ArrayList<>());
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
        //dto.host = host;
        //dto.port = port;
        dto.exchange = MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME;
        dto.route = MqConstants.RouterConstants.INCREMENT_RESULT;
        //dto.vhost = vhost;
        //dto.user = username;
        //dto.pwd = password;

        BusinessResult<ProcessorEntity> res = componentsBuild.buildPublishMqProcess(dto);
        verifyProcessorResult(res);
        return res.data;
    }

    /**
     * 插入日志组件
     *
     * @param groupId  组id
     * @param dbPoolId 连接池id
     * @param dto      dto
     * @param config   config
     * @return 组件对象
     */
    private ProcessorEntity putLogProcessor(String groupId, String dbPoolId, BuildNifiFlowDTO dto, DataAccessConfigDTO config) {
        BuildPutSqlProcessorDTO putSqlDto = new BuildPutSqlProcessorDTO();
        putSqlDto.name = "Put Log to Config Db";
        putSqlDto.details = "query_phase";
        putSqlDto.groupId = groupId;
        putSqlDto.dbConnectionId = dbPoolId;
        putSqlDto.sqlStatement = buildLogSql(dto, config.processorConfig.sourceExecSqlQuery);
        putSqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(7);

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
    private ProcessorEntity splitJsonProcessor(String groupId) {
        BuildSplitJsonProcessorDTO buildSplitJsonProcessorDTO = new BuildSplitJsonProcessorDTO();
        buildSplitJsonProcessorDTO.name = "SplitJson";
        buildSplitJsonProcessorDTO.details = "SplitJson";
        buildSplitJsonProcessorDTO.groupId = groupId;
        buildSplitJsonProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(8);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildSplitJsonProcess(buildSplitJsonProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    private ProcessorEntity sqlParameterProcessor(DataAccessConfigDTO config, String groupId) {
        BuildProcessEvaluateJsonPathDTO dto = new BuildProcessEvaluateJsonPathDTO();
        dto.name = "sqlParameterProcessor";
        dto.details = "sqlParameterProcessor";
        dto.groupId = groupId;
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(9);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildSqlParameterProcess(config, dto);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    private ProcessorEntity assembleSql(DataAccessConfigDTO config, String groupId) {
        BuildReplaceTextProcessorDTO dto = new BuildReplaceTextProcessorDTO();
        dto.name = "assembleSql";
        dto.details = "assembleSql";
        dto.groupId = groupId;
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(10);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildAssembleSqlProcess(config, dto);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    private ProcessorEntity CallDbProcedurev1(DataAccessConfigDTO config, String groupId, String targetDbPoolId, SynchronousTypeEnum synchronousTypeEnum, BuildNifiFlowDTO buildNifiFlow) {
        BuildCallDbProcedureProcessorDTO callDbProcedureProcessorDTO = new BuildCallDbProcedureProcessorDTO();
        callDbProcedureProcessorDTO.name = "CallDbProcedure";
        callDbProcedureProcessorDTO.details = "insert_phase";
        callDbProcedureProcessorDTO.groupId = groupId;
        String executsql = "";
        //config.processorConfig.targetTableName = "stg_" + config.processorConfig.targetTableName;
        String syncMode = syncModeTypeEnum.getNameByValue(config.targetDsConfig.syncMode);
        log.info("同步类型为:" + syncMode + config.targetDsConfig.syncMode);
        executsql = componentsBuild.assemblySql(config, synchronousTypeEnum, FuncNameEnum.PG_DATA_STG_TO_ODS_TOTAL.getName(), buildNifiFlow);
        //callDbProcedureProcessorDTO.dbConnectionId=config.targetDsConfig.componentId;
        callDbProcedureProcessorDTO.dbConnectionId = targetDbPoolId;

        log.info("SQL预览语句：{}", JSON.toJSONString(buildNifiFlow.syncStgToOdsSql));

        log.info("数仓外键语句：{}", JSON.toJSONString(buildNifiFlow.updateSql));
        callDbProcedureProcessorDTO.executsql = StringUtils.isNotEmpty(buildNifiFlow.syncStgToOdsSql) ? buildNifiFlow.syncStgToOdsSql : executsql;
        callDbProcedureProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(11);
        callDbProcedureProcessorDTO.haveNextOne = true;
        // 组件并发数量
        callDbProcedureProcessorDTO.concurrencyNums = buildNifiFlow.concurrencyNums;

        //todo: CallDbProcedure组件：自定义的加载前sql和加载后sql
        log.info("数仓自定义加载前语句：{}", JSON.toJSONString(buildNifiFlow.customScriptBefore));
        log.info("数仓自定义加载后语句：{}", JSON.toJSONString(buildNifiFlow.customScriptAfter));
        //获取外键sql
        String updateSql = buildNifiFlow.updateSql;
        //声明自定义加载前sql
        String customScriptBefore = buildNifiFlow.customScriptBefore;
        //声明presql
        String sqlPreQuery = null;
        //通过判断 updateSql的值是否为空，决定最终的 sqlPreQuery
        if (StringUtils.isNotBlank(updateSql) && StringUtils.isNotBlank(customScriptBefore)) {
            sqlPreQuery = updateSql + customScriptBefore;
        } else if (StringUtils.isNotBlank(updateSql) && StringUtils.isBlank(customScriptBefore)) {
            sqlPreQuery = updateSql;
        } else if (StringUtils.isNotBlank(customScriptBefore) && StringUtils.isBlank(updateSql)) {
            sqlPreQuery = customScriptBefore;
        }

        callDbProcedureProcessorDTO.sqlPreQuery = sqlPreQuery;
        callDbProcedureProcessorDTO.sqlPostQuery = buildNifiFlow.customScriptAfter;

        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildCallDbProcedureProcess(callDbProcedureProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    private ProcessorEntity CallDbProcedure(DataAccessConfigDTO config, String groupId, String targetDbPoolId, SynchronousTypeEnum synchronousTypeEnum, BuildNifiFlowDTO buildNifiFlow) {
        BuildCallDbProcedureProcessorDTO callDbProcedureProcessorDTO = new BuildCallDbProcedureProcessorDTO();
        callDbProcedureProcessorDTO.name = "CallDbProcedure";
        callDbProcedureProcessorDTO.details = "insert_phase";
        callDbProcedureProcessorDTO.groupId = groupId;
        String executsql = "";
        //config.processorConfig.targetTableName = "stg_" + config.processorConfig.targetTableName;
        String syncMode = syncModeTypeEnum.getNameByValue(config.targetDsConfig.syncMode);
        log.info("同步类型为:" + syncMode + config.targetDsConfig.syncMode);
        executsql = componentsBuild.assemblySql(config, synchronousTypeEnum, FuncNameEnum.PG_DATA_STG_TO_ODS_TOTAL.getName(), buildNifiFlow);
        //callDbProcedureProcessorDTO.dbConnectionId=config.targetDsConfig.componentId;
        callDbProcedureProcessorDTO.dbConnectionId = targetDbPoolId;

        log.info("SQL预览语句：{}", JSON.toJSONString(buildNifiFlow.syncStgToOdsSql));
        log.info("数仓外键语句：{}", JSON.toJSONString(buildNifiFlow.updateSql));
        callDbProcedureProcessorDTO.executsql = StringUtils.isNotEmpty(buildNifiFlow.syncStgToOdsSql) ? buildNifiFlow.syncStgToOdsSql : executsql;
        callDbProcedureProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(13);
        callDbProcedureProcessorDTO.haveNextOne = true;
        // 组件并发数量
        callDbProcedureProcessorDTO.concurrencyNums = buildNifiFlow.concurrencyNums;

        //todo: CallDbProcedure组件：自定义的加载前sql和加载后sql
        log.info("数仓自定义加载前语句：{}", JSON.toJSONString(buildNifiFlow.customScriptBefore));
        log.info("数仓自定义加载后语句：{}", JSON.toJSONString(buildNifiFlow.customScriptAfter));
        //获取外键sql
        String updateSql = buildNifiFlow.updateSql;
        //声明自定义加载前sql
        String customScriptBefore = buildNifiFlow.customScriptBefore;
        //声明presql
        String sqlPreQuery = null;
        //通过判断 updateSql的值是否为空，决定最终的 sqlPreQuery
        if (StringUtils.isNotBlank(updateSql) && StringUtils.isNotBlank(customScriptBefore)) {
            sqlPreQuery = updateSql + customScriptBefore;
        } else if (StringUtils.isNotBlank(updateSql) && StringUtils.isBlank(customScriptBefore)) {
            sqlPreQuery = updateSql;
        } else if (StringUtils.isNotBlank(customScriptBefore) && StringUtils.isBlank(updateSql)) {
            sqlPreQuery = customScriptBefore;
        }

        callDbProcedureProcessorDTO.sqlPreQuery = sqlPreQuery;

        //2024-11-14 数仓建模的加载后语句不再由nifi执行
//        callDbProcedureProcessorDTO.sqlPostQuery = buildNifiFlow.customScriptAfter;
        callDbProcedureProcessorDTO.sqlPostQuery = null;

        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildCallDbProcedureProcess(callDbProcedureProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    private ProcessorEntity CallDbProcedureForDoris(DataAccessConfigDTO config, String groupId, String targetDbPoolId, SynchronousTypeEnum synchronousTypeEnum, BuildNifiFlowDTO buildNifiFlow) {
        BuildCallDbProcedureProcessorDTO callDbProcedureProcessorDTO = new BuildCallDbProcedureProcessorDTO();
        callDbProcedureProcessorDTO.name = "CallDbProcedureForDoris";
        callDbProcedureProcessorDTO.details = "insert_phase";
        callDbProcedureProcessorDTO.groupId = groupId;
        String executsql = "";
        //config.processorConfig.targetTableName = "stg_" + config.processorConfig.targetTableName;
        String syncMode = syncModeTypeEnum.getNameByValue(config.targetDsConfig.syncMode);
        log.info("同步类型为:" + syncMode + config.targetDsConfig.syncMode);
        executsql = componentsBuild.assemblySql(config, synchronousTypeEnum, FuncNameEnum.PG_DATA_STG_TO_ODS_TOTAL.getName(), buildNifiFlow);
        //callDbProcedureProcessorDTO.dbConnectionId=config.targetDsConfig.componentId;
        callDbProcedureProcessorDTO.dbConnectionId = targetDbPoolId;

        log.info("SQL预览语句：{}", JSON.toJSONString(buildNifiFlow.syncStgToOdsSql));
        log.info("数仓外键语句：{}", JSON.toJSONString(buildNifiFlow.updateSql));
        callDbProcedureProcessorDTO.executsql = StringUtils.isNotEmpty(buildNifiFlow.syncStgToOdsSql) ? buildNifiFlow.syncStgToOdsSql : executsql;
        callDbProcedureProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(13);
        callDbProcedureProcessorDTO.haveNextOne = true;
        // 组件并发数量
        callDbProcedureProcessorDTO.concurrencyNums = buildNifiFlow.concurrencyNums;

        //todo: CallDbProcedure组件：自定义的加载前sql和加载后sql
        log.info("数仓自定义加载前语句：{}", JSON.toJSONString(buildNifiFlow.customScriptBefore));
        log.info("数仓自定义加载后语句：{}", JSON.toJSONString(buildNifiFlow.customScriptAfter));
        //获取外键sql
        String sqlPreQuery = buildNifiFlow.customScriptBefore;

        callDbProcedureProcessorDTO.sqlPreQuery = sqlPreQuery;
        //2024-11-14 数仓建模的加载后语句不再由nifi执行
//        callDbProcedureProcessorDTO.sqlPostQuery = buildNifiFlow.customScriptAfter;
        callDbProcedureProcessorDTO.sqlPostQuery = null;

        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildCallDbProcedureProcessForDoris(callDbProcedureProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    private ProcessorEntity CallDbProcedureForDorisKeySql(DataAccessConfigDTO config, String groupId, String targetDbPoolId, SynchronousTypeEnum synchronousTypeEnum, BuildNifiFlowDTO buildNifiFlow, Integer x) {
        BuildCallDbProcedureProcessorDTO callDbProcedureProcessorDTO = new BuildCallDbProcedureProcessorDTO();
        callDbProcedureProcessorDTO.name = "UpdateDorisKeySql";
        callDbProcedureProcessorDTO.details = "insert_phase";
        callDbProcedureProcessorDTO.groupId = groupId;
        String executsql = "";
        //config.processorConfig.targetTableName = "stg_" + config.processorConfig.targetTableName;
        String syncMode = syncModeTypeEnum.getNameByValue(config.targetDsConfig.syncMode);
        log.info("同步类型为:" + syncMode + config.targetDsConfig.syncMode);
        executsql = buildNifiFlow.updateSql;
        //callDbProcedureProcessorDTO.dbConnectionId=config.targetDsConfig.componentId;
        callDbProcedureProcessorDTO.dbConnectionId = targetDbPoolId;

        log.info("数仓外键语句：{}", JSON.toJSONString(executsql));
        callDbProcedureProcessorDTO.executsql = executsql;
        callDbProcedureProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(-x - 3, 13);
        callDbProcedureProcessorDTO.haveNextOne = true;
        // 组件并发数量
        callDbProcedureProcessorDTO.concurrencyNums = buildNifiFlow.concurrencyNums;
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildCallDbProcedureProcess(callDbProcedureProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

//    /**
//     * 该组件创建在CallDbProcedure之前，专门用于数仓建模的关联维度外键的sql执行
//     * 2023-05-11李世纪修改
//     *
//     * @param config
//     * @param groupId
//     * @param targetDbPoolId
//     * @param synchronousTypeEnum
//     * @param buildNifiFlow
//     * @return
//     */
//    private ProcessorEntity executeSqlRecordForKey(DataAccessConfigDTO config, String groupId, String targetDbPoolId, SynchronousTypeEnum synchronousTypeEnum, BuildNifiFlowDTO buildNifiFlow) {
//        BuildCallDbProcedureProcessorDTO callDbProcedureProcessorDTO = new BuildCallDbProcedureProcessorDTO();
//        callDbProcedureProcessorDTO.name = "executeSqlRecord";
//        callDbProcedureProcessorDTO.details = "insert_phase";
//        callDbProcedureProcessorDTO.groupId = groupId;
//        String executsql = "";
//        //config.processorConfig.targetTableName = "stg_" + config.processorConfig.targetTableName;
//        String syncMode = syncModeTypeEnum.getNameByValue(config.targetDsConfig.syncMode);
//        log.info("同步类型为:" + syncMode + config.targetDsConfig.syncMode);
//        //callDbProcedureProcessorDTO.dbConnectionId=config.targetDsConfig.componentId;
//        callDbProcedureProcessorDTO.dbConnectionId = targetDbPoolId;
//        //数仓建模，关联外键的sql
//        callDbProcedureProcessorDTO.executsql = buildNifiFlow.updateSql;
//        callDbProcedureProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(12);
//        callDbProcedureProcessorDTO.haveNextOne = true;
//
//        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildCallDbProcedureProcess(callDbProcedureProcessorDTO);
//        verifyProcessorResult(processorEntityBusinessResult);
//        return processorEntityBusinessResult.data;
//    }


    private ProcessorEntity queryNumbersProcessor(BuildNifiFlowDTO dto, DataAccessConfigDTO config, String groupId, String targetDbPoolId) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Query numbers Field";
        querySqlDto.details = "insert_phase";
        querySqlDto.groupId = groupId;
        //接入需要数据校验,查的是ods表,其他的不变
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceOdsId));
        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            DataSourceDTO data = fiDataDataSource.data;
            IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(data.conType);
            String sql = dbCommand.queryNumbersField(dto, config, groupId);
            querySqlDto.querySql = sql;
        } else {
            log.error("userclient无法查询到ods库的连接信息");
            throw new FkException(ResultEnum.ERROR);
        }
        querySqlDto.dbConnectionId = targetDbPoolId;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(14);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<>());
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    private ProcessorEntity queryNumbersProcessorForDoris(BuildNifiFlowDTO dto, DataAccessConfigDTO config, String groupId, String targetDbPoolId) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Query numbers Field For Doris";
        querySqlDto.details = "insert_phase";
        querySqlDto.groupId = groupId;
        //接入需要数据校验,查的是ods表,其他的不变
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceOdsId));
        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            DataSourceDTO data = fiDataDataSource.data;
            //doris暂时用配置库的连接信息去查  mpp分布式查不到会导致流程报错
            IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(DataSourceTypeEnum.MYSQL);
            String sql = dbCommand.queryNumbersField(dto, config, groupId);
            querySqlDto.querySql = sql;
        } else {
            log.error("userclient无法查询到ods库的连接信息");
            throw new FkException(ResultEnum.ERROR);
        }
        querySqlDto.dbConnectionId = targetDbPoolId;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(14);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<>());
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }


    private ProcessorEntity queryNumbers(BuildNifiFlowDTO dto, DataAccessConfigDTO config, String groupId, String targetDbPoolId, String afterSql, Integer targetDbId) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Query numbers Field";
        querySqlDto.preSql = afterSql;
        querySqlDto.details = "insert_phase";
        querySqlDto.groupId = groupId;
        //接入需要数据校验,查的是ods表,其他的不变
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(targetDbId);
        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            DataSourceDTO data = fiDataDataSource.data;
            IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(data.conType);
            String sql = dbCommand.queryNumbersFieldForTableServer(dto, config, groupId);
            querySqlDto.querySql = sql;
        } else {
            log.error("userclient无法查询到ods库的连接信息");
            throw new FkException(ResultEnum.ERROR);
        }
        querySqlDto.dbConnectionId = targetDbPoolId;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(13);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<>());
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    private ProcessorEntity queryNumbers(String groupId, String targetControllerServiceId, NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Query numbers Field";
        querySqlDto.details = "insert_phase";
        querySqlDto.groupId = groupId;
        querySqlDto.querySql = "select '${pipelTraceId}' as pipelTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId,'${pipelStageTraceId}' as pipelStageTraceId,'${taskId}' as taskId;";
        querySqlDto.dbConnectionId = targetControllerServiceId;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(4);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<>());
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    private ProcessorEntity CallDbLogProcedure(DataAccessConfigDTO config, String groupId, String cfgDbPoolId) {
        BuildCallDbProcedureProcessorDTO callDbProcedureProcessorDTO = new BuildCallDbProcedureProcessorDTO();
        callDbProcedureProcessorDTO.name = "CallDbLogProcedure";
        callDbProcedureProcessorDTO.details = "insert_phase";
        callDbProcedureProcessorDTO.groupId = groupId;
        //调用存储过程sql,存日志
        String executsql1 = "UPDATE tb_etl_log SET `status` =1,enddate='${" + NifiConstants.AttrConstants.END_TIME + "}',datarows='${" + NifiConstants.AttrConstants.NUMBERS + "}',topic_name='${" + NifiConstants.AttrConstants.KAFKA_TOPIC + "}' ";
//        String executsql1 = "UPDATE tb_etl_log SET `status` =1,enddate=now(),datarows='${" + NifiConstants.AttrConstants.NUMBERS + "}',topic_name='${" + NifiConstants.AttrConstants.KAFKA_TOPIC + "}' ";
        executsql1 += "WHERE\n" +
                "\tcode='${pipelTraceId:isEmpty():ifElse(${pipelTaskTraceId},${pipelTraceId})}' and tablename='" + config.targetDsConfig.targetTableName + "';\n";
        executsql1 += "update tb_etl_Incremental  set incremental_objectivescore_start='${incremental_objectivescore_end}', enable_flag=2 " +
                "where object_name = '" + config.targetDsConfig.targetTableName + "' ;";
        callDbProcedureProcessorDTO.dbConnectionId = cfgDbPoolId;
        callDbProcedureProcessorDTO.executsql = executsql1;
        callDbProcedureProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(17);
        callDbProcedureProcessorDTO.haveNextOne = false;
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
        toSqlDto.tableName = config.processorConfig.targetTableName;
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
    private ProcessorEntity convertJsonProcessor(String groupId, int x, int y) {
        BuildConvertToJsonProcessorDTO toJsonDto = new BuildConvertToJsonProcessorDTO();
        toJsonDto.name = "Convert Data To Json";
        toJsonDto.details = "query_phase";
        toJsonDto.groupId = groupId;
        toJsonDto.positionDTO = NifiPositionHelper.buildXYPositionDTO(x, y);
        BusinessResult<ProcessorEntity> toJsonRes = componentsBuild.buildConvertToJsonProcess(toJsonDto);
        verifyProcessorResult(toJsonRes);
        return toJsonRes.data;
    }

    public ProcessorEntity evaluateNumbersProcessor(String groupId) {
        BuildProcessEvaluateJsonPathDTO dto = new BuildProcessEvaluateJsonPathDTO();
        dto.name = "Set numbers Field";
        dto.details = "insert_phase";
        dto.groupId = groupId;
        List<String> strings = new ArrayList<>();
        strings.add(NifiConstants.AttrConstants.NUMBERS);
        strings.add(NifiConstants.AttrConstants.END_TIME);
        strings.add(NifiConstants.AttrConstants.TABLE_TYPE);
        strings.add(NifiConstants.AttrConstants.TABLE_ID);
        dto.selfDefinedParameter = strings;
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(16);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildEvaluateJsonPathProcess(dto);
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
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
        querySqlDto.MaxRowsPerFlowFile = "1000000";
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
    private ProcessorEntity execDeleteSqlProcessor(DataAccessConfigDTO config, String groupId, String targetDbPoolId, SynchronousTypeEnum synchronousTypeEnum, BuildNifiFlowDTO buildNifiFlow) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Exec Target Delete";
        querySqlDto.details = "query_phase";
        //2023-05-05 李世纪注释掉，目前该功能由sql预览模块施行
//        querySqlDto.postSql = buildNifiFlow.whereScript;
        querySqlDto.preSql = buildNifiFlow.buildTableSql;
        querySqlDto.groupId = groupId;

        //2023-04-28:李世纪注释，
//        querySqlDto.querySql = componentsBuild.assemblySql(config, synchronousTypeEnum, FuncNameEnum.PG_DATA_STG_TO_ODS_DELETE.getName(), buildNifiFlow);
        //2023-04-28李世纪修改：目前数据接入页面配置的stg过期时间需要起作用，因此这里改为从传递过来的参数获取执行删除的sql
        //2023-05-18李世纪修正：如果buildNifiFlow.deleteScript为空，仍然调用存储过程，防止nifi流程报错
        querySqlDto.querySql = StringUtils.isEmpty(buildNifiFlow.deleteScript) ?
                componentsBuild.assemblySql(config, synchronousTypeEnum, FuncNameEnum.PG_DATA_STG_TO_ODS_DELETE.getName(), buildNifiFlow)
                : buildNifiFlow.deleteScript;

        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTODORIS)) {
            querySqlDto.querySql = "TRUNCATE table " + config.processorConfig.targetTableName;
        }
        querySqlDto.dbConnectionId = targetDbPoolId;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(8);
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
    private ProcessorEntity execDeleteSqlProcessorForDoris(DataAccessConfigDTO config, String groupId, String targetDbPoolId, SynchronousTypeEnum synchronousTypeEnum, BuildNifiFlowDTO buildNifiFlow) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Exec Target Delete";
        querySqlDto.details = "query_phase";
        //2023-05-05 李世纪注释掉，目前该功能由sql预览模块施行
//        querySqlDto.postSql = buildNifiFlow.whereScript;
        //todo:数仓临时表建表语句暂时不放在nifi执行
//        querySqlDto.preSql = buildNifiFlow.buildTableSql;
        querySqlDto.groupId = groupId;

        //2023-04-28:李世纪注释，
//        querySqlDto.querySql = componentsBuild.assemblySql(config, synchronousTypeEnum, FuncNameEnum.PG_DATA_STG_TO_ODS_DELETE.getName(), buildNifiFlow);
        //2023-04-28李世纪修改：目前数据接入页面配置的stg过期时间需要起作用，因此这里改为从传递过来的参数获取执行删除的sql
        //2023-05-18李世纪修正：如果buildNifiFlow.deleteScript为空，仍然调用存储过程，防止nifi流程报错
        querySqlDto.querySql = StringUtils.isEmpty(buildNifiFlow.deleteScript) ?
                componentsBuild.assemblySql(config, synchronousTypeEnum, FuncNameEnum.PG_DATA_STG_TO_ODS_DELETE.getName(), buildNifiFlow)
                : buildNifiFlow.deleteScript;

        //如果是doris - 数仓的全量同步 则此组件也要清空目标表
        log.info("本次同步方式：" + buildNifiFlow.synMode);
        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTOPG)) {
            if (buildNifiFlow.synMode == 1) {
                String truncateTargetTblSql = buildNifiFlow.deleteScript;
                truncateTargetTblSql = truncateTargetTblSql.replaceFirst("temp_", "");
                querySqlDto.postSql = truncateTargetTblSql;
            }
        }

        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTODORIS)) {
            querySqlDto.querySql = "TRUNCATE table " + config.processorConfig.targetTableName;
        }

        querySqlDto.dbConnectionId = targetDbPoolId;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(8);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcessForDoris(querySqlDto, new ArrayList<String>());
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }


    /**
     * 执行sql delete组件
     *
     * @param groupId        组id
     * @param targetDbPoolId ods连接池id
     * @return 组件对象
     */
    private ProcessorEntity execBeforeSqlProcessor(String groupId, String targetDbPoolId, String beforeSql) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Exec Target Delete";
        querySqlDto.details = "query_phase";
        querySqlDto.groupId = groupId;
        querySqlDto.querySql = beforeSql;
        querySqlDto.dbConnectionId = targetDbPoolId;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(8);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<String>());
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    /**
     * 调用api参数组件
     *
     * @param config  数据接入配置
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity replaceTextProcessv1(DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto) {
        BuildReplaceTextProcessorDTO buildReplaceTextProcessorDTO = new BuildReplaceTextProcessorDTO();
        //校验通过修改字段集合
        HashMap<String, Object> updateFieldMap_Y = new HashMap<>();
        updateFieldMap_Y.put("fi_sync_type", 2);
        updateFieldMap_Y.put("fi_verify_type", 3);
        //校验不通过修改字段集合
        HashMap<String, Object> updateFieldMap_N = new HashMap<>();
        updateFieldMap_N.put("fi_sync_type", 3);
        updateFieldMap_N.put("fi_verify_type", 2);
        //校验不通过但校验规则为弱类型规则修改字段集合
        HashMap<String, Object> updateFieldMap_R = new HashMap<>();
        updateFieldMap_R.put("fi_sync_type", 2);
        updateFieldMap_R.put("fi_verify_type", 4);
        //校验依据字段集合
        HashMap<String, Object> checkByFieldMap = new HashMap<>();
//        checkByFieldMap.put("fidata_flow_batch_code", "${fragment.index}");
        checkByFieldMap.put("fidata_batch_code", "${fidata_batch_code}");
        DataCheckSyncDTO dataCheckSyncDTO = new DataCheckSyncDTO();
        dataCheckSyncDTO.fiDataDataSourceId = "2";
        //异常信息字段
//        dataCheckSyncDTO.msgField = "error_message";
        dataCheckSyncDTO.msgField = "fi_error_message";
        dataCheckSyncDTO.updateFieldMap_Y = updateFieldMap_Y;
        dataCheckSyncDTO.updateFieldMap_N = updateFieldMap_N;
        dataCheckSyncDTO.updateFieldMap_R = updateFieldMap_R;
        dataCheckSyncDTO.checkByFieldMap = checkByFieldMap;
        dataCheckSyncDTO.tablePrefix = "stg_";
        dataCheckSyncDTO.tableUnique = String.valueOf(dto.id);
        String tableName = dto.tableName;
        if (tableName.contains(".")) {
            dataCheckSyncDTO.tableName = tableName.split("\\.")[1];
        } else {
            dataCheckSyncDTO.tableName = tableName;
        }

        dataCheckSyncDTO.tableBusinessType = TableBusinessTypeEnum.NONE;
        dataCheckSyncDTO.uniqueField = dto.pkName;

        buildReplaceTextProcessorDTO.name = "GenerateFlowFileProcessor";
        buildReplaceTextProcessorDTO.details = "query_phase";
        buildReplaceTextProcessorDTO.groupId = groupId;
        buildReplaceTextProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(-2, 10);
        //替换流文件
        buildReplaceTextProcessorDTO.evaluationMode = "Entire text";
        buildReplaceTextProcessorDTO.maximumBufferSize = "100 MB";
        buildReplaceTextProcessorDTO.replacementValue = JSON.toJSONString(dataCheckSyncDTO);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildReplaceTextProcess(buildReplaceTextProcessorDTO, new ArrayList<>());
        return processorEntityBusinessResult.data;
    }

    /**
     * 调用api参数组件
     *
     * @param config  数据接入配置
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity replaceTextProcess(DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto) {
        BuildReplaceTextProcessorDTO buildReplaceTextProcessorDTO = new BuildReplaceTextProcessorDTO();
        //校验通过修改字段集合
        HashMap<String, Object> updateFieldMap_Y = new HashMap<>();
        updateFieldMap_Y.put("fi_verify_type", "3");
        updateFieldMap_Y.put("fi_sync_type", "2");
        //校验不通过修改字段集合
        HashMap<String, Object> updateFieldMap_N = new HashMap<>();
        updateFieldMap_N.put("fi_sync_type", 3);
        updateFieldMap_N.put("fi_verify_type", 2);
        //校验不通过但校验规则为弱类型规则修改字段集合
        HashMap<String, Object> updateFieldMap_R = new HashMap<>();
        updateFieldMap_R.put("fi_sync_type", 2);
        updateFieldMap_R.put("fi_verify_type", 4);
        //校验依据字段集合
        HashMap<String, Object> checkByFieldMap = new HashMap<>();
        checkByFieldMap.put("fidata_batch_code", "${fidata_batch_code}");
        checkByFieldMap.put("fidata_flow_batch_code", "${fragment.index}");
        DataCheckSyncDTO dataCheckSyncDTO = new DataCheckSyncDTO();
        dataCheckSyncDTO.fiDataDataSourceId = "2";
        dataCheckSyncDTO.msgField = "fi_error_message";
        dataCheckSyncDTO.updateFieldMap_Y = updateFieldMap_Y;
        dataCheckSyncDTO.updateFieldMap_N = updateFieldMap_N;
        dataCheckSyncDTO.updateFieldMap_R = updateFieldMap_R;
        dataCheckSyncDTO.checkByFieldMap = checkByFieldMap;
        dataCheckSyncDTO.tablePrefix = "stg_";
        dataCheckSyncDTO.tableUnique = String.valueOf(dto.id);
        String tableName = dto.tableName;
        if (tableName.contains(".")) {
            dataCheckSyncDTO.tableName = tableName.split("\\.")[1];
        } else {
            dataCheckSyncDTO.tableName = tableName;
        }

        dataCheckSyncDTO.tableBusinessType = TableBusinessTypeEnum.NONE;
        dataCheckSyncDTO.uniqueField = dto.pkName;

        buildReplaceTextProcessorDTO.name = "GenerateFlowFileProcessor";
        buildReplaceTextProcessorDTO.details = "query_phase";
        buildReplaceTextProcessorDTO.groupId = groupId;
        buildReplaceTextProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(-2, 12);
        //替换流文件
        buildReplaceTextProcessorDTO.evaluationMode = "Entire text";
        buildReplaceTextProcessorDTO.maximumBufferSize = "100 MB";
        buildReplaceTextProcessorDTO.replacementValue = JSON.toJSONString(dataCheckSyncDTO);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildReplaceTextProcess(buildReplaceTextProcessorDTO, new ArrayList<>());
        return processorEntityBusinessResult.data;
    }

    /**
     * 调用api参数组件
     *
     * @param config  数据接入配置
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity replaceTextForFtpProcess(DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto) {
        BuildReplaceTextProcessorDTO buildReplaceTextProcessorDTO = new BuildReplaceTextProcessorDTO();
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyStr", "${kafka.topic}");


        buildReplaceTextProcessorDTO.name = "replaceTextForFtpProcess";
        buildReplaceTextProcessorDTO.details = "query_phase";
        buildReplaceTextProcessorDTO.groupId = groupId;
        buildReplaceTextProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(-1, 9);
        //替换流文件
        buildReplaceTextProcessorDTO.evaluationMode = "Entire text";
        buildReplaceTextProcessorDTO.maximumBufferSize = "100 MB";
        //替换为map
        buildReplaceTextProcessorDTO.replacementValue = JSON.toJSONString(map);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildReplaceTextProcess(buildReplaceTextProcessorDTO, new ArrayList<>());
        return processorEntityBusinessResult.data;
    }

    /**
     * 调用api参数组件
     *
     * @param config  数据接入配置
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity replaceTextForFtpDataUploadProcess(DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto) {
        BuildReplaceTextProcessorDTO buildReplaceTextProcessorDTO = new BuildReplaceTextProcessorDTO();
        HashMap<String, Object> map = new HashMap<>();
        //放入topic
        map.put("topic", "${kafka.topic}");
        //放入大批次号
        map.put("fidata_batch_code", "${fidata_batch_code}");
        //放入修改前的源字段
        map.put("sourceFieldNames", dto.sourceFieldNames);

        buildReplaceTextProcessorDTO.name = "replaceTextForFtpProcess";
        buildReplaceTextProcessorDTO.details = "query_phase";
        buildReplaceTextProcessorDTO.groupId = groupId;
        buildReplaceTextProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(9);
        //替换流文件
        buildReplaceTextProcessorDTO.evaluationMode = "Entire text";
        buildReplaceTextProcessorDTO.maximumBufferSize = "100 MB";
        //设置替换值为map，也就是map里面装载的连个键值对
        buildReplaceTextProcessorDTO.replacementValue = JSON.toJSONString(map);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildReplaceTextProcess(buildReplaceTextProcessorDTO, new ArrayList<>());
        return processorEntityBusinessResult.data;
    }

    /**
     * 调用api参数组件
     *
     * @param config  数据接入配置
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity replaceTextForVisionProcess(DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto) {
        BuildReplaceTextProcessorDTO buildReplaceTextProcessorDTO = new BuildReplaceTextProcessorDTO();
        HashMap<String, Object> map = new HashMap<>();
        map.put("keyStr", "${kafka.topic}");


        buildReplaceTextProcessorDTO.name = "replaceTextForFtpProcess";
        buildReplaceTextProcessorDTO.details = "query_phase";
        buildReplaceTextProcessorDTO.groupId = groupId;
        buildReplaceTextProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(10);
        //替换流文件
        buildReplaceTextProcessorDTO.evaluationMode = "Entire text";
        buildReplaceTextProcessorDTO.maximumBufferSize = "100 MB";
        buildReplaceTextProcessorDTO.replacementValue = JSON.toJSONString(map);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildReplaceTextProcess(buildReplaceTextProcessorDTO, new ArrayList<>());
        return processorEntityBusinessResult.data;
    }

    /**
     * 调用api参数组件
     *
     * @param config  数据接入配置
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity replaceTextForSapBwProcess(DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto) {
        BuildReplaceTextProcessorDTO buildReplaceTextProcessorDTO = new BuildReplaceTextProcessorDTO();
        HashMap<String, Object> map = new HashMap<>();
        //放入topic
        map.put("topic", "${kafka.topic}");
        //放入大批次号
        map.put("fidata_batch_code", "${fidata_batch_code}");

        buildReplaceTextProcessorDTO.name = "replaceTextForSapBwProcess";
        buildReplaceTextProcessorDTO.details = "query_phase";
        buildReplaceTextProcessorDTO.groupId = groupId;
        buildReplaceTextProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(9);
        //替换流文件
        buildReplaceTextProcessorDTO.evaluationMode = "Entire text";
        buildReplaceTextProcessorDTO.maximumBufferSize = "100 MB";
        //设置替换值为map，也就是map里面装载的连个键值对
        buildReplaceTextProcessorDTO.replacementValue = JSON.toJSONString(map);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildReplaceTextProcess(buildReplaceTextProcessorDTO, new ArrayList<>());
        return processorEntityBusinessResult.data;
    }

    /**
     * 调用api参数组件
     *
     * @param config  数据接入配置
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity replaceTextForPowerBIProcess(DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto) {
        BuildReplaceTextProcessorDTO buildReplaceTextProcessorDTO = new BuildReplaceTextProcessorDTO();
        HashMap<String, Object> map = new HashMap<>();
        //放入topic
        map.put("topic", "${kafka.topic}");
        //放入大批次号
        map.put("fidata_batch_code", "${fidata_batch_code}");

        buildReplaceTextProcessorDTO.name = "replaceTextForPowerBIProcess";
        buildReplaceTextProcessorDTO.details = "query_phase";
        buildReplaceTextProcessorDTO.groupId = groupId;
        buildReplaceTextProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(9);
        //替换流文件
        buildReplaceTextProcessorDTO.evaluationMode = "Entire text";
        buildReplaceTextProcessorDTO.maximumBufferSize = "100 MB";
        //设置替换值为map，也就是map里面装载的连个键值对
        buildReplaceTextProcessorDTO.replacementValue = JSON.toJSONString(map);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildReplaceTextProcess(buildReplaceTextProcessorDTO, new ArrayList<>());
        return processorEntityBusinessResult.data;
    }

    /**
     * 调用api参数组件
     *
     * @param config  数据接入配置
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity replaceTextForApiProcess(DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto) {
        BuildReplaceTextProcessorDTO buildReplaceTextProcessorDTO = new BuildReplaceTextProcessorDTO();
        HashMap<String, Object> map = new HashMap<>();
        //放入topic
        map.put("topic", "${kafka.topic}");
        //放入大批次号
        map.put("fidata_batch_code", "${fidata_batch_code}");

        buildReplaceTextProcessorDTO.name = "replaceTextForApiProcess";
        buildReplaceTextProcessorDTO.details = "query_phase";
        buildReplaceTextProcessorDTO.groupId = groupId;
        buildReplaceTextProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(9);
        //替换流文件
        buildReplaceTextProcessorDTO.evaluationMode = "Entire text";
        buildReplaceTextProcessorDTO.maximumBufferSize = "100 MB";
        //设置替换值为map，也就是map里面装载的连个键值对
        buildReplaceTextProcessorDTO.replacementValue = JSON.toJSONString(map);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildReplaceTextProcess(buildReplaceTextProcessorDTO, new ArrayList<>());
        return processorEntityBusinessResult.data;
    }

    /**
     * 调用api参数组件
     *
     * @param config  数据接入配置
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity replaceTextForDwProcessv1(DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto) {
        BuildReplaceTextProcessorDTO buildReplaceTextProcessorDTO = new BuildReplaceTextProcessorDTO();
        //校验通过修改字段集合
        HashMap<String, Object> updateFieldMap_Y = new HashMap<>();
        updateFieldMap_Y.put("fi_verify_type", "3");
        updateFieldMap_Y.put("fi_sync_type", "2");
        //校验不通过修改字段集合
        HashMap<String, Object> updateFieldMap_N = new HashMap<>();
        updateFieldMap_N.put("fi_sync_type", 3);
        updateFieldMap_N.put("fi_verify_type", 2);
        //校验不通过但校验规则为弱类型规则修改字段集合
        HashMap<String, Object> updateFieldMap_R = new HashMap<>();
        updateFieldMap_R.put("fi_sync_type", 2);
        updateFieldMap_R.put("fi_verify_type", 4);
        //校验依据字段集合
        HashMap<String, Object> checkByFieldMap = new HashMap<>();
//        checkByFieldMap.put("fidata_flow_batch_code", "${fragment.index}");
        checkByFieldMap.put("fidata_batch_code", "${fidata_batch_code}");
//        checkByFieldMap.put("fidata_flow_batch_code", "${input.flowfile.uuid}");
        DataCheckSyncDTO dataCheckSyncDTO = new DataCheckSyncDTO();
        dataCheckSyncDTO.fiDataDataSourceId = "1";
        dataCheckSyncDTO.msgField = "fi_error_message";
        dataCheckSyncDTO.updateFieldMap_Y = updateFieldMap_Y;
        dataCheckSyncDTO.updateFieldMap_N = updateFieldMap_N;
        dataCheckSyncDTO.updateFieldMap_R = updateFieldMap_R;
        dataCheckSyncDTO.checkByFieldMap = checkByFieldMap;
        dataCheckSyncDTO.tablePrefix = "temp_";
        dataCheckSyncDTO.tableUnique = String.valueOf(dto.id);
        String tableName = dto.tableName;
        if (tableName.contains(".")) {
            dataCheckSyncDTO.tableName = tableName.split("\\.")[1];
        } else {
            dataCheckSyncDTO.tableName = tableName;
        }

        dataCheckSyncDTO.tableBusinessType = TableBusinessTypeEnum.NONE;
        dataCheckSyncDTO.uniqueField = dto.pkName;

        buildReplaceTextProcessorDTO.name = "GenerateFlowFileProcessor";
        buildReplaceTextProcessorDTO.details = "query_phase";
        buildReplaceTextProcessorDTO.groupId = groupId;
        buildReplaceTextProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(-2, 10);
        //替换流文件
        buildReplaceTextProcessorDTO.evaluationMode = "Entire text";
        buildReplaceTextProcessorDTO.maximumBufferSize = "100 MB";
        buildReplaceTextProcessorDTO.replacementValue = JSON.toJSONString(dataCheckSyncDTO);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildReplaceTextProcess(buildReplaceTextProcessorDTO, new ArrayList<>());
        return processorEntityBusinessResult.data;
    }

    /**
     * 调用api参数组件
     *
     * @param config  数据接入配置
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity replaceTextForDwProcess(DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO dto) {
        BuildReplaceTextProcessorDTO buildReplaceTextProcessorDTO = new BuildReplaceTextProcessorDTO();
        //校验通过修改字段集合
        HashMap<String, Object> updateFieldMap_Y = new HashMap<>();
        updateFieldMap_Y.put("fi_verify_type", "3");
        updateFieldMap_Y.put("fi_sync_type", "2");
        //校验不通过修改字段集合
        HashMap<String, Object> updateFieldMap_N = new HashMap<>();
        updateFieldMap_N.put("fi_sync_type", 3);
        updateFieldMap_N.put("fi_verify_type", 2);
        //校验不通过但校验规则为弱类型规则修改字段集合
        HashMap<String, Object> updateFieldMap_R = new HashMap<>();
        updateFieldMap_R.put("fi_sync_type", 2);
        updateFieldMap_R.put("fi_verify_type", 4);
        //校验依据字段集合
        HashMap<String, Object> checkByFieldMap = new HashMap<>();
        checkByFieldMap.put("fidata_flow_batch_code", "'${fragment.index}'");
        checkByFieldMap.put("fidata_batch_code", "'${fidata_batch_code}'");
//        checkByFieldMap.put("fidata_flow_batch_code", "'${input.flowfile.uuid}'");
        DataCheckSyncDTO dataCheckSyncDTO = new DataCheckSyncDTO();
        dataCheckSyncDTO.fiDataDataSourceId = "1";
        dataCheckSyncDTO.msgField = "fi_error_message";
        dataCheckSyncDTO.updateFieldMap_Y = updateFieldMap_Y;
        dataCheckSyncDTO.updateFieldMap_N = updateFieldMap_N;
        dataCheckSyncDTO.updateFieldMap_R = updateFieldMap_R;
        dataCheckSyncDTO.checkByFieldMap = checkByFieldMap;
        dataCheckSyncDTO.tablePrefix = "temp_";
        dataCheckSyncDTO.tableUnique = String.valueOf(dto.id);
        String tableName = dto.tableName;
        if (tableName.contains(".")) {
            dataCheckSyncDTO.tableName = tableName.split("\\.")[1];
        } else {
            dataCheckSyncDTO.tableName = tableName;
        }

        dataCheckSyncDTO.tableBusinessType = TableBusinessTypeEnum.NONE;
        dataCheckSyncDTO.uniqueField = dto.pkName;

        buildReplaceTextProcessorDTO.name = "GenerateFlowFileProcessor";
        buildReplaceTextProcessorDTO.details = "query_phase";
        buildReplaceTextProcessorDTO.groupId = groupId;
        buildReplaceTextProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(-2, 12);
        //替换流文件
        buildReplaceTextProcessorDTO.evaluationMode = "Entire text";
        buildReplaceTextProcessorDTO.maximumBufferSize = "100 MB";
        buildReplaceTextProcessorDTO.replacementValue = JSON.toJSONString(dataCheckSyncDTO);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildReplaceTextProcess(buildReplaceTextProcessorDTO, new ArrayList<>());
        return processorEntityBusinessResult.data;
    }

    /**
     * 调用api参数组件
     *
     * @param config  数据接入配置
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity generateFlowFileProcessor(DataAccessConfigDTO config, String groupId) {
        HashMap<String, Object> updateFieldMap_Y = new HashMap<>();
        updateFieldMap_Y.put("fi_verify_type", "3");
        updateFieldMap_Y.put("fi_sync_type", "2");
        HashMap<String, Object> updateFieldMap_N = new HashMap<>();
        updateFieldMap_N.put("fi_sync_type", 3);
        updateFieldMap_N.put("fi_verify_type", 2);
        HashMap<String, Object> updateFieldMap_R = new HashMap<>();
        updateFieldMap_R.put("fi_sync_type", 2);
        updateFieldMap_R.put("fi_verify_type", 4);
        HashMap<String, Object> checkByFieldMap = new HashMap<>();
        checkByFieldMap.put("fidata_flow_batch_code", "'${input.flowfile.uuid}'");
        DataCheckSyncDTO dataCheckSyncDTO = new DataCheckSyncDTO();
//        dataCheckSyncDTO.dataSourceId = null;
        dataCheckSyncDTO.msgField = "fi_error_message";
        dataCheckSyncDTO.updateFieldMap_Y = updateFieldMap_Y;
        dataCheckSyncDTO.updateFieldMap_N = updateFieldMap_N;
        dataCheckSyncDTO.updateFieldMap_R = updateFieldMap_R;
        dataCheckSyncDTO.checkByFieldMap = checkByFieldMap;
        BuildGenerateFlowFileProcessorDTO buildGenerateFlowFileProcessorDTO = new BuildGenerateFlowFileProcessorDTO();
        buildGenerateFlowFileProcessorDTO.name = "GenerateFlowFileProcessor";
        buildGenerateFlowFileProcessorDTO.details = "query_phase";
        buildGenerateFlowFileProcessorDTO.groupId = groupId;
        buildGenerateFlowFileProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(8);
        buildGenerateFlowFileProcessorDTO.generateCustomText = JSON.toJSONString(dataCheckSyncDTO);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildGenerateFlowFileProcessor(buildGenerateFlowFileProcessorDTO, new ArrayList<>());
        return processorEntityBusinessResult.data;
    }

    /**
     * 调用api参数组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity invokeHTTPProcessorv1(String groupId) {
        BuildInvokeHttpProcessorDTO buildInvokeHttpProcessorDTO = new BuildInvokeHttpProcessorDTO();
        buildInvokeHttpProcessorDTO.name = "invokeHTTPProcessor";
        buildInvokeHttpProcessorDTO.details = "query_phase";
        buildInvokeHttpProcessorDTO.groupId = groupId;
        buildInvokeHttpProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(-2, 11);
        buildInvokeHttpProcessorDTO.attributesToSend = "(?s)(^.*$)";
        buildInvokeHttpProcessorDTO.contentType = "application/json;charset=UTF-8";
        buildInvokeHttpProcessorDTO.httpMethod = "POST";
        buildInvokeHttpProcessorDTO.remoteUrl = dataGovernanceUrl + "/datagovernance/datacheck/syncCheckData?Content-Type=application/json";
        buildInvokeHttpProcessorDTO.nifiToken = nifiToken;
        buildInvokeHttpProcessorDTO.socketConnectTimeout = "300 secs";
        buildInvokeHttpProcessorDTO.socketReadTimeout = "300 secs";
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildInvokeHTTPProcessor(buildInvokeHttpProcessorDTO, new ArrayList<>());
        return processorEntityBusinessResult.data;
    }

    /**
     * 调用api参数组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity invokeHTTPProcessor(String groupId) {
        BuildInvokeHttpProcessorDTO buildInvokeHttpProcessorDTO = new BuildInvokeHttpProcessorDTO();
        buildInvokeHttpProcessorDTO.name = "invokeHTTPProcessor";
        buildInvokeHttpProcessorDTO.details = "query_phase";
        buildInvokeHttpProcessorDTO.groupId = groupId;
        buildInvokeHttpProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(-2, 13);
        buildInvokeHttpProcessorDTO.attributesToSend = "(?s)(^.*$)";
        buildInvokeHttpProcessorDTO.contentType = "application/json;charset=UTF-8";
        buildInvokeHttpProcessorDTO.httpMethod = "POST";
        buildInvokeHttpProcessorDTO.remoteUrl = dataGovernanceUrl + "/datagovernance/datacheck/syncCheckData?Content-Type=application/json";
        buildInvokeHttpProcessorDTO.nifiToken = nifiToken;
        buildInvokeHttpProcessorDTO.socketConnectTimeout = "300 secs";
        buildInvokeHttpProcessorDTO.socketReadTimeout = "300 secs";
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildInvokeHTTPProcessor(buildInvokeHttpProcessorDTO, new ArrayList<>());
        return processorEntityBusinessResult.data;
    }

    /**
     * ftp调用api参数组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity invokeHTTPForFtpProcessor(String groupId) {
        BuildInvokeHttpProcessorDTO buildInvokeHttpProcessorDTO = new BuildInvokeHttpProcessorDTO();
        buildInvokeHttpProcessorDTO.name = "invokeHTTPProcessor";
        buildInvokeHttpProcessorDTO.details = "query_phase";
        buildInvokeHttpProcessorDTO.groupId = groupId;
        buildInvokeHttpProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(-2, 9);
        buildInvokeHttpProcessorDTO.attributesToSend = "(?s)(^.*$)";
        buildInvokeHttpProcessorDTO.contentType = "application/json;charset=UTF-8";
        buildInvokeHttpProcessorDTO.httpMethod = "POST";
        buildInvokeHttpProcessorDTO.remoteUrl = dataGovernanceUrl + "/dataAccess/ftp/copyFtpFile?Content-Type=application/json";
        buildInvokeHttpProcessorDTO.nifiToken = nifiToken;
        List<String> autoEnd = new ArrayList<>();
        autoEnd.add("Response");
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildInvokeHTTPProcessor(buildInvokeHttpProcessorDTO, autoEnd);
        return processorEntityBusinessResult.data;
    }

    /**
     * ftp调用api参数组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity invokeHTTPForFtpDataUploadProcessor(String groupId) {
        BuildInvokeHttpProcessorDTO buildInvokeHttpProcessorDTO = new BuildInvokeHttpProcessorDTO();
        buildInvokeHttpProcessorDTO.name = "invokeHTTPProcessor";
        buildInvokeHttpProcessorDTO.details = "query_phase";
        buildInvokeHttpProcessorDTO.groupId = groupId;
        buildInvokeHttpProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(10);
        buildInvokeHttpProcessorDTO.attributesToSend = "(?s)(^.*$)";
        buildInvokeHttpProcessorDTO.contentType = "application/json;charset=UTF-8";
        buildInvokeHttpProcessorDTO.httpMethod = "POST";
        buildInvokeHttpProcessorDTO.remoteUrl = dataGovernanceUrl + "/task/nifi/sftpDataUploadListener?Content-Type=application/json";
        buildInvokeHttpProcessorDTO.nifiToken = nifiToken;
        buildInvokeHttpProcessorDTO.socketConnectTimeout = "300 secs";
        buildInvokeHttpProcessorDTO.socketReadTimeout = "300 secs";
        List<String> autoEnd = new ArrayList<>();
        autoEnd.add("Response");
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildInvokeHTTPProcessor(buildInvokeHttpProcessorDTO, autoEnd);
        return processorEntityBusinessResult.data;
    }

    /**
     * ftp调用api参数组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity invokeHTTPForSapBwProcessor(String groupId) {
        BuildInvokeHttpProcessorDTO buildInvokeHttpProcessorDTO = new BuildInvokeHttpProcessorDTO();
        buildInvokeHttpProcessorDTO.name = "invokeHTTPProcessorForSapBw";
        buildInvokeHttpProcessorDTO.details = "query_phase";
        buildInvokeHttpProcessorDTO.groupId = groupId;
        buildInvokeHttpProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(10);
        buildInvokeHttpProcessorDTO.attributesToSend = "(?s)(^.*$)";
        buildInvokeHttpProcessorDTO.contentType = "application/json;charset=UTF-8";
        buildInvokeHttpProcessorDTO.httpMethod = "POST";
        buildInvokeHttpProcessorDTO.remoteUrl = dataGovernanceUrl + "/task/nifi/sapBwToStg?Content-Type=application/json";
        buildInvokeHttpProcessorDTO.nifiToken = nifiToken;
        buildInvokeHttpProcessorDTO.socketConnectTimeout = "300 secs";
        buildInvokeHttpProcessorDTO.socketReadTimeout = "300 secs";
        List<String> autoEnd = new ArrayList<>();
        autoEnd.add("Response");
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildInvokeHTTPProcessor(buildInvokeHttpProcessorDTO, autoEnd);
        return processorEntityBusinessResult.data;
    }

    /**
     * ftp调用api参数组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity invokeHTTPForPowerBIProcessor(String groupId) {
        BuildInvokeHttpProcessorDTO buildInvokeHttpProcessorDTO = new BuildInvokeHttpProcessorDTO();
        buildInvokeHttpProcessorDTO.name = "invokeHTTPForPowerBIProcessor";
        buildInvokeHttpProcessorDTO.details = "query_phase";
        buildInvokeHttpProcessorDTO.groupId = groupId;
        buildInvokeHttpProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(10);
        buildInvokeHttpProcessorDTO.attributesToSend = "(?s)(^.*$)";
        buildInvokeHttpProcessorDTO.contentType = "application/json;charset=UTF-8";
        buildInvokeHttpProcessorDTO.httpMethod = "POST";
        buildInvokeHttpProcessorDTO.remoteUrl = dataGovernanceUrl + "/task/nifi/powerBiToStg?Content-Type=application/json";
        buildInvokeHttpProcessorDTO.nifiToken = nifiToken;
        buildInvokeHttpProcessorDTO.socketConnectTimeout = "300 secs";
        buildInvokeHttpProcessorDTO.socketReadTimeout = "300 secs";
        List<String> autoEnd = new ArrayList<>();
        autoEnd.add("Response");
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildInvokeHTTPProcessor(buildInvokeHttpProcessorDTO, autoEnd);
        return processorEntityBusinessResult.data;
    }

    /**
     * ftp调用api参数组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity invokeHTTPForApiProcessor(String groupId) {
        BuildInvokeHttpProcessorDTO buildInvokeHttpProcessorDTO = new BuildInvokeHttpProcessorDTO();
        buildInvokeHttpProcessorDTO.name = "invokeHTTPProcessorForApi";
        buildInvokeHttpProcessorDTO.details = "query_phase";
        buildInvokeHttpProcessorDTO.groupId = groupId;
        buildInvokeHttpProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(10);
        buildInvokeHttpProcessorDTO.attributesToSend = "(?s)(^.*$)";
        buildInvokeHttpProcessorDTO.contentType = "application/json;charset=UTF-8";
        buildInvokeHttpProcessorDTO.httpMethod = "POST";
        buildInvokeHttpProcessorDTO.remoteUrl = dataGovernanceUrl + "/task/nifi/apiToStg?Content-Type=application/json";
        buildInvokeHttpProcessorDTO.nifiToken = nifiToken;
        buildInvokeHttpProcessorDTO.socketConnectTimeout = "300 secs";
        buildInvokeHttpProcessorDTO.socketReadTimeout = "300 secs";
        List<String> autoEnd = new ArrayList<>();
        autoEnd.add("Response");
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildInvokeHTTPProcessor(buildInvokeHttpProcessorDTO, autoEnd);
        return processorEntityBusinessResult.data;
    }

    /**
     * ftp调用api参数组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity invokeHTTPForVisionProcessor(String groupId) {
        BuildInvokeHttpProcessorDTO buildInvokeHttpProcessorDTO = new BuildInvokeHttpProcessorDTO();
        buildInvokeHttpProcessorDTO.name = "invokeHTTPProcessor";
        buildInvokeHttpProcessorDTO.details = "query_phase";
        buildInvokeHttpProcessorDTO.groupId = groupId;
        buildInvokeHttpProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(10);
        buildInvokeHttpProcessorDTO.attributesToSend = "(?s)(^.*$)";
        buildInvokeHttpProcessorDTO.contentType = "application/json;charset=UTF-8";
        buildInvokeHttpProcessorDTO.httpMethod = "POST";
        buildInvokeHttpProcessorDTO.remoteUrl = dataGovernanceUrl + "/dataAccess/tableFields/delTableVersion?Content-Type=application/json";
        buildInvokeHttpProcessorDTO.nifiToken = nifiToken;
        List<String> autoEnd = new ArrayList<>();
        autoEnd.add("Response");
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildInvokeHTTPProcessor(buildInvokeHttpProcessorDTO, autoEnd);
        return processorEntityBusinessResult.data;
    }

    /**
     * 创建变量组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity evaluateJson(String groupId, int position) {
        ArrayList<String> strings = new ArrayList<>();
        //strings.add(NifiConstants.AttrConstants.INCREMENT_START);
        strings.add(NifiConstants.AttrConstants.START_TIME);
        strings.add(NifiConstants.AttrConstants.FIDATA_BATCH_CODE);
        strings.add(NifiConstants.AttrConstants.PIPEL_TRACE_ID);
        strings.add(NifiConstants.AttrConstants.PIPEL_JOB_TRACE_ID);
        strings.add(NifiConstants.AttrConstants.PIPEL_TASK_TRACE_ID);
        strings.add(NifiConstants.AttrConstants.PIPEL_STAGE_TRACE_ID);
        strings.add(NifiConstants.AttrConstants.TOPIC_TYPE);
        strings.add(NifiConstants.AttrConstants.SOURCESYS);
        //strings.add(NifiConstants.AttrConstants.START_TIME);
        BuildProcessEvaluateJsonPathDTO dto = new BuildProcessEvaluateJsonPathDTO();
        dto.name = "Set Increment Field";
        dto.details = "query_phase";
        dto.groupId = groupId;
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(position);
        dto.selfDefinedParameter = strings;
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildEvaluateJsonPathProcess(dto);
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    /**
     * 创建变量组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity evaluateJsonPathProcessor(String groupId, int position) {
        ArrayList<String> strings = new ArrayList<>();
        //strings.add(NifiConstants.AttrConstants.INCREMENT_START);
        strings.add(NifiConstants.AttrConstants.START_TIME);
        strings.add(NifiConstants.AttrConstants.FIDATA_BATCH_CODE);
        strings.add(NifiConstants.AttrConstants.PIPEL_TRACE_ID);
        strings.add(NifiConstants.AttrConstants.PIPEL_JOB_TRACE_ID);
        strings.add(NifiConstants.AttrConstants.PIPEL_TASK_TRACE_ID);
        strings.add(NifiConstants.AttrConstants.PIPEL_STAGE_TRACE_ID);
        strings.add(NifiConstants.AttrConstants.TOPIC_TYPE);
        //strings.add(NifiConstants.AttrConstants.START_TIME);
        BuildProcessEvaluateJsonPathDTO dto = new BuildProcessEvaluateJsonPathDTO();
        dto.name = "Set Increment Field";
        dto.details = "query_phase";
        dto.groupId = groupId;
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(position);
        dto.selfDefinedParameter = strings;
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildEvaluateJsonPathProcess(dto);
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    /**
     * 创建变量组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity evaluateTimeVariablesProcessor(String groupId, List<String> strings) {

        BuildProcessEvaluateJsonPathDTO dto = new BuildProcessEvaluateJsonPathDTO();
        dto.name = "Set Increment Field";
        dto.details = "query_phase";
        dto.groupId = groupId;
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(6);
        dto.selfDefinedParameter = strings;
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildEvaluateJsonPathProcess(dto);
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    /**
     * 执行sql 查询增量字段组件
     *
     * @param groupId
     * @param targetControllerServiceId
     * @param nifiCustomWorkflowDetailDTO
     * @return
     */
    private ProcessorEntity queryIncrementFieldProcessor(String groupId, String targetControllerServiceId, NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Query Increment Field";
        querySqlDto.details = "query_phase";
        querySqlDto.groupId = groupId;
        querySqlDto.querySql = nifiCustomWorkflowDetailDTO.customScript;
        querySqlDto.dbConnectionId = targetControllerServiceId;
        /*querySqlDto.scheduleExpression = config.processorConfig.scheduleExpression;
        querySqlDto.scheduleType = config.processorConfig.scheduleType;*/
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(3);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<String>());
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    /**
     * createConsumeKafkaProcessor
     *
     * @param configDTO 数据接入配置
     * @param groupId   组id
     * @return 组件对象
     */
    private ProcessorEntity createConsumeKafkaProcessor(DataAccessConfigDTO configDTO, BuildNifiFlowDTO dto, String groupId, int postion) {
        BuildConsumeKafkaProcessorDTO buildConsumeKafkaProcessorDTO = new BuildConsumeKafkaProcessorDTO();
        buildConsumeKafkaProcessorDTO.name = "ConsumeKafka";
        buildConsumeKafkaProcessorDTO.details = "query_phase";
        buildConsumeKafkaProcessorDTO.groupId = groupId;
        //管道id
        buildConsumeKafkaProcessorDTO.GroupId = "dmp.nifi.datafactory.pipeline";
        buildConsumeKafkaProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(postion);
        Map<String, String> variable = new HashMap<>();
        variable.put(ComponentIdTypeEnum.KAFKA_BROKERS.getName(), KafkaBrokers);
        componentsBuild.buildNifiGlobalVariable(variable);
        buildConsumeKafkaProcessorDTO.kafkaBrokers = "${" + ComponentIdTypeEnum.KAFKA_BROKERS.getName() + "}";
        buildConsumeKafkaProcessorDTO.honorTransactions = false;
        TableTopicDTO tableTopicDTO = new TableTopicDTO();
        tableTopicDTO.topicType = TopicTypeEnum.NO_TYPE.getValue();
        tableTopicDTO.tableId = Math.toIntExact(dto.id);
        tableTopicDTO.tableType = dto.type.getValue();
        List<TableTopicDTO> tableTopicList = tableTopicService.getTableTopicList(tableTopicDTO);
        buildConsumeKafkaProcessorDTO.topicNames = tableTopicList.stream().map(e -> e.topicName).collect(Collectors.joining(" , "));
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildConsumeKafkaProcessor(buildConsumeKafkaProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    /**
     * createPublishKafkaProcessor
     *
     * @param configDTO 数据接入配置
     * @param groupId   组id
     * @param dto
     * @return 组件对象
     */
    public ProcessorEntity createPublishKafkaProcessor(DataAccessConfigDTO configDTO, BuildNifiFlowDTO dto, String groupId, int position, boolean createProcessor) {
        BuildPublishKafkaProcessorDTO buildPublishKafkaProcessorDTO = new BuildPublishKafkaProcessorDTO();
        Map<String, String> variable = new HashMap<>();
        variable.put(ComponentIdTypeEnum.KAFKA_BROKERS.getName(), KafkaBrokers);
        componentsBuild.buildNifiGlobalVariable(variable);
        buildPublishKafkaProcessorDTO.KafkaBrokers = "${" + ComponentIdTypeEnum.KAFKA_BROKERS.getName() + "}";
        buildPublishKafkaProcessorDTO.KafkaKey = "${uuid}";
        buildPublishKafkaProcessorDTO.groupId = groupId;
        buildPublishKafkaProcessorDTO.name = "PublishKafka";
        buildPublishKafkaProcessorDTO.details = "PublishKafka";
        buildPublishKafkaProcessorDTO.UseTransactions = "false";
        buildPublishKafkaProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(position);
        String targetTableName = configDTO.processorConfig.targetTableName;

        //更新topic
        TableTopicDTO tableTopicDTO = new TableTopicDTO();
        tableTopicDTO.tableId = Math.toIntExact(dto.id);
        tableTopicDTO.tableType = dto.type.getValue();
        tableTopicDTO.topicName = MqConstants.TopicPrefix.TOPIC_PREFIX + dto.type.getValue() + "." + dto.appId + "." + dto.id;
        tableTopicDTO.topicType = TopicTypeEnum.DAILY_NIFI_FLOW.getValue();
        if (Objects.equals(dto.type, OlapTableEnum.KPI)) {
            tableTopicDTO.topicName = MqConstants.TopicPrefix.TOPIC_PREFIX + OlapTableEnum.KPI.getValue() + "." + dto.appId + "." + dto.id;
        }
        tableTopicService.updateTableTopic(tableTopicDTO);
        buildPublishKafkaProcessorDTO.TopicName = MqConstants.QueueConstants.BUILD_TASK_PUBLISH_FLOW;
        if (createProcessor) {
            BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildPublishKafkaProcessor(buildPublishKafkaProcessorDTO);
            verifyProcessorResult(processorEntityBusinessResult);
            return processorEntityBusinessResult.data;
        }
        return null;

    }


    /**
     * createPublishKafkaForPipelineProcessor
     *
     * @param groupId
     * @param x
     * @param y
     * @return
     */
    public ProcessorEntity createPublishKafkaForPipelineProcessor(String groupId, int x, int y) {
        BuildPublishKafkaProcessorDTO buildPublishKafkaProcessorDTO = new BuildPublishKafkaProcessorDTO();
        Map<String, String> variable = new HashMap<>();
        variable.put(ComponentIdTypeEnum.KAFKA_BROKERS.getName(), KafkaBrokers);
        componentsBuild.buildNifiGlobalVariable(variable);
        buildPublishKafkaProcessorDTO.KafkaBrokers = "${" + ComponentIdTypeEnum.KAFKA_BROKERS.getName() + "}";
        buildPublishKafkaProcessorDTO.KafkaKey = "${uuid}";
        buildPublishKafkaProcessorDTO.groupId = groupId;
        buildPublishKafkaProcessorDTO.name = "PublishKafka";
        buildPublishKafkaProcessorDTO.details = "insert_phase";
        buildPublishKafkaProcessorDTO.UseTransactions = "false";
        buildPublishKafkaProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(x, y);
        buildPublishKafkaProcessorDTO.TopicName = MqConstants.QueueConstants.BUILD_EXEC_SCRIPT_FLOW_NEXT;
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildPublishKafkaProcessor(buildPublishKafkaProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    /**
     * script
     *
     * @param configDTO
     * @param dto
     * @param groupId
     * @param x
     * @param y
     * @return
     */
    public ProcessorEntity createPublishKafkaForPipelineProcessor(DataAccessConfigDTO configDTO, BuildNifiFlowDTO dto, String groupId, int x, int y) {
        BuildPublishKafkaProcessorDTO buildPublishKafkaProcessorDTO = new BuildPublishKafkaProcessorDTO();
        Map<String, String> variable = new HashMap<>();
        variable.put(ComponentIdTypeEnum.KAFKA_BROKERS.getName(), KafkaBrokers);
        componentsBuild.buildNifiGlobalVariable(variable);
        buildPublishKafkaProcessorDTO.KafkaBrokers = "${" + ComponentIdTypeEnum.KAFKA_BROKERS.getName() + "}";
        buildPublishKafkaProcessorDTO.KafkaKey = "${uuid}";
        buildPublishKafkaProcessorDTO.groupId = groupId;
        buildPublishKafkaProcessorDTO.name = "PublishKafka";
        buildPublishKafkaProcessorDTO.details = "insert_phase";
        buildPublishKafkaProcessorDTO.UseTransactions = "false";
        buildPublishKafkaProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(x, y);
        buildPublishKafkaProcessorDTO.TopicName = pipelineTopicName;
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildPublishKafkaProcessor(buildPublishKafkaProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    /**
     * createPublishKafkaForSupervisionProcessor
     *
     * @param groupId  组id
     * @param position
     * @return 组件对象
     */
    public ProcessorEntity createPublishKafkaForSupervisionProcessor(String groupId, int position) {
        BuildPublishKafkaProcessorDTO buildPublishKafkaProcessorDTO = new BuildPublishKafkaProcessorDTO();
        Map<String, String> variable = new HashMap<>();
        variable.put(ComponentIdTypeEnum.KAFKA_BROKERS.getName(), KafkaBrokers);
        componentsBuild.buildNifiGlobalVariable(variable);
        buildPublishKafkaProcessorDTO.KafkaBrokers = "${" + ComponentIdTypeEnum.KAFKA_BROKERS.getName() + "}";
        buildPublishKafkaProcessorDTO.KafkaKey = "${uuid}";
        buildPublishKafkaProcessorDTO.groupId = groupId;
        buildPublishKafkaProcessorDTO.name = "PublishKafka";
        buildPublishKafkaProcessorDTO.details = "PublishKafka";
        buildPublishKafkaProcessorDTO.UseTransactions = "false";
        buildPublishKafkaProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(3, position);
        buildPublishKafkaProcessorDTO.TopicName = "pipeline.supervision";
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildPublishKafkaProcessor(buildPublishKafkaProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    /**
     * 执行sql 查询增量字段组件
     *
     * @param config      数据接入配置
     * @param groupId     组id
     * @param cfgDbPoolId 增量配置库连接池id
     * @return 组件对象
     */
    private ProcessorEntity queryDispatchProcessor(DataAccessConfigDTO config, String groupId, String cfgDbPoolId, BuildNifiFlowDTO dto) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "queryDispatchProcessor";
        querySqlDto.details = "queryDispatchProcessor";
        querySqlDto.groupId = groupId;
        querySqlDto.querySql = buildTableServiceSql(config.processorConfig.targetTableName, dto);
        querySqlDto.dbConnectionId = cfgDbPoolId;
        querySqlDto.scheduleExpression = config.processorConfig.scheduleExpression;
        querySqlDto.scheduleType = config.processorConfig.scheduleType;
/*        querySqlDto.scheduleExpression = "1800";
        querySqlDto.scheduleType = SchedulingStrategyTypeEnum.TIMER;*/
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(1);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<String>());
        //验证执行结果
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;

    }

    /**
     * 执行sql 查询增量字段组件 api用
     *
     * @param config      数据接入配置
     * @param groupId     组id
     * @param cfgDbPoolId 增量配置库连接池id
     * @return 组件对象
     */
    private ProcessorEntity queryDispatchApiProcessor(DataAccessConfigDTO config, String groupId, String cfgDbPoolId, BuildNifiFlowDTO dto) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "queryDispatchProcessor";
        querySqlDto.details = "queryDispatchProcessor";
        querySqlDto.groupId = groupId;
        querySqlDto.querySql = buildTableApiServiceSql(dto);
        querySqlDto.dbConnectionId = cfgDbPoolId;
        querySqlDto.scheduleExpression = config.processorConfig.scheduleExpression;
        querySqlDto.scheduleType = config.processorConfig.scheduleType;
/*        querySqlDto.scheduleExpression = "1800";
        querySqlDto.scheduleType = SchedulingStrategyTypeEnum.TIMER;*/
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(1);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<String>());
        //验证执行结果
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;

    }

    private ProcessorEntity queryForPipelineSupervision(String groupId, String cfgDbPoolId) {
        BuildReplaceTextProcessorDTO buildReplaceTextProcessorDTO = new BuildReplaceTextProcessorDTO();
        buildReplaceTextProcessorDTO.name = "queryForPipelineSupervision";
        buildReplaceTextProcessorDTO.details = "queryForPipelineSupervision";
        buildReplaceTextProcessorDTO.groupId = groupId;
        buildReplaceTextProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(1, 6);
        //替换流文件
        buildReplaceTextProcessorDTO.evaluationMode = "Entire text";
        NifiMessageDTO nifiMessage = new NifiMessageDTO();
        nifiMessage.message = "${executesql.error.message:escapeJson()}";
        nifiMessage.topic = "${kafka.topic}";
        nifiMessage.groupId = groupId;
        nifiMessage.startTime = "${start_time}";
        nifiMessage.endTime = "${end_time}";
        nifiMessage.counts = "${numbers}";
        nifiMessage.pipelStageTraceId = "${pipelStageTraceId}";
        nifiMessage.pipelTaskTraceId = "${pipelTaskTraceId}";
        nifiMessage.pipelJobTraceId = "${pipelJobTraceId}";
        nifiMessage.pipelTraceId = "${pipelTraceId}";
        nifiMessage.entryDate = "${entryDate:format('YYYY-MM-dd HH:mm:ss')}";
        buildReplaceTextProcessorDTO.replacementValue = JSON.toJSONString(nifiMessage);
        buildReplaceTextProcessorDTO.maximumBufferSize = "100 MB";
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildReplaceTextProcess(buildReplaceTextProcessorDTO, new ArrayList<>());
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    private ProcessorEntity specificSymbolProcessor(String groupId, String cfgDbPoolId) {
        BuildReplaceTextProcessorDTO buildReplaceTextProcessorDTO = new BuildReplaceTextProcessorDTO();
        buildReplaceTextProcessorDTO.name = "specificSymbol";
        buildReplaceTextProcessorDTO.details = "specificSymbol";
        buildReplaceTextProcessorDTO.groupId = groupId;
        buildReplaceTextProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(2, 6);
        //替换流文件
        buildReplaceTextProcessorDTO.evaluationMode = "Entire text";
        buildReplaceTextProcessorDTO.replacementValue = "$1";
        buildReplaceTextProcessorDTO.maximumBufferSize = "100 MB";
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildReplaceTextProcess(buildReplaceTextProcessorDTO, new ArrayList<>());
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
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
    public void enabledProcessor(String groupId, List<ProcessorEntity> processors) {
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
    private void writeBackComponentId(long appId, String appComponentId, long tableId, String tableComponentId, String sourceDbPoolComponentId, String targetDbPoolComponentId, String cfgDbPoolComponentId, String schedulerComponentId) {
        NifiAccessDTO dto = new NifiAccessDTO();
        dto.appId = appId;
        dto.appGroupId = appComponentId;
        dto.tableId = tableId;
        dto.tableGroupId = tableComponentId;
        dto.targetDbPoolComponentId = targetDbPoolComponentId;
        dto.sourceDbPoolComponentId = sourceDbPoolComponentId;
        dto.cfgDbPoolComponentId = cfgDbPoolComponentId;
        dto.schedulerComponentId = schedulerComponentId;
//        client.addComponentId(dto);
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
        //str.append(NifiConstants.AttrConstants.INCREMENT_DB_FIELD_START).append(" as ").append(NifiConstants.AttrConstants.INCREMENT_START).append(", ");
        str.append(" ifnull(incremental_objectivescore_start,'0001-01-01') ").append(" as ").append(SystemVariableTypeEnum.START_TIME.getName()).append(" ,");
        str.append(" ifnull(incremental_objectivescore_end,CONCAT(current_timestamp(),'')) ").append(" as ").append(SystemVariableTypeEnum.END_TIME.getName()).append(" ");
        str.append(" from ").append(NifiConstants.AttrConstants.INCREMENT_DB_TABLE_NAME);
        str.append(" where object_name = '").append(targetDbName).append("'");
        return str.toString();
    }

    /**
     * 创建增量字段查询sql
     *
     * @param dto 目标表名称
     * @return sql
     */
    private String buildTableApiServiceSql(BuildNifiFlowDTO dto) {
        StringBuilder str = new StringBuilder();
        str.append("select ");
        KafkaReceiveDTO kafkaRkeceiveDTO = KafkaReceiveDTO.builder().build();
        kafkaRkeceiveDTO.topic = MqConstants.TopicPrefix.TOPIC_PREFIX + dto.type.getValue() + "." + dto.appId + "." + dto.id;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        kafkaRkeceiveDTO.start_time = simpleDateFormat.format(new Date());
        kafkaRkeceiveDTO.pipelTaskTraceId = UUID.randomUUID().toString();
        kafkaRkeceiveDTO.fidata_batch_code = kafkaRkeceiveDTO.pipelTaskTraceId;
        kafkaRkeceiveDTO.pipelStageTraceId = UUID.randomUUID().toString();
        kafkaRkeceiveDTO.ifTaskStart = true;
        kafkaRkeceiveDTO.topicType = TopicTypeEnum.DAILY_NIFI_FLOW.getValue();
        str.append("'").append(kafkaRkeceiveDTO.topic).append("' as topic, '").append(kafkaRkeceiveDTO.start_time).append("' as start_time, ").append("md5(UUID())");
        str.append(" as pipelTaskTraceId, ").append("\"\"").append(" as fidata_batch_code , ").append("md5(UUID())").append(" as pipelStageTraceId, '");
        str.append("true' as ifTaskStart , '").append(kafkaRkeceiveDTO.topicType).append("' as topicType");
        return str.toString();
    }

    /**
     * 创建增量字段查询sql
     *
     * @param targetDbName 目标表名称
     * @return sql
     */
    private String buildTableServiceSql(String targetDbName, BuildNifiFlowDTO dto) {
        StringBuilder str = new StringBuilder();
        str.append("select ");
        //str.append(NifiConstants.AttrConstants.INCREMENT_DB_FIELD_START).append(" as ").append(NifiConstants.AttrConstants.INCREMENT_START).append(", ");
        /*str.append(" ifnull(incremental_objectivescore_start,'0001-01-01') ").append(" as ").append(SystemVariableTypeEnum.START_TIME.getName()).append(" ,");
        str.append(" ifnull(incremental_objectivescore_end,CONCAT(current_timestamp(),'')) ").append(" as ").append(SystemVariableTypeEnum.END_TIME.getName()).append(" ");
        str.append(" from ").append(NifiConstants.AttrConstants.INCREMENT_DB_TABLE_NAME);
        str.append(" where object_name = '").append(targetDbName).append("'");*/
        KafkaReceiveDTO kafkaRkeceiveDTO = KafkaReceiveDTO.builder().build();
        kafkaRkeceiveDTO.topic = MqConstants.TopicPrefix.TOPIC_PREFIX + dto.type.getValue() + "." + dto.appId + "." + dto.id;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        kafkaRkeceiveDTO.start_time = simpleDateFormat.format(new Date());
        kafkaRkeceiveDTO.pipelTaskTraceId = UUID.randomUUID().toString();
        kafkaRkeceiveDTO.fidata_batch_code = kafkaRkeceiveDTO.pipelTaskTraceId;
        kafkaRkeceiveDTO.pipelStageTraceId = UUID.randomUUID().toString();
        kafkaRkeceiveDTO.ifTaskStart = true;
        kafkaRkeceiveDTO.topicType = TopicTypeEnum.DAILY_NIFI_FLOW.getValue();
        str.append("'").append(kafkaRkeceiveDTO.topic).append("' as topic, '").append(kafkaRkeceiveDTO.start_time).append("' as start_time, ").append("md5(UUID())");
        str.append(" as pipelTaskTraceId, ").append("md5(UUID())").append(" as fidata_batch_code , ").append("md5(UUID())").append(" as pipelStageTraceId, '");
        str.append("true' as ifTaskStart , '").append(kafkaRkeceiveDTO.topicType).append("' as topicType");
        return str.toString();
    }

    /**
     * 创建日志sql
     *
     * @return sql
     */
    private String buildLogSql(BuildNifiFlowDTO dto, String selectSql) {
        String filedValues = "";
        // filedValues += dto.queryStartTime == null ? ",'0000-01-01 00:00:00'" : (",'" + dto.queryStartTime + "'");
        //filedValues += dto.queryEndTime == null ? ",now()" : (",'" + dto.queryEndTime + "'");
        filedValues += ",'${incremental_objectivescore_start}','${incremental_objectivescore_end}'";
        //先不存储查询sql
//        dto.selectSql = "";
//        if (dto.selectSql != null && dto.selectSql != "") {
//            filedValues += ",\"" + dto.selectSql.replaceAll("\"", "\\\\\"") + "\"";
//        } else {
//            filedValues += ",\"" + selectSql.replaceAll("\"", "\\\\\"") + "\"";
//        }

        return "INSERT INTO tb_etl_log ( tablename, startdate, `status`,query_start_time,query_end_time,code) " +
                "VALUES ('" + dto.tableName + "', '${" + NifiConstants.AttrConstants.START_TIME + "}', 1" + filedValues + ",'${pipelTraceId:isEmpty():ifElse(${pipelTaskTraceId},${pipelTraceId})}')";
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
            BuildDbControllerServiceDTO targetDto = buildDbControllerServiceDTO(config, groupId, DbPoolTypeEnum.CONFIG, null);
            BusinessResult<ControllerServiceEntity> targetRes = componentsBuild.buildDbControllerService(targetDto);
            if (targetRes.success) {
                return targetRes.data;
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, targetRes.msg);
            }
        }
    }

    /**
     * 创建input_port/output_port组件
     *
     * @param portName    组件名称
     * @param componentId 上级id
     * @param componentX  坐标
     * @param componentY  坐标
     * @param typeEnum    组件类型
     * @return 生成的组件id
     */
    private String buildPortComponent(String portName, String componentId, Double componentX, Double componentY, PortComponentEnum typeEnum) {

        BuildPortDTO buildPortDTO = new BuildPortDTO();
        PortEntity portEntity;

        switch (typeEnum.getValue()) {
            // 创建input_port(应用)
            case 0:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_APP_SUFFIX;
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX;
                buildPortDTO.componentY = componentY + NifiConstants.PortConstants.INPUT_PORT_Y / 2 + NifiConstants.PortConstants.INPUT_PORT_OFFSET_Y;
                portEntity = componentsBuild.buildInputPort(buildPortDTO);
                return portEntity.getId();
            // 创建output_port(应用)
            case 1:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_APP_SUFFIX;
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX;
                buildPortDTO.componentY = componentY + NifiConstants.PortConstants.OUTPUT_PORT_Y;
                portEntity = componentsBuild.buildOutputPort(buildPortDTO);
                return portEntity.getId();
            // 创建input_port(任务)
            case 2:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_TABLE_SUFFIX + new Date().getTime();
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX / NifiConstants.AttrConstants.POSITION_X;
                buildPortDTO.componentY = componentY / NifiConstants.AttrConstants.POSITION_X + NifiConstants.PortConstants.INPUT_PORT_Y;
                portEntity = componentsBuild.buildInputPort(buildPortDTO);
                return portEntity.getId();
            // 创建output_port(任务)
            case 3:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_TABLE_SUFFIX + new Date().getTime();
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX / NifiConstants.AttrConstants.POSITION_X;
                buildPortDTO.componentY = componentY / NifiConstants.AttrConstants.POSITION_X + NifiConstants.PortConstants.OUTPUT_PORT_Y;
                portEntity = componentsBuild.buildOutputPort(buildPortDTO);
                return portEntity.getId();
            // 创建input_port(组)
            case 4:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_FIELD_SUFFIX + new Date().getTime();
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX;
                buildPortDTO.componentY = componentY + NifiConstants.PortConstants.INPUT_PORT_Y;
                portEntity = componentsBuild.buildInputPort(buildPortDTO);
                return portEntity.getId();
            // 创建output_port(组)
            case 5:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_FIELD_SUFFIX + new Date().getTime();
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX;
                buildPortDTO.componentY = componentY + NifiConstants.PortConstants.OUTPUT_PORT_Y;
                portEntity = componentsBuild.buildOutputPort(buildPortDTO);
                return portEntity.getId();
            default:
                break;
        }
        return null;
    }

    /**
     * 创建input_port/output_port connections
     *
     * @param fatherComponentId   当前组件父id
     * @param destinationGroupId  destinationGroupId
     * @param destinationId       destinationId
     * @param destinationTypeEnum destinationTypeEnum
     * @param sourceGroupId       sourceGroupId
     * @param sourceId            sourceId
     * @param sourceTypeEnum      sourceTypeEnum
     * @param level               level
     * @param typeEnum            typeEnum
     * @return connection id
     */
    public String buildPortConnection(String fatherComponentId, String destinationGroupId, String destinationId, ConnectableDTO.TypeEnum destinationTypeEnum,
                                      String sourceGroupId, String sourceId, ConnectableDTO.TypeEnum sourceTypeEnum, int level, PortComponentEnum typeEnum) {
        BuildConnectDTO buildConnectDTO = new BuildConnectDTO();
        NifiConnectDTO destination = new NifiConnectDTO();
        NifiConnectDTO source = new NifiConnectDTO();
        ConnectionEntity connectionEntity;
        log.info("连接参数为:" + fatherComponentId + "," + destinationGroupId + "," + destinationId + "," + destinationTypeEnum + "," + sourceGroupId + "," + sourceId + "," + sourceTypeEnum + "," + level + "," + typeEnum);
        switch (typeEnum.getValue()) {
            // 创建input_port连接(应用)
            case 6:
                // 创建input_port连接(任务)
            case 8:
                // 创建input_port连接(组)
            case 10:
                buildConnectDTO.fatherComponentId = fatherComponentId;
                destination.groupId = destinationGroupId;
                destination.id = destinationId;
                destination.typeEnum = destinationTypeEnum;
                source.groupId = sourceGroupId;
                source.id = sourceId;
                source.typeEnum = sourceTypeEnum;
                buildConnectDTO.destination = destination;
                buildConnectDTO.source = source;
                connectionEntity = componentsBuild.buildInputPortConnections(buildConnectDTO);
                return connectionEntity.getId();
            // 创建output_port连接(应用)
            case 7:
                // 创建output_port连接(任务)
            case 9:
                // 创建output_port连接(组)
            case 11:
                buildConnectDTO.fatherComponentId = fatherComponentId;
                destination.groupId = destinationGroupId;
                destination.id = destinationId;
                destination.typeEnum = destinationTypeEnum;
                source.groupId = sourceGroupId;
                source.id = sourceId;
                source.typeEnum = sourceTypeEnum;
                buildConnectDTO.level = level;
                buildConnectDTO.destination = destination;
                buildConnectDTO.source = source;
                connectionEntity = componentsBuild.buildOutPortPortConnections(buildConnectDTO);
                return connectionEntity.getId();
            default:
                break;
        }
        return null;
    }

    public String buildPortConnection2(String fatherComponentId, String destinationGroupId, String destinationId, ConnectableDTO.TypeEnum destinationTypeEnum,
                                       String sourceGroupId, String sourceId, ConnectableDTO.TypeEnum sourceTypeEnum, int level, PortComponentEnum typeEnum, ConnectionDTO.LoadBalanceStrategyEnum loadBalanceStrategy) {
        BuildConnectDTO buildConnectDTO = new BuildConnectDTO();
        NifiConnectDTO destination = new NifiConnectDTO();
        NifiConnectDTO source = new NifiConnectDTO();
        ConnectionEntity connectionEntity;
        log.info("连接参数为:" + fatherComponentId + "," + destinationGroupId + "," + destinationId + "," + destinationTypeEnum + "," + sourceGroupId + "," + sourceId + "," + sourceTypeEnum + "," + level + "," + typeEnum + "," + loadBalanceStrategy);
        switch (typeEnum.getValue()) {
            // 创建input_port连接(应用)
            case 6:
                // 创建input_port连接(任务)
            case 8:
                // 创建input_port连接(组)
            case 10:
                buildConnectDTO.fatherComponentId = fatherComponentId;
                destination.groupId = destinationGroupId;
                destination.id = destinationId;
                destination.typeEnum = destinationTypeEnum;
                source.groupId = sourceGroupId;
                source.id = sourceId;
                source.typeEnum = sourceTypeEnum;
                buildConnectDTO.destination = destination;
                buildConnectDTO.source = source;
                buildConnectDTO.loadBalanceStrategyEnum = loadBalanceStrategy;
                connectionEntity = componentsBuild.buildInputPortConnections(buildConnectDTO);
                return connectionEntity.getId();
            // 创建output_port连接(应用)
            case 7:
                // 创建output_port连接(任务)
            case 9:
                // 创建output_port连接(组)
            case 11:
                buildConnectDTO.fatherComponentId = fatherComponentId;
                destination.groupId = destinationGroupId;
                destination.id = destinationId;
                destination.typeEnum = destinationTypeEnum;
                source.groupId = sourceGroupId;
                source.id = sourceId;
                source.typeEnum = sourceTypeEnum;
                buildConnectDTO.level = level;
                buildConnectDTO.destination = destination;
                buildConnectDTO.source = source;
                buildConnectDTO.loadBalanceStrategyEnum = loadBalanceStrategy;
                connectionEntity = componentsBuild.buildOutPortPortConnections(buildConnectDTO);
                return connectionEntity.getId();
            default:
                break;
        }
        return null;
    }

    public List<ProcessorEntity> buildDeltaTimeProcessorEntity(List<DeltaTimeDTO> deltaTimes, String groupId, String sourceId, List<ProcessorEntity> res, TableNifiSettingPO tableNifiSettingPO, String supervisionId, List<AutoEndBranchTypeEnum> autoEndBranchTypeEnums) {
        List<ProcessorEntity> processorEntities = new ArrayList<>();
        String connectId = "";
        if (!CollectionUtils.isEmpty(deltaTimes)) {
            for (DeltaTimeDTO dto : deltaTimes) {
                //变量和变量的值都不为空
                if (Objects.nonNull(dto) && Objects.nonNull(dto.deltaTimeParameterTypeEnum) &&
                        Objects.nonNull(dto.systemVariableTypeEnum) && StringUtils.isNotEmpty(dto.variableValue)) {
                    //此变量为开始时间
                    if (Objects.equals(dto.systemVariableTypeEnum, SystemVariableTypeEnum.START_TIME)) {

                        if (Objects.equals(dto.deltaTimeParameterTypeEnum, DeltaTimeParameterTypeEnum.CONSTANT)) {
                            //该变量的值为常量,常量的话去增量表里查,需要在保存并发布的时候向增量表插入一条数据,此时不用添加组件
                            tableNifiSettingPO.queryStratTimeProcessorId = null;
                            tableNifiSettingPO.convertStratTimeToJsonProcessorId = null;
                            tableNifiSettingPO.setStratTimeProcessorId = null;

                        } else if (Objects.equals(dto.deltaTimeParameterTypeEnum, DeltaTimeParameterTypeEnum.VARIABLE)) {
                            // 该变量的值为表达式,需要去数据源查,需要加三个组件
                            // todo:2024/06/24 针对数据接入发布表 增量时间组件的数据源id  改为页面可配置 即可从应用使用的数据源读取 也可以从我们dmp_ods中读取
                            // todo:如果增量时间里 引用的系统数据源id不为空(考虑历史数据)
                            // todo:则将sourceId替换为引用的系统数据源id对应的tb_nifi_config表里的component_id
                            if (dto.referencedSystemDataSourceId != null) {
                                NifiConfigPO one = nifiConfigService.getOne(new LambdaQueryWrapper<NifiConfigPO>()
                                        .eq(NifiConfigPO::getDatasourceConfigId, dto.referencedSystemDataSourceId)
                                );
                                if (one != null) {
                                    sourceId = one.getComponentId();
                                }
                            }

                            ProcessorEntity processorEntity = queryIncrementTimeProcessor(dto.variableValue, groupId, sourceId);
                            ProcessorEntity jsonRes = convertJsonProcessor(groupId, 0, 5);
                            List<String> strings = new ArrayList<>();
                            strings.add(NifiConstants.AttrConstants.INCREMENT_DB_FIELD_START);
                            ProcessorEntity evaluateJson = evaluateTimeVariablesProcessor(groupId, strings);
                            processorEntities.add(processorEntity);
                            processorEntities.add(jsonRes);
                            processorEntities.add(evaluateJson);
                            tableNifiSettingPO.queryStratTimeProcessorId = processorEntity.getId();
                            tableNifiSettingPO.convertStratTimeToJsonProcessorId = jsonRes.getId();
                            tableNifiSettingPO.setStratTimeProcessorId = evaluateJson.getId();
                            componentConnector(groupId, processorEntity.getId(), jsonRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
                            componentsConnector(groupId, processorEntity.getId(), supervisionId, autoEndBranchTypeEnums);
                            componentConnector(groupId, jsonRes.getId(), evaluateJson.getId(), AutoEndBranchTypeEnum.SUCCESS);
                            connectId = evaluateJson.getId();
                        } else {
                            //该变量的值未定义,未定义的话用前面情况5查出来的值,此时不用加组件
                            tableNifiSettingPO.queryStratTimeProcessorId = null;
                            tableNifiSettingPO.convertStratTimeToJsonProcessorId = null;
                            tableNifiSettingPO.setStratTimeProcessorId = null;
                        }
                    } else if (Objects.equals(dto.systemVariableTypeEnum, SystemVariableTypeEnum.END_TIME)) {
                        if (Objects.equals(dto.deltaTimeParameterTypeEnum, DeltaTimeParameterTypeEnum.CONSTANT)) {
                            //该变量的值为常量,常量的话去增量表里查,需要在保存并发布的时候向增量表插入一条数据,此时不用添加组件
                            tableNifiSettingPO.queryStratTimeProcessorId = null;
                            tableNifiSettingPO.convertStratTimeToJsonProcessorId = null;
                            tableNifiSettingPO.setStratTimeProcessorId = null;
                        } else if (Objects.equals(dto.deltaTimeParameterTypeEnum, DeltaTimeParameterTypeEnum.VARIABLE)) {
                            //该变量的值为表达式,需要去数据源查,需要加三个组件
                            // todo:2024/06/24 针对数据接入发布表 增量时间组件的数据源id  改为页面可配置 即可从应用使用的数据源读取 也可以从我们dmp_ods中读取
                            // todo:如果增量时间里 引用的系统数据源id不为空(考虑历史数据)
                            // todo:则将sourceId替换为引用的系统数据源id对应的tb_nifi_config表里的component_id
                            if (dto.referencedSystemDataSourceId != null) {
                                NifiConfigPO one = nifiConfigService.getOne(new LambdaQueryWrapper<NifiConfigPO>()
                                        .eq(NifiConfigPO::getDatasourceConfigId, dto.referencedSystemDataSourceId)
                                );
                                if (one != null) {
                                    sourceId = one.getComponentId();
                                }
                            }
                            ProcessorEntity processorEntity = queryIncrementTimeProcessor(dto.variableValue, groupId, sourceId);
                            ProcessorEntity jsonRes = convertJsonProcessor(groupId, 0, 5);
                            List<String> strings = new ArrayList<>();
                            strings.add(NifiConstants.AttrConstants.INCREMENT_DB_FIELD_END);
                            ProcessorEntity evaluateJson = evaluateTimeVariablesProcessor(groupId, strings);
                            processorEntities.add(processorEntity);
                            processorEntities.add(jsonRes);
                            processorEntities.add(evaluateJson);
                            tableNifiSettingPO.queryEndTimeProcessorId = processorEntity.getId();
                            tableNifiSettingPO.convertEndTimeToJsonProcessorId = jsonRes.getId();
                            tableNifiSettingPO.setEndTimeProcessorId = evaluateJson.getId();
                            if (connectId != "") {
                                componentConnector(groupId, connectId, processorEntity.getId(), AutoEndBranchTypeEnum.MATCHED);
                            }
                            componentConnector(groupId, processorEntity.getId(), jsonRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
                            componentsConnector(groupId, processorEntity.getId(), supervisionId, autoEndBranchTypeEnums);
                            componentConnector(groupId, jsonRes.getId(), evaluateJson.getId(), AutoEndBranchTypeEnum.SUCCESS);
                        } else {
                            //该变量的值未定义,未定义的话用前面情况5查出来的值,此时不用加组件
                            tableNifiSettingPO.queryStratTimeProcessorId = null;
                            tableNifiSettingPO.convertStratTimeToJsonProcessorId = null;
                            tableNifiSettingPO.setStratTimeProcessorId = null;
                        }
                    }
                }
            }
        }
        res.addAll(processorEntities);
        return processorEntities;
    }

    /**
     * @param generateVersionSql
     * @param groupId
     * @param targetDbPoolId
     * @param res
     * @param tableNifiSettingPO
     * @return
     */
    public List<ProcessorEntity> buildgenerateVersionProcessorEntity(String generateVersionSql, String groupId, String targetDbPoolId, List<ProcessorEntity> res, TableNifiSettingPO tableNifiSettingPO) {
        List<ProcessorEntity> processorEntities = new ArrayList<>();
        if (StringUtils.isNotEmpty(generateVersionSql)) {
            //todo 定义变量的三个组件
            ProcessorEntity processorEntity = queryIncrementTimeProcessor(generateVersionSql, groupId, targetDbPoolId);
            ProcessorEntity jsonRes = convertJsonProcessor(groupId, 0, 5);
            List<String> strings = new ArrayList<>();
            strings.add(NifiConstants.AttrConstants.FI_VERSION);
            ProcessorEntity evaluateJson = evaluateTimeVariablesProcessor(groupId, strings);
            processorEntities.add(processorEntity);
            processorEntities.add(jsonRes);
            processorEntities.add(evaluateJson);
            tableNifiSettingPO.queryVersionProcessorId = processorEntity.getId();
            tableNifiSettingPO.convertVersionToJsonProcessorId = jsonRes.getId();
            tableNifiSettingPO.setVersionProcessorId = evaluateJson.getId();
            componentConnector(groupId, processorEntity.getId(), jsonRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
            componentConnector(groupId, jsonRes.getId(), evaluateJson.getId(), AutoEndBranchTypeEnum.SUCCESS);
            //todo 调用删除的两个组件 replaceTextForVersionProcessorId  invokeHttpForVersionProcessorId
            ProcessorEntity replaceTextForVisionProcess = replaceTextForVisionProcess(null, groupId, null);
            ProcessorEntity invokeHTTPForVisionProcessor = invokeHTTPForVisionProcessor(groupId);
            componentConnector(groupId, evaluateJson.getId(), replaceTextForVisionProcess.getId(), AutoEndBranchTypeEnum.MATCHED);
            componentConnector(groupId, replaceTextForVisionProcess.getId(), invokeHTTPForVisionProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
            res.add(processorEntity);
            res.add(jsonRes);
            res.add(evaluateJson);
            res.add(replaceTextForVisionProcess);
            res.add(invokeHTTPForVisionProcessor);
            tableNifiSettingPO.replaceTextForVersionProcessorId = replaceTextForVisionProcess.getId();
            tableNifiSettingPO.invokeHttpForVersionProcessorId = invokeHTTPForVisionProcessor.getId();
            processorEntities.add(replaceTextForVisionProcess);
            processorEntities.add(invokeHTTPForVisionProcessor);
        } else {
            tableNifiSettingPO.queryVersionProcessorId = null;
            tableNifiSettingPO.convertVersionToJsonProcessorId = null;
            tableNifiSettingPO.setVersionProcessorId = null;
            tableNifiSettingPO.replaceTextForVersionProcessorId = null;
            tableNifiSettingPO.invokeHttpForVersionProcessorId = null;
        }
        return processorEntities;
    }

    /**
     * 执行sql 查询增量字段组件
     *
     * @param sql            查询语句
     * @param groupId        组id
     * @param sourceDbPoolId 增量配置库连接池id
     * @return 组件对象
     */
    private ProcessorEntity queryIncrementTimeProcessor(String sql, String groupId, String sourceDbPoolId) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Query Increment Field";
        querySqlDto.details = "query_phase";
        querySqlDto.groupId = groupId;
        querySqlDto.querySql = sql;
        querySqlDto.dbConnectionId = sourceDbPoolId;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(4);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<String>());
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    /**
     * 创建任务组
     *
     * @param name
     * @param groupId
     * @return
     */
    public ProcessGroupEntity buildTaskGroup(String name, String detail, String groupId) {
        BuildProcessGroupDTO dto = new BuildProcessGroupDTO();
        dto.name = name;
        dto.details = detail;
        dto.groupId = groupId;
        //根据组个数，定义坐标
        int count = componentsBuild.getGroupCount(groupId);
        dto.positionDTO = NifiPositionHelper.buildXPositionDTO(count + 10);
        //创建组件
        BusinessResult<ProcessGroupEntity> res = componentsBuild.buildProcessGroup(dto);
        if (res.success) {
            return res.data;
        } else {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
        }
    }

    public List<ProcessorEntity> buildScriptProcessor(String groupId, NifiCustomWorkflowDTO nifiCustomWorkflow, NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO) {
        String targetControllerServiceId = "";
        // 目标
        targetControllerServiceId = saveDbconfig(nifiCustomWorkflowDetailDTO.dataSourceId);
        List<ProcessorEntity> res = new ArrayList<>();
        TableNifiSettingPO tableNifiSettingPO = new TableNifiSettingPO();
        TableNifiSettingPO tableNifiSettingPO1 = new TableNifiSettingPO();
        tableNifiSettingPO1 = tableNifiSettingService.query().eq("table_access_id", nifiCustomWorkflowDetailDTO.id).eq("type", OlapTableEnum.CUSTOMIZESCRIPT.getValue()).one();

        if (tableNifiSettingPO1 != null) {
            tableNifiSettingPO = tableNifiSettingPO1;
        }
        tableNifiSettingPO.tableComponentId = groupId;
        tableNifiSettingPO.type = OlapTableEnum.CUSTOMIZESCRIPT.getValue();
        tableNifiSettingPO.tableName = nifiCustomWorkflowDetailDTO.componentName;
        tableNifiSettingPO.tableAccessId = (int) nifiCustomWorkflowDetailDTO.id;
        //日志监控
        List<ProcessorEntity> processorEntities = pipelineSupervision(groupId, tableNifiSettingPO);
        String supervisionId = processorEntities.get(0).getId();
        //调度组件,在数据接入的时候调一次
        ProcessorEntity publishKafkaProcessor = createPublishKafkaProcessor(nifiCustomWorkflow, nifiCustomWorkflowDetailDTO, groupId, 1);
        tableNifiSettingPO.publishKafkaProcessorId = publishKafkaProcessor.getId();
        //原变量字段
        ProcessorEntity evaluateJsonPathProcessor = evaluateJsonPathProcessor(groupId);
        tableNifiSettingPO.setIncrementProcessorId = evaluateJsonPathProcessor.getId();
        res.add(evaluateJsonPathProcessor);
        //接受消息ConsumeKafka
        componentConnector(groupId, publishKafkaProcessor.getId(), evaluateJsonPathProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);

        ProcessorEntity queryField = queryIncrementFieldProcessor(groupId, targetControllerServiceId, nifiCustomWorkflowDetailDTO);
        componentConnector(groupId, evaluateJsonPathProcessor.getId(), queryField.getId(), AutoEndBranchTypeEnum.MATCHED);
        componentConnector(groupId, queryField.getId(), supervisionId, AutoEndBranchTypeEnum.FAILURE);
        tableNifiSettingPO.queryIncrementProcessorId = queryField.getId();
        ProcessorEntity queryNumbers = queryNumbers(groupId, targetControllerServiceId, nifiCustomWorkflowDetailDTO);
        componentConnector(groupId, queryField.getId(), queryNumbers.getId(), AutoEndBranchTypeEnum.SUCCESS);
        tableNifiSettingPO.queryNumbersProcessorId = queryNumbers.getId();
        //转json
        ProcessorEntity numberToJsonRes = convertJsonProcessor(groupId, 0, 5);
        componentConnector(groupId, queryNumbers.getId(), numberToJsonRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        tableNifiSettingPO.convertNumbersToJsonProcessorId = numberToJsonRes.getId();

        ProcessorEntity publishKafkaForPipelineProcessor = createPublishKafkaForPipelineProcessor(groupId, 0, 6);
        componentConnector(groupId, numberToJsonRes.getId(), publishKafkaForPipelineProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
        tableNifiSettingPO.publishKafkaPipelineProcessorId = publishKafkaForPipelineProcessor.getId();
        res.addAll(processorEntities);
        tableNifiSettingService.saveOrUpdate(tableNifiSettingPO);
        return res;
    }


    public List<ProcessorEntity> pipelineSupervision(String groupId, TableNifiSettingPO TableNifiSettingPO) {
        List<ProcessorEntity> processorEntityList = new ArrayList<>();
        //查询
        ProcessorEntity processorEntity = queryForPipelineSupervision(groupId);
        TableNifiSettingPO.queryForSupervisionProcessorId = processorEntity.getId();
        processorEntityList.add(processorEntity);
        //转string convertJsonForSupervision
        ProcessorEntity convertJsonForSupervision = specificSymbolProcessor(groupId);
        TableNifiSettingPO.convertJsonForSupervisionProcessorId = convertJsonForSupervision.getId();
        componentConnector(groupId, processorEntity.getId(), convertJsonForSupervision.getId(), AutoEndBranchTypeEnum.SUCCESS);
        processorEntityList.add(convertJsonForSupervision);
        //发消息
        ProcessorEntity publishKafkaForSupervisionProcessor = createPublishKafkaForSupervisionProcessor(groupId, 3);
        TableNifiSettingPO.publishKafkaForSupervisionProcessorId = publishKafkaForSupervisionProcessor.getId();
        componentConnector(groupId, convertJsonForSupervision.getId(), publishKafkaForSupervisionProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
        processorEntityList.add(publishKafkaForSupervisionProcessor);
        return processorEntityList;
    }

    private ProcessorEntity queryForPipelineSupervision(String groupId) {
        BuildReplaceTextProcessorDTO buildReplaceTextProcessorDTO = new BuildReplaceTextProcessorDTO();
        buildReplaceTextProcessorDTO.name = "queryForPipelineSupervision";
        buildReplaceTextProcessorDTO.details = "queryForPipelineSupervision";
        buildReplaceTextProcessorDTO.groupId = groupId;
        buildReplaceTextProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(1, 3);
        //替换流文件
        buildReplaceTextProcessorDTO.evaluationMode = "Entire text";
        NifiMessageDTO nifiMessage = new NifiMessageDTO();
        nifiMessage.message = "${executesql.error.message:escapeJson()}";
        nifiMessage.topic = "${kafka.topic}";
        nifiMessage.groupId = groupId;
        nifiMessage.startTime = "${start_time}";
        nifiMessage.endTime = "${end_time}";
        nifiMessage.counts = "${numbers}";
        nifiMessage.pipelStageTraceId = "${pipelStageTraceId}";
        nifiMessage.pipelTaskTraceId = "${pipelTaskTraceId}";
        nifiMessage.pipelJobTraceId = "${pipelJobTraceId}";
        nifiMessage.pipelTraceId = "${pipelTraceId}";
        nifiMessage.entryDate = "${entryDate:format('YYYY-MM-dd HH:mm:ss')}";
        buildReplaceTextProcessorDTO.replacementValue = JSON.toJSONString(nifiMessage);
        buildReplaceTextProcessorDTO.maximumBufferSize = "100 MB";
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildReplaceTextProcess(buildReplaceTextProcessorDTO, new ArrayList<>());
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    private ProcessorEntity specificSymbolProcessor(String groupId) {
        BuildReplaceTextProcessorDTO buildReplaceTextProcessorDTO = new BuildReplaceTextProcessorDTO();
        buildReplaceTextProcessorDTO.name = "specificSymbol";
        buildReplaceTextProcessorDTO.details = "specificSymbol";
        buildReplaceTextProcessorDTO.groupId = groupId;
        buildReplaceTextProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(2, 3);
        //替换流文件
        buildReplaceTextProcessorDTO.evaluationMode = "Entire text";
        buildReplaceTextProcessorDTO.replacementValue = "$1";
        buildReplaceTextProcessorDTO.maximumBufferSize = "100 MB";
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildReplaceTextProcess(buildReplaceTextProcessorDTO, new ArrayList<>());
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }


    /**
     * 创建变量组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity evaluateJsonPathProcessor(String groupId) {
        ArrayList<String> strings = new ArrayList<>();
        //strings.add(NifiConstants.AttrConstants.INCREMENT_START);
        strings.add(NifiConstants.AttrConstants.START_TIME);
        strings.add(NifiConstants.AttrConstants.FIDATA_BATCH_CODE);
        strings.add(NifiConstants.AttrConstants.PIPEL_TRACE_ID);
        strings.add(NifiConstants.AttrConstants.PIPEL_JOB_TRACE_ID);
        strings.add(NifiConstants.AttrConstants.PIPEL_TASK_TRACE_ID);
        strings.add(NifiConstants.AttrConstants.PIPEL_STAGE_TRACE_ID);
        strings.add(NifiConstants.AttrConstants.TOPIC_TYPE);
        strings.add(NifiConstants.AttrConstants.TASK_ID);
        //strings.add(NifiConstants.AttrConstants.START_TIME);
        BuildProcessEvaluateJsonPathDTO dto = new BuildProcessEvaluateJsonPathDTO();
        dto.name = "Set Increment Field";
        dto.details = "query_phase";
        dto.groupId = groupId;
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(2);
        dto.selfDefinedParameter = strings;
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildEvaluateJsonPathProcess(dto);
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    /**
     * createPublishKafkaProcessor
     *
     * @param nifiCustomWorkflow
     * @param nifiCustomWorkflowDetailDTO
     * @param groupId
     * @param position
     * @return
     */
    public ProcessorEntity createPublishKafkaProcessor(NifiCustomWorkflowDTO nifiCustomWorkflow, NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO, String groupId, int position) {

        BuildConsumeKafkaProcessorDTO buildConsumeKafkaProcessorDTO = new BuildConsumeKafkaProcessorDTO();
        buildConsumeKafkaProcessorDTO.name = "ConsumeKafka";
        buildConsumeKafkaProcessorDTO.details = "query_phase";
        buildConsumeKafkaProcessorDTO.groupId = groupId;
        //管道id
        buildConsumeKafkaProcessorDTO.GroupId = "dmp.nifi.datafactory.pipeline";
        buildConsumeKafkaProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(position);
        Map<String, String> variable = new HashMap<>();
        variable.put(ComponentIdTypeEnum.KAFKA_BROKERS.getName(), KafkaBrokers);
        componentsBuild.buildNifiGlobalVariable(variable);
        buildConsumeKafkaProcessorDTO.kafkaBrokers = "${" + ComponentIdTypeEnum.KAFKA_BROKERS.getName() + "}";
        buildConsumeKafkaProcessorDTO.honorTransactions = false;
        buildConsumeKafkaProcessorDTO.topicNames = MqConstants.TopicPrefix.TOPIC_PREFIX + nifiCustomWorkflow.id + "." + OlapTableEnum.CUSTOMIZESCRIPT.getValue() + ".0." + nifiCustomWorkflowDetailDTO.id;
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildConsumeKafkaProcessor(buildConsumeKafkaProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;

    }

    /**
     * 执行sql 查询增量字段组件
     *
     * @param config      数据接入配置
     * @param groupId     组id
     * @param cfgDbPoolId 增量配置库连接池id
     * @return 组件对象
     */
    private ProcessorEntity queryIncrementFieldProcessor(DataAccessConfigDTO config, String groupId, String cfgDbPoolId, BuildNifiFlowDTO dto) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Query Increment Field";
        querySqlDto.details = "query_phase";
        querySqlDto.groupId = groupId;
        querySqlDto.querySql = buildIncrementSql(config.processorConfig.targetTableName);
        if (Objects.equals(dto.type, OlapTableEnum.KPI)) {
            querySqlDto.querySql = buildIncrementSql(config.processorConfig.targetTableName + OlapTableEnum.KPI.getValue());
        }
        querySqlDto.dbConnectionId = cfgDbPoolId;
        /*querySqlDto.scheduleExpression = config.processorConfig.scheduleExpression;
        querySqlDto.scheduleType = config.processorConfig.scheduleType;*/
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(4);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<String>());
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }
}
