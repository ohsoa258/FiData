package com.fisk.task.consumer.nifi.impl;

import com.alibaba.fastjson.JSON;
import com.davis.client.ApiException;
import com.davis.client.model.ProcessorRunStatusEntity;
import com.davis.client.model.*;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.constants.NifiConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.FuncNameEnum;
import com.fisk.common.enums.task.SynchronousTypeEnum;
import com.fisk.common.enums.task.TopicTypeEnum;
import com.fisk.common.enums.task.nifi.*;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.NifiAccessDTO;
import com.fisk.dataaccess.dto.TableBusinessDTO;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.dataaccess.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.dataaccess.enums.ComponentIdTypeEnum;
import com.fisk.dataaccess.enums.syncModeTypeEnum;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.syncmode.GetTableBusinessDTO;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.task.consumer.nifi.INifiTaskListener;
import com.fisk.task.dto.daconfig.*;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.task.dto.nifi.*;
import com.fisk.task.dto.task.*;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.enums.PortComponentEnum;
import com.fisk.task.mapper.TBETLIncrementalMapper;
import com.fisk.task.service.nifi.INifiComponentsBuild;
import com.fisk.task.service.nifi.impl.AppNifiSettingServiceImpl;
import com.fisk.task.service.nifi.impl.TableNifiSettingServiceImpl;
import com.fisk.task.service.pipeline.ITableTopicService;
import com.fisk.task.service.pipeline.impl.NifiConfigServiceImpl;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.NifiPositionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
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
    @Value("${pgsql-datainput.url}")
    private String pgsqlDatainputUrl;
    @Value("${pgsql-datainput.username}")
    private String pgsqlDatainputUsername;
    @Value("${pgsql-datainput.password}")
    private String pgsqlDatainputPassword;

    @Value("${nifi-MaxRowsPerFlowFile}")
    public String MaxRowsPerFlowFile;
    @Value("${nifi-OutputBatchSize}")
    public String OutputBatchSize;
    @Value("${nifi-FetchSize}")
    public String FetchSize;
    @Value("${nifi-ConcurrentTasks}")
    public String ConcurrentTasks;
    @Value("${pgsql-datamodel.url}")
    public String pgsqlDatamodelUrl;
    @Value("${pgsql-datamodel.username}")
    public String pgsqlDatamodelUsername;
    @Value("${pgsql-datamodel.password}")
    public String pgsqlDatamodelPassword;
    @Value("${spring.kafka.producer.bootstrap-servers}")
    public String KafkaBrokers;
    @Value("${nifi.pipeline.topicName}")
    public String pipelineTopicName;
    @Resource
    INifiComponentsBuild componentsBuild;
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
    RestTemplate httpClient;

    public String appParentGroupId;
    public String appGroupId;
    public String groupEntityId;
    public String taskGroupEntityId;
    public String appInputPortId;
    public String tableInputPortId;
    public String appOutputPortId;
    public String tableOutputPortId;


    //@MQConsumerLog
    @Override
    public void msg(String data, Acknowledgment ack) {
        ModelPublishStatusDTO modelPublishStatusDTO = new ModelPublishStatusDTO();
        modelPublishStatusDTO.publish = 1;
        log.info("创建nifi流程发布参数:"+data);
        try {
            BuildNifiFlowDTO dto = JSON.parseObject(data, BuildNifiFlowDTO.class);
            modelPublishStatusDTO.tableId = dto.id;
            client.updateTablePublishStatus(modelPublishStatusDTO);
            //获取数据接入配置项
            DataAccessConfigDTO configDTO = getConfigData(dto.id, dto.appId, dto.synchronousTypeEnum, dto.type, dto.dataClassifyEnum, dto.tableName, dto.selectSql, dto);
            if (configDTO == null) {
                log.error("数据接入配置项获取失败。id: 【" + dto.id + "】, appId: 【" + dto.appId + "】");
                return;
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
            appNifiSettingPO.sourceDbPoolComponentId = dbPool.get(0).getId();
            appNifiSettingPO.targetDbPoolComponentId = dbPool.get(1).getId();

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
            List<ProcessorEntity> processors = buildProcessorVersion2(groupEntity.getId(), configDTO, taskGroupEntity.getId(), dbPool.get(0).getId(), dbPool.get(1).getId(), cfgDbPool.getId(), appNifiSettingPO, dto);

            enabledProcessor(taskGroupEntity.getId(), processors.subList(0, processors.size() - 1));
            //7. 如果是接入,同步一次,然后把调度组件停掉
            if (dto.groupStructureId == null && dto.openTransmission) {
                for (int i = 2; i > 0; i--) {
                    Thread.sleep(200);
                    enabledProcessor(taskGroupEntity.getId(), processors.subList(processors.size() - 1, processors.size()));
                    ProcessorEntity processorEntity = processors.get(processors.size() - 1);
                    ProcessorRunStatusEntity processorRunStatusEntity = new ProcessorRunStatusEntity();
                    processorRunStatusEntity.setDisconnectedNodeAcknowledged(false);
                    processorRunStatusEntity.setRevision(processorEntity.getRevision());
                    processorRunStatusEntity.setState(ProcessorRunStatusEntity.StateEnum.STOPPED);
                    NifiHelper.getProcessorsApi().updateRunStatus(processorEntity.getId(), processorRunStatusEntity);
                }
            }
            //7. 回写id
            savaNifiConfig(cfgDbPool.getId(), ComponentIdTypeEnum.CFG_DB_POOL_COMPONENT_ID);
            if (Objects.equals(dto.synchronousTypeEnum.getName(), SynchronousTypeEnum.TOPGODS.getName())) {
                savaNifiConfig(dbPool.get(1).getId(), ComponentIdTypeEnum.PG_ODS_DB_POOL_COMPONENT_ID);
            } else if (Objects.equals(dto.synchronousTypeEnum.getName(), SynchronousTypeEnum.PGTOPG.getName())) {
                savaNifiConfig(dbPool.get(1).getId(), ComponentIdTypeEnum.PG_DW_DB_POOL_COMPONENT_ID);
                savaNifiConfig(dbPool.get(0).getId(), ComponentIdTypeEnum.PG_ODS_DB_POOL_COMPONENT_ID);
            } else if (Objects.equals(dto.synchronousTypeEnum.getName(), SynchronousTypeEnum.PGTODORIS.getName())) {
                savaNifiConfig(dbPool.get(1).getId(), ComponentIdTypeEnum.DORIS_OLAP_DB_POOL_COMPONENT_ID);
                savaNifiConfig(dbPool.get(0).getId(), ComponentIdTypeEnum.PG_DW_DB_POOL_COMPONENT_ID);
            }
        } catch (Exception e) {
            modelPublishStatusDTO.publish = 2;
            client.updateTablePublishStatus(modelPublishStatusDTO);
            log.error("nifi流程创建失败" + e.getMessage());
        } finally {
            ack.acknowledge();
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
    private DataAccessConfigDTO getConfigData(long id, long appId, SynchronousTypeEnum synchronousTypeEnum, OlapTableEnum type, DataClassifyEnum dataClassifyEnum, String tableName, String selectSql, BuildNifiFlowDTO buildNifiFlowDTO) {
        DataAccessConfigDTO data = new DataAccessConfigDTO();
        GroupConfig groupConfig = new GroupConfig();
        DataSourceConfig targetDbPoolConfig = new DataSourceConfig();
        DataSourceConfig sourceDsConfig = new DataSourceConfig();
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
            targetDbPoolConfig.type = DriverTypeEnum.POSTGRESQL;
            targetDbPoolConfig.user = pgsqlDatainputUsername;
            targetDbPoolConfig.password = pgsqlDatainputPassword;
            targetDbPoolConfig.jdbcStr = pgsqlDatainputUrl;
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
                processorConfig.sourceExecSqlQuery = tableNifiSettingPO.selectSql;
                targetDbPoolConfig.syncMode = tableNifiSettingPO.syncMode;
            } else {
                targetDbPoolConfig.syncMode = res.data.targetDsConfig.syncMode;
            }
            sourceDsConfig = res.data.sourceDsConfig;
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
            sourceDsConfig.type = DriverTypeEnum.POSTGRESQL;
            sourceDsConfig.jdbcStr = pgsqlDatamodelUrl;
            sourceDsConfig.user = pgsqlDatamodelUsername;
            sourceDsConfig.password = pgsqlDatamodelPassword;
            targetDbPoolConfig.type = DriverTypeEnum.MYSQL;
            targetDbPoolConfig.user = dorisUser;
            targetDbPoolConfig.password = dorisPwd;
            targetDbPoolConfig.jdbcStr = dorisUrl;
            targetDbPoolConfig.targetTableName = tableName.toLowerCase();
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
            sourceDsConfig.type = DriverTypeEnum.POSTGRESQL;
            sourceDsConfig.jdbcStr = pgsqlDatainputUrl;
            sourceDsConfig.user = pgsqlDatainputUsername;
            sourceDsConfig.password = pgsqlDatainputPassword;
            targetDbPoolConfig.type = DriverTypeEnum.POSTGRESQL;
            targetDbPoolConfig.user = pgsqlDatamodelUsername;
            targetDbPoolConfig.password = pgsqlDatamodelPassword;
            targetDbPoolConfig.jdbcStr = pgsqlDatamodelUrl;
            targetDbPoolConfig.targetTableName = tableName.toLowerCase();
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

        if (!data.groupConfig.newApp && data.targetDsConfig != null) {
            targetDbPoolConfig.componentId = appNifiSettingPO.targetDbPoolComponentId;
            sourceDsConfig.componentId = appNifiSettingPO.sourceDbPoolComponentId;
        }
        data.targetDsConfig = targetDbPoolConfig;
        data.sourceDsConfig = sourceDsConfig;
        data.processorConfig = processorConfig;
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
    private List<ControllerServiceEntity> buildDsConnectionPool(SynchronousTypeEnum synchronousTypeEnum, DataAccessConfigDTO config, String groupId, BuildNifiFlowDTO buildNifiFlowDTO) {
        List<ControllerServiceEntity> list = new ArrayList<>();
        NifiConfigPO nifiConfigPo = new NifiConfigPO();
        NifiConfigPO nifiSourceConfigPo = null;
        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
            nifiConfigPo = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.PG_ODS_DB_POOL_COMPONENT_ID.getName()).one();
        } else if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTODORIS)) {
            nifiConfigPo = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.DORIS_OLAP_DB_POOL_COMPONENT_ID.getName()).one();
            nifiSourceConfigPo = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.PG_DW_DB_POOL_COMPONENT_ID.getName()).one();
        } else if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTOPG)) {
            nifiConfigPo = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.PG_DW_DB_POOL_COMPONENT_ID.getName()).one();
            nifiSourceConfigPo = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.PG_ODS_DB_POOL_COMPONENT_ID.getName()).one();
        }
        BusinessResult<ControllerServiceEntity> targetRes = new BusinessResult<>(true, "控制器服务创建成功");
        BusinessResult<ControllerServiceEntity> sourceRes = new BusinessResult<>(true, "控制器服务创建成功");
        if (config.groupConfig.newApp || Objects.equals(buildNifiFlowDTO.dataClassifyEnum, DataClassifyEnum.CUSTOMWORKDATAACCESS) ||
                Objects.equals(buildNifiFlowDTO.dataClassifyEnum, DataClassifyEnum.DATAMODELKPL)) {
            BuildDbControllerServiceDTO targetDto = buildDbControllerServiceDTO(config, NifiConstants.ApiConstants.ROOT_NODE, DbPoolTypeEnum.TARGET, synchronousTypeEnum);
            if (nifiConfigPo != null) {
                ControllerServiceEntity data = new ControllerServiceEntity();
                data.setId(nifiConfigPo.componentId);
                targetRes.data = data;
            } else {
                targetRes = componentsBuild.buildDbControllerService(targetDto);
            }
            //来源库
            if (nifiSourceConfigPo != null) {
                ControllerServiceEntity data = new ControllerServiceEntity();
                data.setId(nifiSourceConfigPo.componentId);
                sourceRes.data = data;
            } else {
                BuildDbControllerServiceDTO sourceDto = buildDbControllerServiceDTO(config, groupId, DbPoolTypeEnum.SOURCE, synchronousTypeEnum);
                sourceRes = componentsBuild.buildDbControllerService(sourceDto);
            }
            if (targetRes.success && sourceRes.success) {
                list.add(sourceRes.data);
                list.add(targetRes.data);
                return list;
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, "【target】" + targetRes.msg + ",【source】" + sourceRes.msg);
            }
        } else {
            ControllerServiceEntity sourceControllerService = componentsBuild.getDbControllerService(config.sourceDsConfig.componentId);
            ControllerServiceEntity targetResControllerService = componentsBuild.getDbControllerService(config.targetDsConfig.componentId);
            if (sourceControllerService != null && targetResControllerService != null) {
                list.add(sourceControllerService);
                list.add(targetResControllerService);
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
            default:
                break;
        }
        return dto;
    }


    private List<ProcessorEntity> buildProcessorVersion2(String appGroupId, DataAccessConfigDTO config, String groupId, String sourceDbPoolId, String targetDbPoolId, String cfgDbPoolId, AppNifiSettingPO appNifiSettingPO, BuildNifiFlowDTO dto) {
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
        autoEndBranchTypeEnums.add(AutoEndBranchTypeEnum.SUCCESS);
        autoEndBranchTypeEnums.add(AutoEndBranchTypeEnum.FAILURE);
        List<ProcessorEntity> processorEntities = pipelineSupervision(groupId, res, cfgDbPoolId, tableNifiSettingPO);
        String supervisionId = processorEntities.get(0).getId();
        //调度组件,在数据接入的时候调一次
        ProcessorEntity dispatchProcessor = new ProcessorEntity();
        ProcessorEntity publishKafkaProcessor = new ProcessorEntity();
        String inputPortId = "";
        if (dto.groupStructureId == null) {
            dispatchProcessor = queryDispatchProcessor(config, groupId, cfgDbPoolId);
            //发送消息PublishKafka
            publishKafkaProcessor = createPublishKafkaProcessor(config, dto, groupId, 2);
            componentConnector(groupId, dispatchProcessor.getId(), publishKafkaProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
        }
        //读取增量字段组件
        ProcessorEntity queryField = queryIncrementFieldProcessor(config, groupId, cfgDbPoolId, dto);
        //接受消息ConsumeKafka
        ProcessorEntity consumeKafkaProcessor = createConsumeKafkaProcessor(config, dto, groupId);
        componentConnector(groupId, consumeKafkaProcessor.getId(), queryField.getId(), AutoEndBranchTypeEnum.SUCCESS);
        tableNifiSettingPO.consumeKafkaProcessorId = consumeKafkaProcessor.getId();


        if (dto.groupStructureId != null) {
            //  创建input_port(组)
            PositionDTO position = queryField.getComponent().getPosition();
            inputPortId = buildPortComponent(config.taskGroupConfig.appName, groupId, position.getX(), position.getY(),
                    PortComponentEnum.COMPONENT_INPUT_PORT_COMPONENT);
        }

        tableNifiSettingPO.queryIncrementProcessorId = queryField.getId();
        //创建数据转换json组件
        ProcessorEntity jsonRes = convertJsonProcessor(groupId, 0, 5);
        tableNifiSettingPO.convertDataToJsonProcessorId = jsonRes.getId();
        //连接器
        componentConnector(groupId, queryField.getId(), jsonRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, queryField.getId(), supervisionId, autoEndBranchTypeEnums);
        //字段转换nifi变量
        ProcessorEntity evaluateJson = evaluateJsonPathProcessor(groupId);
        tableNifiSettingPO.setIncrementProcessorId = evaluateJson.getId();
        //连接器
        componentConnector(groupId, jsonRes.getId(), evaluateJson.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, jsonRes.getId(), supervisionId, autoEndBranchTypeEnums);
        //创建log
        ProcessorEntity logProcessor = putLogProcessor(groupId, cfgDbPoolId, dto, config);
        tableNifiSettingPO.putLogToConfigDbProcessorId = logProcessor.getId();
        //连接器
        componentConnector(groupId, evaluateJson.getId(), logProcessor.getId(), AutoEndBranchTypeEnum.MATCHED);
        //创建执行删除组件
        ProcessorEntity delSqlRes = execDeleteSqlProcessor(config, groupId, targetDbPoolId, synchronousTypeEnum);
        tableNifiSettingPO.executeTargetDeleteProcessorId = delSqlRes.getId();
        //连接器
        componentConnector(groupId, logProcessor.getId(), delSqlRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, logProcessor.getId(), supervisionId, autoEndBranchTypeEnums);
        //执行查询组件
        ProcessorEntity executeSQLRecord = new ProcessorEntity();
        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTODORIS)) {
            executeSQLRecord = createExecuteSQLRecordDoris(config, groupId, dto, targetDbPoolId);
        } else {
            executeSQLRecord = createExecuteSQLRecord(appGroupId, config, groupId, dto, sourceDbPoolId, tableNifiSettingPO);
        }

        tableNifiSettingPO.executeSqlRecordProcessorId = executeSQLRecord.getId();
        //连接器
        componentConnector(groupId, delSqlRes.getId(), executeSQLRecord.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, delSqlRes.getId(), supervisionId, autoEndBranchTypeEnums);

        String lastId = executeSQLRecord.getId();
        Boolean isLastId = true;

        //pg2doris不需要调用存储过程
        ProcessorEntity processorEntity1 = executeSQLRecord;
        if (!Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTODORIS)) {
            isLastId = false;
            //字段映射转换
            ProcessorEntity updateField = createUpdateField(appGroupId, config, groupId, dto, tableNifiSettingPO);
            tableNifiSettingPO.updateFieldProcessorId = updateField.getId();
            componentConnector(groupId, executeSQLRecord.getId(), updateField.getId(), AutoEndBranchTypeEnum.SUCCESS);
            componentsConnector(groupId, executeSQLRecord.getId(), supervisionId, autoEndBranchTypeEnums);
            //数据入库
            ProcessorEntity putDatabaseRecord = createPutDatabaseRecord(appGroupId, config, groupId, dto, targetDbPoolId, synchronousTypeEnum, tableNifiSettingPO);
            tableNifiSettingPO.saveTargetDbProcessorId = putDatabaseRecord.getId();
            //连接器
            componentConnector(groupId, updateField.getId(), putDatabaseRecord.getId(), AutoEndBranchTypeEnum.SUCCESS);
            componentsConnector(groupId, updateField.getId(), supervisionId, autoEndBranchTypeEnums);
            //合并流文件组件
            //ProcessorEntity mergeRes = mergeContentProcessor(groupId);
            //tableNifiSettingPO.mergeContentProcessorId = mergeRes.getId();
            //连接器
            //componentConnector(groupId, putDatabaseRecord.getId(), mergeRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
            //用组件,调存储过程把stg里的数据向ods里面插入
            processorEntity1 = CallDbProcedure(config, groupId, targetDbPoolId, synchronousTypeEnum);
            tableNifiSettingPO.odsToStgProcessorId = processorEntity1.getId();
            //连接器
            componentConnector(groupId, putDatabaseRecord.getId(), processorEntity1.getId(), AutoEndBranchTypeEnum.SUCCESS);
            componentsConnector(groupId, putDatabaseRecord.getId(), supervisionId, autoEndBranchTypeEnums);
            res.add(putDatabaseRecord);
            res.add(updateField);
        }
        //查询条数
        ProcessorEntity queryNumbers = queryNumbersProcessor(dto, config, groupId, targetDbPoolId);
        tableNifiSettingPO.queryNumbersProcessorId = queryNumbers.getId();
        //连接器
        componentConnector(groupId, processorEntity1.getId(), queryNumbers.getId(), AutoEndBranchTypeEnum.SUCCESS);
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
        ProcessorEntity processorEntity = CallDbLogProcedure(config, groupId, cfgDbPoolId);
        tableNifiSettingPO.saveNumbersProcessorId = processorEntity.getId();
        //连接器
        componentConnector(groupId, evaluateJsons.getId(), processorEntity.getId(), AutoEndBranchTypeEnum.MATCHED);
        componentsConnector(groupId, processorEntity.getId(), supervisionId, autoEndBranchTypeEnums);
        ProcessorEntity publishKafkaForPipelineProcessor = createPublishKafkaForPipelineProcessor(config, dto, groupId, 16);
        tableNifiSettingPO.publishKafkaPipelineProcessorId = publishKafkaForPipelineProcessor.getId();
        //连接器
        componentConnector(groupId, evaluateJsons.getId(), publishKafkaForPipelineProcessor.getId(), AutoEndBranchTypeEnum.MATCHED);
        componentsConnector(groupId, publishKafkaForPipelineProcessor.getId(), supervisionId, autoEndBranchTypeEnums);
        lastId = processorEntity.getId();
        //res.add(mergeRes);
        res.add(processorEntity1);
        res.add(queryNumbers);
        res.add(numberToJsonRes);
        res.add(evaluateJsons);
        res.add(processorEntity);
        res.add(publishKafkaForPipelineProcessor);


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

            // 创建input_port connection(组)
            String componentInputPortConnectionId = buildPortConnection(groupId,
                    groupId, queryField.getId(), ConnectableDTO.TypeEnum.PROCESSOR,
                    groupId, inputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                    0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION);

            // 创建input_port connection(任务)
            String taskInputPortConnectionId = buildPortConnection(groupEntityId,
                    taskGroupEntityId, inputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                    groupEntityId, tableInputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                    0, PortComponentEnum.TASK_INPUT_PORT_CONNECTION);

            // 创建input_port connection(应用)
//        String appInputPortConnectionId = buildPortConnection(appParentGroupId,
//                appGroupId, tableInputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
//                appParentGroupId, appInputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
//                0, PortComponentEnum.APP_INPUT_PORT_CONNECTION);

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

            tableNifiSettingPO.processorInputPortConnectId = componentInputPortConnectionId;
            tableNifiSettingPO.processorOutputPortConnectId = componentOutputPortConnectionId;
            tableNifiSettingPO.tableInputPortConnectId = taskInputPortConnectionId;
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
        res.add(queryField);
        res.add(consumeKafkaProcessor);
        res.add(jsonRes);
        res.add(evaluateJson);
        res.add(logProcessor);
        res.add(delSqlRes);
        res.add(executeSQLRecord);
        res.addAll(processorEntities);
        if (dto.groupStructureId == null) {
            tableNifiSettingPO.dispatchComponentId = dispatchProcessor.getId();
            tableNifiSettingPO.publishKafkaProcessorId = publishKafkaProcessor.getId();
            res.add(publishKafkaProcessor);
            res.add(dispatchProcessor);
        }
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
        ProcessorEntity convertJsonForSupervision = convertJsonProcessor(groupId, 2, 6);
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

    private String buildSchemaArchitecture(List<String> tableFieldsList, String schemaName) {
        String architecture = "{\"namespace\": \"nifi\",\"name\": \"" + schemaName + "\",\"type\": \"record\",\"fields\": [";
        for (String tableFieldsDTO : tableFieldsList) {
            if (tableFieldsDTO != null && tableFieldsDTO != "") {
                architecture += "{ \"name\": \"" + tableFieldsDTO + "\",\"type\": [\"null\",\"string\"] },";
            }
        }
        architecture = architecture.substring(0, architecture.length() - 1) + "]}";
        return architecture;
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
        if (Objects.equals(dto.type, OlapTableEnum.PHYSICS) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKPHYSICS)) {
            sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
        } else if (Objects.equals(dto.type, OlapTableEnum.FACT) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT)
                || Objects.equals(dto.type, OlapTableEnum.DIMENSION) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
            sourceFieldName = config.modelPublishFieldDTOList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
        }
        String schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);
        data.schemaArchitecture = schemaArchitecture;
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
        executeSQLRecordDTO.name = "executeSQLRecord";
        executeSQLRecordDTO.details = "query_phase";
        executeSQLRecordDTO.groupId = groupId;
        executeSQLRecordDTO.FetchSize = FetchSize;
        executeSQLRecordDTO.maxRowsPerFlowFile = MaxRowsPerFlowFile;
        executeSQLRecordDTO.outputBatchSize = OutputBatchSize;
        //executeSQLRecordDTO.databaseConnectionPoolingService=config.sourceDsConfig.componentId;
        executeSQLRecordDTO.databaseConnectionPoolingService = sourceDbPoolId;
        executeSQLRecordDTO.sqlSelectQuery = config.processorConfig.sourceExecSqlQuery;
        executeSQLRecordDTO.recordwriter = id;
        executeSQLRecordDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(9);
        BusinessResult<ProcessorEntity> res = componentsBuild.buildExecuteSQLRecordProcess(executeSQLRecordDTO);
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
        buildCallDbProcedureProcessorDTO.groupId=groupId;
        buildCallDbProcedureProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(9);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildCallDbProcedureProcess(buildCallDbProcedureProcessorDTO);
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
        if (Objects.equals(dto.type, OlapTableEnum.PHYSICS) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKPHYSICS)) {
            sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.fieldName).collect(Collectors.toList());
        } else if (Objects.equals(dto.type, OlapTableEnum.FACT) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT)
                || Objects.equals(dto.type, OlapTableEnum.DIMENSION) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
            sourceFieldName = config.modelPublishFieldDTOList.stream().map(e -> e.fieldEnName).collect(Collectors.toList());
        }
        String schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);
        buildAvroRecordSetWriterServiceDTO.schemaArchitecture = schemaArchitecture;
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
        if (Objects.equals(dto.type, OlapTableEnum.PHYSICS) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKPHYSICS)) {
            sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
            tableFieldsList = config.targetDsConfig.tableFieldsList;
            for (TableFieldsDTO tableFieldsDTO : tableFieldsList) {
                if (!Objects.equals(tableFieldsDTO.fieldName, tableFieldsDTO.sourceFieldName)) {
                    buildParameter.put("/" + tableFieldsDTO.fieldName, "/" + tableFieldsDTO.sourceFieldName);
                }
            }
            buildParameter.put("/" + tableFieldsList.get(0).fieldName, "/" + tableFieldsList.get(0).sourceFieldName);
        } else if (Objects.equals(dto.type, OlapTableEnum.FACT) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT)
                || Objects.equals(dto.type, OlapTableEnum.DIMENSION) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
            List<ModelPublishFieldDTO> modelPublishFieldDTOS = config.modelPublishFieldDTOList;
            for (ModelPublishFieldDTO modelPublishFieldDTO : modelPublishFieldDTOS) {
                if (!Objects.equals(modelPublishFieldDTO.fieldEnName, modelPublishFieldDTO.sourceFieldName)) {
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
            //至少有一个属性
            buildUpdateRecordDTO.filedMap = buildParameter;
        }
        buildUpdateRecordDTO.recordReader = avroReaderService.data.getId();
        buildUpdateRecordDTO.recordWriter = avroRecordSetWriterService.data.getId();
        buildUpdateRecordDTO.replacementValueStrategy = "record-path-value";
        buildUpdateRecordDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(10);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildUpdateRecord(buildUpdateRecordDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
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
        if (Objects.equals(dto.type, OlapTableEnum.PHYSICS) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKPHYSICS)) {
            sourceFieldName = config.targetDsConfig.tableFieldsList.stream().map(e -> e.fieldName).collect(Collectors.toList());
        } else if (Objects.equals(dto.type, OlapTableEnum.FACT) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKFACT)
                || Objects.equals(dto.type, OlapTableEnum.DIMENSION) || Objects.equals(dto.type, OlapTableEnum.CUSTOMWORKDIMENSION)) {
            sourceFieldName = config.modelPublishFieldDTOList.stream().map(e -> e.fieldEnName).collect(Collectors.toList());
        }
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
        putDatabaseRecordDTO.TableName = "stg_" + config.processorConfig.targetTableName.toLowerCase();
        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTODORIS)) {
            putDatabaseRecordDTO.TableName = config.processorConfig.targetTableName.toLowerCase();
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

    private ProcessorEntity CallDbProcedure(DataAccessConfigDTO config, String groupId, String targetDbPoolId, SynchronousTypeEnum synchronousTypeEnum) {
        BuildCallDbProcedureProcessorDTO callDbProcedureProcessorDTO = new BuildCallDbProcedureProcessorDTO();
        callDbProcedureProcessorDTO.name = "CallDbProcedure";
        callDbProcedureProcessorDTO.details = "insert_phase";
        callDbProcedureProcessorDTO.groupId = groupId;
        String executsql = "";
        config.processorConfig.targetTableName = "stg_" + config.processorConfig.targetTableName;
        String syncMode = syncModeTypeEnum.getNameByValue(config.targetDsConfig.syncMode);
        log.info("同步类型为:" + syncMode + config.targetDsConfig.syncMode);
        executsql = componentsBuild.assemblySql(config, synchronousTypeEnum, FuncNameEnum.PG_DATA_STG_TO_ODS_TOTAL.getName());
        //callDbProcedureProcessorDTO.dbConnectionId=config.targetDsConfig.componentId;
        callDbProcedureProcessorDTO.dbConnectionId = targetDbPoolId;
        callDbProcedureProcessorDTO.executsql = executsql;
        callDbProcedureProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(12);
        callDbProcedureProcessorDTO.haveNextOne = true;
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildCallDbProcedureProcess(callDbProcedureProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }


    private ProcessorEntity queryNumbersProcessor(BuildNifiFlowDTO dto, DataAccessConfigDTO config, String groupId, String targetDbPoolId) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Query numbers Field";
        querySqlDto.details = "insert_phase";
        querySqlDto.groupId = groupId;
        if(Objects.equals(dto.type,OlapTableEnum.WIDETABLE)){
            querySqlDto.querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers ,now() as end_time from " + config.processorConfig.targetTableName;
        }else{
            querySqlDto.querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers ,to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss') as end_time from " + config.processorConfig.targetTableName;
        }

        querySqlDto.dbConnectionId = targetDbPoolId;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(13);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<String>());
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
        executsql1 += "WHERE\n" +
                "\tid=(SELECT a.mid FROM(SELECT MAX(id) as mid from tb_etl_log where tablename='" + config.targetDsConfig.targetTableName.toLowerCase() + "') a );\n";
        executsql1 += "update tb_etl_Incremental l1 INNER JOIN tb_etl_Incremental l2 on l1.id=l2.id set l1.incremental_objectivescore_end='${" + NifiConstants.AttrConstants.START_TIME +
                "}' ,l1.incremental_objectivescore_start=ifnull(l2.incremental_objectivescore_end,'1970-01-01 00:00:00'), l1.enable_flag=2 " +
                "where l1.object_name = '" + config.targetDsConfig.targetTableName + "' ;";
        callDbProcedureProcessorDTO.dbConnectionId = cfgDbPoolId;
        callDbProcedureProcessorDTO.executsql = executsql1;
        callDbProcedureProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(16);
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
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(15);
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
    private ProcessorEntity execDeleteSqlProcessor(DataAccessConfigDTO config, String groupId, String targetDbPoolId, SynchronousTypeEnum synchronousTypeEnum) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Exec Target Delete";
        querySqlDto.details = "query_phase";
        querySqlDto.groupId = groupId;
        querySqlDto.querySql = componentsBuild.assemblySql(config, synchronousTypeEnum, FuncNameEnum.PG_DATA_STG_TO_ODS_DELETE.getName());
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
     * 创建变量组件
     *
     * @param groupId 组id
     * @return 组件对象
     */
    private ProcessorEntity evaluateJsonPathProcessor(String groupId) {
        ArrayList<String> strings = new ArrayList<>();
        //strings.add(NifiConstants.AttrConstants.INCREMENT_START);
        strings.add(NifiConstants.AttrConstants.START_TIME);
        //strings.add(NifiConstants.AttrConstants.START_TIME);
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
            querySqlDto.querySql = buildIncrementSql(config.processorConfig.targetTableName + OlapTableEnum.KPI.getName());
        }
        querySqlDto.dbConnectionId = cfgDbPoolId;
        querySqlDto.scheduleExpression = config.processorConfig.scheduleExpression;
        querySqlDto.scheduleType = config.processorConfig.scheduleType;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(4);
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
    private ProcessorEntity createConsumeKafkaProcessor(DataAccessConfigDTO configDTO, BuildNifiFlowDTO dto, String groupId) {
        BuildConsumeKafkaProcessorDTO buildConsumeKafkaProcessorDTO = new BuildConsumeKafkaProcessorDTO();
        buildConsumeKafkaProcessorDTO.name = "ConsumeKafka";
        buildConsumeKafkaProcessorDTO.details = "query_phase";
        buildConsumeKafkaProcessorDTO.groupId = groupId;
        //管道id
        buildConsumeKafkaProcessorDTO.GroupID = "dmp.nifi.datafactory.pipeline";
        buildConsumeKafkaProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(3);
        Map<String, String> variable = new HashMap<>();
        variable.put(ComponentIdTypeEnum.KAFKA_BROKERS.getName(), KafkaBrokers);
        componentsBuild.buildNifiGlobalVariable(variable);
        buildConsumeKafkaProcessorDTO.KafkaBrokers = "${" + ComponentIdTypeEnum.KAFKA_BROKERS.getName() + "}";
        TableTopicDTO tableTopicDTO = new TableTopicDTO();
        tableTopicDTO.topicType = TopicTypeEnum.NO_TYPE.getValue();
        tableTopicDTO.tableId = Math.toIntExact(dto.id);
        tableTopicDTO.tableType = dto.type.getValue();
        List<TableTopicDTO> tableTopicList = tableTopicService.getTableTopicList(tableTopicDTO);
        buildConsumeKafkaProcessorDTO.TopicNames = tableTopicList.stream().map(e -> e.topicName).collect(Collectors.joining(" , "));
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
    public ProcessorEntity createPublishKafkaProcessor(DataAccessConfigDTO configDTO, BuildNifiFlowDTO dto, String groupId, int position) {
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
        String[] s = targetTableName.split("_");
        String s1 = targetTableName.replaceFirst(s[0] + "_", "");

        //更新topic
        TableTopicDTO tableTopicDTO = new TableTopicDTO();
        tableTopicDTO.tableId = Math.toIntExact(dto.id);
        tableTopicDTO.tableType = dto.type.getValue();
        tableTopicDTO.topicName = "dmp.datafactory.nifi." + s[0] + "." + s1;
        tableTopicDTO.topicType = TopicTypeEnum.DAILY_NIFI_FLOW.getValue();
        if (Objects.equals(dto.type, OlapTableEnum.KPI)) {
            tableTopicDTO.topicName = "dmp.datafactory.nifi." + s[0] + "." + s1 + OlapTableEnum.KPI.getName();
        }
        tableTopicService.updateTableTopic(tableTopicDTO);
        buildPublishKafkaProcessorDTO.TopicName = tableTopicDTO.topicName;
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildPublishKafkaProcessor(buildPublishKafkaProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }


    /**
     * createPublishKafkaForPipelineProcessor
     *
     * @param configDTO 数据接入配置
     * @param groupId   组id
     * @param dto
     * @return 组件对象
     */
    public ProcessorEntity createPublishKafkaForPipelineProcessor(DataAccessConfigDTO configDTO, BuildNifiFlowDTO dto, String groupId, int position) {
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
        buildPublishKafkaProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(position);
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
    private ProcessorEntity queryDispatchProcessor(DataAccessConfigDTO config, String groupId, String cfgDbPoolId) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "queryDispatchProcessor";
        querySqlDto.details = "queryDispatchProcessor";
        querySqlDto.groupId = groupId;
        querySqlDto.querySql = buildIncrementSql(config.processorConfig.targetTableName);
        querySqlDto.dbConnectionId = cfgDbPoolId;
        /*querySqlDto.scheduleExpression = config.processorConfig.scheduleExpression;
        querySqlDto.scheduleType = config.processorConfig.scheduleType;*/
        querySqlDto.scheduleExpression = "3600";
        querySqlDto.scheduleType = SchedulingStrategyTypeEnum.TIMER;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(1);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<String>());
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    private ProcessorEntity queryForPipelineSupervision(String groupId, String cfgDbPoolId) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "queryForPipelineSupervision";
        querySqlDto.details = "queryForPipelineSupervision";
        querySqlDto.groupId = groupId;
        querySqlDto.querySql = "select '${executesql.error.message}' as message,'${kafka.topic}' as topic,'" + groupId + "' as groupId";
        querySqlDto.dbConnectionId = cfgDbPoolId;
        querySqlDto.scheduleExpression = "1";
        querySqlDto.scheduleType = SchedulingStrategyTypeEnum.TIMER;
        querySqlDto.positionDTO = NifiPositionHelper.buildXYPositionDTO(1, 6);
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
        str.append(" now() ").append(" as ").append(NifiConstants.AttrConstants.START_TIME).append(" ");
        //str.append(NifiConstants.AttrConstants.CREATE_TIME).append(" as ").append(NifiConstants.AttrConstants.START_TIME);
        str.append(" from ").append(NifiConstants.AttrConstants.INCREMENT_DB_TABLE_NAME);
        str.append(" where object_name = '").append(targetDbName).append("'");
        return str.toString();
    }

    /**
     * 创建日志sql
     *
     * @return sql
     */
    private String buildLogSql(BuildNifiFlowDTO dto, String selectSql) {
        String filedValues = "";
        filedValues += dto.queryStartTime == null ? ",'0000-01-01 00:00:00'" : (",'" + dto.queryStartTime + "'");
        filedValues += dto.queryEndTime == null ? ",now()" : (",'" + dto.queryEndTime + "'");
        if (dto.selectSql != null && dto.selectSql != "") {
            filedValues += ",'" + dto.selectSql + "'";
        } else {
            filedValues += ",'" + selectSql + "'";
        }

        return "INSERT INTO tb_etl_log ( tablename, startdate, `status`,query_start_time,query_end_time,query_sql) " +
                "VALUES ('" + dto.tableName + "', '${" + NifiConstants.AttrConstants.START_TIME + "}', 1" + filedValues + ")";
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

}
