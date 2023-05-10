package com.fisk.task.listener.nifi.impl;


import com.alibaba.fastjson.JSON;
import com.davis.client.ApiException;
import com.davis.client.model.*;
import com.davis.client.model.ProcessorRunStatusEntity;
import com.fisk.common.core.baseObject.entity.BusinessResult;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.constants.NifiConstants;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.core.enums.task.nifi.AutoEndBranchTypeEnum;
import com.fisk.common.core.enums.task.nifi.DbPoolTypeEnum;
import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.dataaccess.enums.ComponentIdTypeEnum;
import com.fisk.dataaccess.enums.DeltaTimeParameterTypeEnum;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import com.fisk.dataaccess.enums.syncModeTypeEnum;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.mdm.client.MdmClient;
import com.fisk.mdm.dto.accessmodel.AccessPublishStatusDTO;
import com.fisk.mdm.enums.ImportTypeEnum;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.accessmdm.AccessAttributeDTO;
import com.fisk.task.dto.mdmconfig.*;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.mdmconfig.AccessMdmConfigDTO;
import com.fisk.task.dto.mdmtask.BuildMdmNifiFlowDTO;
import com.fisk.task.dto.nifi.*;
import com.fisk.task.dto.task.TableTopicDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.enums.PortComponentEnum;
import com.fisk.task.listener.nifi.IAccessMdmNifiTaskListener;
import com.fisk.task.listener.postgre.datainput.IbuildTable;
import com.fisk.task.listener.postgre.datainput.impl.BuildFactoryHelper;
import com.fisk.task.po.app.NifiConfigPO;
import com.fisk.task.po.mdm.MdmNifiSettingPO;
import com.fisk.task.po.mdm.MdmTableNifiSettingPO;
import com.fisk.task.service.nifi.IMdmTableNifiSettingService;
import com.fisk.task.service.nifi.impl.MdmNifiSettingServiceImpl;
import com.fisk.task.service.pipeline.ITableTopicService;
import com.fisk.task.service.pipeline.impl.NifiConfigServiceImpl;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.NifiPositionHelper;
import com.fisk.task.utils.StackTraceHelper;
import com.fisk.task.utils.nifi.INiFiHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wangjian
 */
@Component
@Slf4j

public class BuildAccessMdmNifiTaskListener implements IAccessMdmNifiTaskListener {

    @Value("${fiData-data-ods-source}")
    private String dataSourceOdsId;
    @Value("${nifi-ConcurrentTasks}")
    public String ConcurrentTasks;
    @Value("${fiData-data-mdm-source}")
    private String dataTargetMdmId;

    @Value("${nifi-MaxRowsPerFlowFile}")
    public String MaxRowsPerFlowFile;
    @Value("${nifi-OutputBatchSize}")
    public String OutputBatchSize;
    @Value("${nifi-FetchSize}")
    public String FetchSize;
    @Value("${spring.kafka.producer.bootstrap-servers}")
    public String KafkaBrokers;

    @Value("${nifi.pipeline.topicName}")
    public String pipelineTopicName;
    @Resource
    UserClient userClient;
    @Resource
    MdmClient mdmClient;

    @Resource
    MdmNifiSettingServiceImpl mdmNifiSettingService;
    @Resource
    NifiConfigServiceImpl nifiConfigService;

    @Resource
    INiFiHelper componentsBuild;

    @Resource
    IMdmTableNifiSettingService mdmTableNifiSettingService;
    @Resource
    PublishTaskController pc;

    @Resource
    private ITableTopicService tableTopicService;


    public String mdmParentGroupId;
    public String mdmGroupId;
    public String groupEntityId;
    public String taskGroupEntityId;
    public String mdmInputPortId;
    public String tableInputPortId;
    public String mdmOutputPortId;
    public String tableOutputPortId;
    @Override
    public ResultEnum accessMdmMsg(String dataInfo, Acknowledgment acke) {
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        AccessPublishStatusDTO accessPublishStatusDTO = new AccessPublishStatusDTO();
        accessPublishStatusDTO.publish = 1;

        log.info("创建nifi流程发布参数:" + dataInfo);
        dataInfo = "[" + dataInfo + "]";
        List<BuildMdmNifiFlowDTO> buildNifiFlows = JSON.parseArray(dataInfo, BuildMdmNifiFlowDTO.class);
        BuildMdmNifiFlowDTO dto = new BuildMdmNifiFlowDTO();
        String subRunId = "";
        try {
            for (BuildMdmNifiFlowDTO buildNifiFlow : buildNifiFlows) {
                accessPublishStatusDTO.setId(buildNifiFlow.accessId.intValue());
                dto = buildNifiFlow;
                //获取数据接入配置项
                AccessMdmConfigDTO configDTO = getConfigData(dto.accessId, dto.modelId, dto.entityId, dto.synchronousTypeEnum, dto.type, dto.dataClassifyEnum, dto.tableName, dto.selectSql, dto);
                if (configDTO == null) {
                    log.error("数据接入配置项获取失败。modelId: 【" + dto.modelId + "】, entityId: 【" + dto.entityId + "】");
                    return ResultEnum.NOTFOUND;
                }
                MdmNifiSettingPO mdmNifiSettingPO = new MdmNifiSettingPO();
                MdmNifiSettingPO mdmNifiSettingPO1 = new MdmNifiSettingPO();
                if (dto.nifiCustomWorkflowId != null) {
                    mdmNifiSettingPO1 = mdmNifiSettingService.query().eq("model_id", dto.modelId).eq("nifi_custom_workflow_id", dto.nifiCustomWorkflowId).eq("type", dto.dataClassifyEnum.getValue()).eq("del_flag", 1).one();

                } else {
                    List<MdmNifiSettingPO> list = mdmNifiSettingService.query().eq("model_id", dto.modelId).eq("type", dto.dataClassifyEnum.getValue()).eq("del_flag", 1).list();
                    if (list != null && list.size() != 0) {
                        for (MdmNifiSettingPO mdmNifiSettingPO2 : list) {
                            if (mdmNifiSettingPO2.nifiCustomWorkflowId == null) {
                                mdmNifiSettingPO1 = mdmNifiSettingPO2;
                            }
                        }
                    }

                }
                if (mdmNifiSettingPO1 != null) {
                    mdmNifiSettingPO = mdmNifiSettingPO1;
                }
                log.info("【数据接入配置项参数】" + JSON.toJSONString(configDTO));
                //1. 获取数据接入配置库连接池
                ControllerServiceEntity cfgDbPool = buildCfgDsPool(configDTO);

                //2. 创建应用组
                ProcessGroupEntity groupEntity = buildMdmGroup(configDTO, dto.groupComponentId);
                mdmNifiSettingPO.modelId = String.valueOf(dto.modelId);
                mdmNifiSettingPO.modelComponentId = groupEntity.getId();
                mdmNifiSettingPO.type = dto.dataClassifyEnum.getValue();
                mdmGroupId = groupEntity.getId();
                mdmParentGroupId = groupEntity.getComponent().getParentGroupId();
                if (dto.groupStructureId != null) {
                    mdmGroupId = dto.groupStructureId;
                }
                List<ControllerServiceEntity> dbPool = buildDsConnectionPool(dto.synchronousTypeEnum, configDTO, mdmGroupId, dto);
                String sourceId = "";
                if (!dto.excelFlow) {

                    sourceId = dbPool.get(0).getId();
                }


                //4. 创建任务组创建时要把原任务组删掉,防止重复发布带来影响  dto.id, dto.appId
                DataModelVO dataModelVO = new DataModelVO();
                dataModelVO.dataClassifyEnum = dto.dataClassifyEnum;
                dataModelVO.delBusiness = false;
                dataModelVO.businessId = String.valueOf(dto.modelId);
                dataModelVO.userId = dto.userId;
                DataModelTableVO dataModelTableVO = new DataModelTableVO();
                dataModelTableVO.type = dto.type;
                List<Long> ids = new ArrayList<>();
                ids.add(dto.entityId);
                dataModelTableVO.ids = ids;
                dataModelVO.physicsIdList = dataModelTableVO;
                MdmTableNifiSettingPO tableNifiSettingPO = new MdmTableNifiSettingPO();
                if (dto.workflowDetailId != null) {
                    tableNifiSettingPO = mdmTableNifiSettingService.query().eq("model_id", dto.modelId).eq("nifi_custom_workflow_detail_id", dto.workflowDetailId).eq("entity_id", dto.entityId).eq("type", dto.type.getValue()).one();

                } else {
                    tableNifiSettingPO = mdmTableNifiSettingService.query().eq("model_id", dto.modelId).eq("entity_id", dto.entityId).eq("type", dto.type.getValue()).one();

                }
                if (tableNifiSettingPO != null && tableNifiSettingPO.tableComponentId != null) {
                    componentsBuild.deleteMdmNifiFlow(dataModelVO);
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

                List<ProcessorEntity> processors = buildMdmProcessor(groupEntity.getId(), configDTO, taskGroupEntity.getId(), sourceId, dbPool.get(1).getId(), cfgDbPool.getId(), mdmNifiSettingPO, dto);
                enabledProcessor(taskGroupEntity.getId(), processors);
                //7. 如果是接入,同步一次,然后把调度组件停掉
                if (dto.groupStructureId == null && dto.openTransmission) {
                    String topicName = MqConstants.TopicPrefix.TOPIC_MDM_PREFIX + dto.type.getValue() + "." + dto.modelId + "." + dto.id;
                    int value = TopicTypeEnum.MDM_NIFI_FLOW.getValue();
                    if (Objects.equals(value, OlapTableEnum.KPI)) {
                        topicName = MqConstants.TopicPrefix.TOPIC_MDM_PREFIX + OlapTableEnum.KPI.getValue() + "." + dto.modelId + "." + dto.id;
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
                    kafkaRkeceiveDTO.topicType = TopicTypeEnum.MDM_NIFI_FLOW.getValue();
                    kafkaRkeceiveDTO.traceId = dto.traceId;
                    kafkaRkeceiveDTO.userId = dto.userId;
                    pc.taskPublish(kafkaRkeceiveDTO);
                }
                if (Objects.equals(dto.synchronousTypeEnum, SynchronousTypeEnum.ODSTOMDM)) {
                    accessPublishStatusDTO.subRunId = subRunId;
                    accessPublishStatusDTO.tableHistoryId = dto.tableHistoryId;
                    mdmClient.updateAccessPublishState(accessPublishStatusDTO);
                }
                //7. 回写id
                savaNifiConfig(cfgDbPool.getId(), ComponentIdTypeEnum.CFG_DB_POOL_COMPONENT_ID);
            }
            return resultEnum;
        } catch (Exception e) {
            resultEnum = ResultEnum.ERROR;
            if (Objects.equals(dto.synchronousTypeEnum, SynchronousTypeEnum.ODSTOMDM)) {
                accessPublishStatusDTO.publish = 3;
                accessPublishStatusDTO.publishErrorMsg = StackTraceHelper.getStackTraceInfo(e);
                accessPublishStatusDTO.tableHistoryId = dto.tableHistoryId;
                accessPublishStatusDTO.subRunId = subRunId;
                mdmClient.updateAccessPublishState(accessPublishStatusDTO);
            }
            log.error("nifi流程创建失败" + StackTraceHelper.getStackTraceInfo(e));
            return resultEnum;
        } finally {
            if (acke != null) {
                acke.acknowledge();
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


    private ProcessorEntity createUpdateField1(String appGroupId, AccessMdmConfigDTO config, String groupId, BuildMdmNifiFlowDTO dto, MdmTableNifiSettingPO mdmTableNifiSettingPO) {
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
        sourceFieldName = config.mdmPublishFieldDTOList.stream().map(e -> e.fieldName).collect(Collectors.toList());
        if (StringUtils.isNotEmpty(dto.generateVersionSql)) {
            sourceFieldName.add(NifiConstants.AttrConstants.FI_VERSION);
        }
        sourceFieldName.add("fidata_batch_code");
        sourceFieldName.add("fidata_flow_batch_code");
        sourceFieldName.add("fidata_version_id");
        sourceFieldName.add("fidata_import_type");
        sourceFieldName.add("fidata_create_time");
        sourceFieldName.add("fidata_create_user");
        sourceFieldName.add("fidata_update_time");
        sourceFieldName.add("fidata_update_user");
        sourceFieldName.add("fidata_del_flag");

        String schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);
        buildAvroRecordSetWriterServiceDTO.schemaArchitecture = schemaArchitecture;
        buildAvroRecordSetWriterServiceDTO.schemaWriteStrategy = "avro-embedded";
        buildAvroRecordSetWriterServiceDTO.schemaAccessStrategy = "schema-text-property";
        BusinessResult<ControllerServiceEntity> avroRecordSetWriterService = componentsBuild.buildAvroRecordSetWriterService(buildAvroRecordSetWriterServiceDTO);
        mdmTableNifiSettingPO.convertAvroRecordSetWriterForCodeId = avroRecordSetWriterService.data.getId();
        //--------------------------------------
        BuildAvroReaderServiceDTO buildAvroReaderServiceDTO = new BuildAvroReaderServiceDTO();
        buildAvroReaderServiceDTO.details = "transition_phase";
        buildAvroReaderServiceDTO.name = "PutDatabaseRecordTransition";
        if (groupStructureId != null) {
            buildAvroReaderServiceDTO.groupId = groupStructureId;
        } else {
            buildAvroReaderServiceDTO.groupId = appGroupId;
        }
        schemaArchitecture = buildSchemaArchitecture(sourceFieldName.subList(0, sourceFieldName.size() - 9), config.processorConfig.targetTableName);

        buildAvroReaderServiceDTO.schemaText = schemaArchitecture;
        BusinessResult<ControllerServiceEntity> avroReaderService = componentsBuild.buildAvroReaderService(buildAvroReaderServiceDTO);
        mdmTableNifiSettingPO.convertPutDatabaseRecordForCodeId = avroReaderService.data.getId();
        //--------------------------------------
        buildUpdateRecordDTO.groupId = groupId;
        buildUpdateRecordDTO.details = "transition_phase";
        buildUpdateRecordDTO.name = "UpdateRecord";

        //至少有一个属性
        //nifi的三元运算,如果pipelTraceId是空的,取pipelTaskTraceId当作fidata_batch_code的值
        buildParameter.put("/fidata_batch_code", "${fidata_batch_code}");
        buildParameter.put("/fidata_flow_batch_code", "${fragment.index}");
        buildParameter.put("/fidata_version_id", String.valueOf(dto.versionId));
        buildParameter.put("/fidata_import_type", String.valueOf(ImportTypeEnum.NIFI_SYNC.getValue()));
        buildParameter.put("/fidata_create_time", "${now():format('yyyy-MM-dd HH:mm:ss')}");
        buildParameter.put("/fidata_create_user", String.valueOf(dto.userId));
        buildParameter.put("/fidata_update_time", "${now():format('yyyy-MM-dd HH:mm:ss')}");
        buildParameter.put("/fidata_update_user", String.valueOf(dto.userId));
        buildParameter.put("/fidata_del_flag", "1");
        buildUpdateRecordDTO.filedMap = buildParameter;

        buildUpdateRecordDTO.recordReader = avroReaderService.data.getId();
        buildUpdateRecordDTO.recordWriter = avroRecordSetWriterService.data.getId();
        buildUpdateRecordDTO.replacementValueStrategy = "literal-value";
        buildUpdateRecordDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(10);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildUpdateRecord(buildUpdateRecordDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }
    /**
     * 获取数据接入的配置
     *
     * @param modelId 配置的id
     * @return 数据接入配置
     */
    private AccessMdmConfigDTO getConfigData(long accessId, long modelId, long entityId, SynchronousTypeEnum synchronousTypeEnum, OlapTableEnum type, DataClassifyEnum dataClassifyEnum, String tableName, String selectSql, BuildMdmNifiFlowDTO buildMdmNifiFlowDTO) {
        AccessMdmConfigDTO data = new AccessMdmConfigDTO();
        GroupConfig groupConfig = new GroupConfig();
        DataSourceConfig targetDbPoolConfig = new DataSourceConfig();
        DataSourceConfig sourceDsConfig = new DataSourceConfig();
        TaskGroupConfig taskGroupConfig = new TaskGroupConfig();
        ProcessorConfig processorConfig = new ProcessorConfig();
        ResultEntity<AccessMdmConfigDTO> res = new ResultEntity<>();
        List<AccessAttributeDTO> fieldDetails = new ArrayList<>();
        if (synchronousTypeEnum == SynchronousTypeEnum.ODSTOMDM) {
                res = mdmClient.dataAccessConfig(entityId,modelId);
                if (res.code != ResultEnum.SUCCESS.getCode()) {
                    return null;
                }
                if (res.data != null) {
                    data = res.data;
                }
        }
        ResultEntity<List<AccessAttributeDTO>> accessAttributeField = mdmClient.getAccessAttributeField((int) accessId, (int) entityId);
        if (accessAttributeField.code != ResultEnum.SUCCESS.getCode()){
            log.error("mdmClient无法查询到字段信息");
            throw new FkException(ResultEnum.ERROR);
        }
        fieldDetails = accessAttributeField.getData();
        //拿出来
        MdmNifiSettingPO mdmNifiSettingPO = new MdmNifiSettingPO();
        if (buildMdmNifiFlowDTO.nifiCustomWorkflowId != null) {
            mdmNifiSettingPO = mdmNifiSettingService.query().eq("model_id", modelId).eq("nifi_custom_workflow_id", buildMdmNifiFlowDTO.nifiCustomWorkflowId).eq("type", dataClassifyEnum.getValue()).eq("del_flag", 1).one();

        } else {
            List<MdmNifiSettingPO> list = mdmNifiSettingService.query().eq("model_id", modelId).eq("type", dataClassifyEnum.getValue()).eq("del_flag", 1).list();
            if (list != null && list.size() != 0) {
                for (MdmNifiSettingPO mdmNifiSettingPO2 : list) {
                    if (mdmNifiSettingPO2.nifiCustomWorkflowId == null) {
                        mdmNifiSettingPO = mdmNifiSettingPO2;
                    }
                }
            }
        }
        NifiConfigPO nifiConfigPO = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.CFG_DB_POOL_COMPONENT_ID.getName()).one();
        if (res.data != null && mdmNifiSettingPO != null && mdmNifiSettingPO.modelComponentId != null) {
            data.groupConfig.newApp = false;
        } else {
            if (data != null && data.groupConfig != null) {
                data.groupConfig.newApp = true;
            }
        }

        if (mdmNifiSettingPO != null) {
            if (data.sourceDsConfig != null) {
                data.sourceDsConfig.componentId = mdmNifiSettingPO.sourceDbPoolComponentId;
                data.targetDsConfig.componentId = mdmNifiSettingPO.targetDbPoolComponentId;
                data.groupConfig.componentId = mdmNifiSettingPO.modelComponentId;
            } else {
                //赋值对象
                sourceDsConfig.componentId = mdmNifiSettingPO.sourceDbPoolComponentId;
                targetDbPoolConfig.componentId = mdmNifiSettingPO.targetDbPoolComponentId;
                groupConfig.componentId = mdmNifiSettingPO.modelComponentId;
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
                data.cfgDsConfig.componentId = nifiConfigPO.componentId;
            }

        }

        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.ODSTOMDM)) {
            if (mdmNifiSettingPO != null && mdmNifiSettingPO.modelComponentId != null) {
                groupConfig.newApp = false;
                groupConfig.componentId = mdmNifiSettingPO.modelComponentId;
            } else {
                groupConfig.newApp = true;
            }
            groupConfig.appName = buildMdmNifiFlowDTO.modelName;
            groupConfig.appDetails = tableName;
            if (nifiConfigPO != null){
                data.cfgDsConfig.componentId = nifiConfigPO.componentId;
            }
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

            ResultEntity<DataSourceDTO> fiDataDataDwSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataTargetMdmId));
            if (fiDataDataDwSource.code == ResultEnum.SUCCESS.getCode()) {
                DataSourceDTO dwData = fiDataDataDwSource.data;
                targetDbPoolConfig.type = DriverTypeEnum.valueOf(dwData.conType.getName());
                targetDbPoolConfig.user = dwData.conAccount;
                targetDbPoolConfig.password = dwData.conPassword;
                targetDbPoolConfig.jdbcStr = dwData.conStr;
            } else {
                log.error("userclient无法查询到mdm库的连接信息");
                throw new FkException(ResultEnum.ERROR);
            }

            targetDbPoolConfig.targetTableName = tableName;
            targetDbPoolConfig.tableFieldsList = null;
            targetDbPoolConfig.syncMode = buildMdmNifiFlowDTO.synMode;
            data.groupConfig = groupConfig;
            data.taskGroupConfig = taskGroupConfig;
            data.processorConfig = processorConfig;
            data.sourceDsConfig = sourceDsConfig;
            data.targetDsConfig = targetDbPoolConfig;
            data.mdmPublishFieldDTOList = fieldDetails;

        }
        return data;
    }

    /**
     * 获取/创建数据接入配置库的连接池
     *
     * @param config 数据接入配置
     * @return 组件实体
     */
    private ControllerServiceEntity buildCfgDsPool(AccessMdmConfigDTO config) {
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
     * 创建控制器服务对象
     *
     * @param config  数据接入配置
     * @param groupId 组id
     * @param type    数据源类型
     * @return dto
     */
    private BuildDbControllerServiceDTO buildDbControllerServiceDTO(AccessMdmConfigDTO config, String groupId, DbPoolTypeEnum type, SynchronousTypeEnum synchronousTypeEnum) {
        DataSourceConfig dsConfig;
        String name;
        BuildDbControllerServiceDTO dto = new BuildDbControllerServiceDTO();
        HashMap<String, String> configMap = new HashMap<>();
        switch (type) {
            case SOURCE:
                dsConfig = config.sourceDsConfig;
                name = "Source Data DB Connection";
                if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.ODSTOMDM)) {
                    dto.conUrl = ComponentIdTypeEnum.PG_MDM_DB_POOL_URL.getName();
                    dto.user = ComponentIdTypeEnum.PG_MDM_DB_POOL_USERNAME.getName();
                    dto.pwd = ComponentIdTypeEnum.PG_MDM_DB_POOL_PASSWORD.getName();
                }
                break;
            case TARGET:
                dsConfig = config.targetDsConfig;
                name = "Target Data DB Connection";
                dto.conUrl = ComponentIdTypeEnum.PG_ODS_DB_POOL_URL.getName();
                dto.user = ComponentIdTypeEnum.PG_ODS_DB_POOL_USERNAME.getName();
                dto.pwd = ComponentIdTypeEnum.PG_ODS_DB_POOL_PASSWORD.getName();
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
            default:
                break;
        }
        return dto;
    }

    /**
     * 创建app组
     *
     * @param config 数据接入配置
     * @return 组信息
     */
    private ProcessGroupEntity buildMdmGroup(AccessMdmConfigDTO config, String groupComponentId) {
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
                NifiConfigPO nifiConfigPO = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.MDM_NIFI_FLOW_GROUP_ID.getName()).one();
                if (nifiConfigPO != null) {
                    dto.groupId = nifiConfigPO.componentId;
                } else {
                    BuildProcessGroupDTO buildProcessGroupDTO = new BuildProcessGroupDTO();
                    buildProcessGroupDTO.name = ComponentIdTypeEnum.MDM_NIFI_FLOW_GROUP_ID.getName();
                    buildProcessGroupDTO.details = ComponentIdTypeEnum.MDM_NIFI_FLOW_GROUP_ID.getName();
                    int groupCount = componentsBuild.getGroupCount(NifiConstants.ApiConstants.ROOT_NODE);
                    buildProcessGroupDTO.positionDTO = NifiPositionHelper.buildXPositionDTO(groupCount);
                    BusinessResult<ProcessGroupEntity> processGroupEntityBusinessResult = componentsBuild.buildProcessGroup(buildProcessGroupDTO);
                    if (processGroupEntityBusinessResult.success) {
                        dto.groupId = processGroupEntityBusinessResult.data.getId();
                        NifiConfigPO nifiConfigPO1 = new NifiConfigPO();
                        nifiConfigPO1.componentId = dto.groupId;
                        nifiConfigPO1.componentKey = ComponentIdTypeEnum.MDM_NIFI_FLOW_GROUP_ID.getName();
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
     * 创建数据库连接池
     *
     * @param config 数据接入配置
     * @return 控制器服务对象
     */
    private List<ControllerServiceEntity> buildDsConnectionPool(SynchronousTypeEnum synchronousTypeEnum, AccessMdmConfigDTO config,
                                                                String groupId, BuildMdmNifiFlowDTO buildMdmNifiFlowDTO) {
        List<ControllerServiceEntity> list = new ArrayList<>();
        NifiConfigPO nifiConfigPo = new NifiConfigPO();
        NifiConfigPO nifiSourceConfigPo = null;
        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.ODSTOMDM)) {
            nifiConfigPo = nifiConfigService.query().eq("datasource_config_id", buildMdmNifiFlowDTO.targetDbId).one();
            nifiSourceConfigPo = nifiConfigService.query().eq("datasource_config_id", buildMdmNifiFlowDTO.dataSourceDbId).one();
        }
        BusinessResult<ControllerServiceEntity> targetRes = new BusinessResult<>(true, "控制器服务创建成功");
        BusinessResult<ControllerServiceEntity> sourceRes = new BusinessResult<>(true, "控制器服务创建成功");
        if (config.groupConfig.newApp || Objects.equals(buildMdmNifiFlowDTO.dataClassifyEnum, DataClassifyEnum.MDM_DATA_ACCESS)) {
            if (nifiConfigPo != null) {
                ControllerServiceEntity data = new ControllerServiceEntity();
                data.setId(nifiConfigPo.componentId);
                targetRes.data = data;
            } else {
                // 统一数据源改造
                String componentId = saveDbconfig(buildMdmNifiFlowDTO.targetDbId);
                ControllerServiceEntity entity = new ControllerServiceEntity();
                entity.setId(componentId);
                targetRes.data = entity;
            }
            //来源库
            if (!buildMdmNifiFlowDTO.excelFlow) {
                if (nifiSourceConfigPo != null) {
                    ControllerServiceEntity data = new ControllerServiceEntity();
                    data.setId(nifiSourceConfigPo.componentId);
                    sourceRes.data = data;
                } else {
                    // 统一数据源改造
                    String componentId = saveDbconfig(buildMdmNifiFlowDTO.dataSourceDbId);
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
            if (!buildMdmNifiFlowDTO.excelFlow) {
                String componentId = saveDbconfig(buildMdmNifiFlowDTO.dataSourceDbId);
                ControllerServiceEntity entity = new ControllerServiceEntity();
                entity.setId(componentId);
                sourceControllerService = entity;
            }
            String componentId = saveDbconfig(buildMdmNifiFlowDTO.targetDbId);
            ControllerServiceEntity entity = new ControllerServiceEntity();
            entity.setId(componentId);
            ControllerServiceEntity targetResControllerService = entity;
            list.add(sourceControllerService);
            list.add(targetResControllerService);
            return list;
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
     * 创建任务组
     *
     * @param config 数据接入配置
     * @return 组信息
     */
    private ProcessGroupEntity buildTaskGroup(AccessMdmConfigDTO config, String groupId) {
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

    private List<ProcessorEntity> buildMdmProcessor(String appGroupId, AccessMdmConfigDTO config, String groupId, String sourceDbPoolId, String targetDbPoolId, String cfgDbPoolId, MdmNifiSettingPO mdmNifiSettingPO, BuildMdmNifiFlowDTO dto) {
        List<ProcessorEntity> res = new ArrayList<>();
        SynchronousTypeEnum synchronousTypeEnum = dto.synchronousTypeEnum;
        MdmTableNifiSettingPO mdmTableNifiSettingPO = new MdmTableNifiSettingPO();
        MdmTableNifiSettingPO mdmTableNifiSettingPO1 = new MdmTableNifiSettingPO();
        if (dto.workflowDetailId != null) {
            mdmTableNifiSettingPO1 = mdmTableNifiSettingService.query().eq("model_id", dto.modelId).eq("nifi_custom_workflow_detail_id", dto.workflowDetailId).eq("entity_id", dto.entityId).eq("type", dto.type.getValue()).one();

        } else {
            mdmTableNifiSettingPO1 = mdmTableNifiSettingService.query().eq("model_id", dto.modelId).eq("entity_id", dto.entityId).eq("type", dto.type.getValue()).one();

        }
        if (mdmTableNifiSettingPO1 != null) {
            mdmTableNifiSettingPO = mdmTableNifiSettingPO1;
        }
        mdmTableNifiSettingPO.tableComponentId = groupId;
        mdmTableNifiSettingPO.modelId = Math.toIntExact(dto.modelId);
        mdmTableNifiSettingPO.entityId = Math.toIntExact(dto.entityId);
        mdmTableNifiSettingPO.type = dto.type.getValue();
        mdmTableNifiSettingPO.tableName = config.targetDsConfig.targetTableName;
        //日志监控
        List<AutoEndBranchTypeEnum> autoEndBranchTypeEnums = new ArrayList<>();
        //失败的不连
        autoEndBranchTypeEnums.add(AutoEndBranchTypeEnum.FAILURE);
        List<ProcessorEntity> processorEntities = pipelineSupervision(groupId, res, cfgDbPoolId, mdmTableNifiSettingPO);
        String supervisionId = processorEntities.get(0).getId();
        //调度组件,在数据接入的时候调一次
        String inputPortId = "";
        createPublishKafkaProcessor(config, dto, groupId, 1, false);
        //原变量字段
        ProcessorEntity evaluateJsonPathProcessor = evaluateJsonPathProcessor(groupId);
        mdmTableNifiSettingPO.setIncrementProcessorId = evaluateJsonPathProcessor.getId();
        res.add(evaluateJsonPathProcessor);
        //接受消息ConsumeKafka
        ProcessorEntity consumeKafkaProcessor = createConsumeKafkaProcessor(config, dto, groupId);
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
        mdmTableNifiSettingPO.consumeKafkaProcessorId = consumeKafkaProcessor.getId();
        //读取增量字段组件
        ProcessorEntity queryField = queryIncrementFieldProcessor(config, groupId, cfgDbPoolId, dto);
        componentConnector(groupId, evaluateJsonPathProcessor.getId(), queryField.getId(), AutoEndBranchTypeEnum.MATCHED);
        mdmTableNifiSettingPO.queryIncrementProcessorId = queryField.getId();
        //创建数据转换json组件
        ProcessorEntity jsonRes = convertJsonProcessor(groupId, 0, 4);
        mdmTableNifiSettingPO.convertDataToJsonProcessorId = jsonRes.getId();
        //连接器
        componentConnector(groupId, queryField.getId(), jsonRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //字段转换nifi变量
        List<String> strings = new ArrayList<>();
        strings.add(NifiConstants.AttrConstants.INCREMENTAL_OBJECTIVESCORE_END);
        strings.add(NifiConstants.AttrConstants.INCREMENTAL_OBJECTIVESCORE_START);
        ProcessorEntity evaluateJson = evaluateTimeVariablesProcessor(groupId, strings);
        mdmTableNifiSettingPO.evaluateTimeVariablesProcessorId = evaluateJson.getId();
        res.add(evaluateJson);
        res.add(queryField);
        res.add(jsonRes);
        //连接器
        componentConnector(groupId, jsonRes.getId(), evaluateJson.getId(), AutoEndBranchTypeEnum.SUCCESS);
        //创建log
        ProcessorEntity logProcessor = putLogProcessor(groupId, cfgDbPoolId, dto, config);
        mdmTableNifiSettingPO.putLogToConfigDbProcessorId = logProcessor.getId();
        componentsConnector(groupId, logProcessor.getId(), supervisionId, autoEndBranchTypeEnums);
        res.add(logProcessor);
        //连接器
        // 这里要换,上面是evaluateJson,下面是logProcessor.接下来的组件赋予的变量值会覆盖上面的
        List<ProcessorEntity> processorEntities1 = buildDeltaTimeProcessorEntity(dto.deltaTimes, groupId, sourceDbPoolId, res, mdmTableNifiSettingPO);
        if (CollectionUtils.isEmpty(processorEntities1)) {
            componentConnector(groupId, evaluateJson.getId(), logProcessor.getId(), AutoEndBranchTypeEnum.MATCHED);
        } else {
            componentConnector(groupId, evaluateJson.getId(), processorEntities1.get(0).getId(), AutoEndBranchTypeEnum.MATCHED);
            componentConnector(groupId, processorEntities1.get(processorEntities1.size() - 1).getId(), logProcessor.getId(), AutoEndBranchTypeEnum.MATCHED);
        }

        //创建执行删除组件
        ProcessorEntity delSqlRes = execDeleteSqlProcessor(config, groupId, targetDbPoolId, synchronousTypeEnum, dto);
        res.add(delSqlRes);
        mdmTableNifiSettingPO.executeTargetDeleteProcessorId = delSqlRes.getId();
        componentConnector(groupId, logProcessor.getId(), delSqlRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, delSqlRes.getId(), supervisionId, autoEndBranchTypeEnums);
        //执行查询组件
        ProcessorEntity executeSQLRecord = createExecuteSQLRecord(appGroupId, config, groupId, dto, sourceDbPoolId, mdmTableNifiSettingPO);
        componentConnector(groupId, delSqlRes.getId(), executeSQLRecord.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, executeSQLRecord.getId(), supervisionId, autoEndBranchTypeEnums);
        res.add(executeSQLRecord);
        mdmTableNifiSettingPO.executeSqlRecordProcessorId = executeSQLRecord.getId();
        //字段映射转换
        ProcessorEntity updateField = createUpdateField(appGroupId, config, groupId, dto, mdmTableNifiSettingPO);
        mdmTableNifiSettingPO.updateFieldProcessorId = updateField.getId();
        componentConnector(groupId, executeSQLRecord.getId(), updateField.getId(), AutoEndBranchTypeEnum.SUCCESS);
        res.add(updateField);
        //加批量字段值
        ProcessorEntity updateField1 = createUpdateField1(appGroupId, config, groupId, dto, mdmTableNifiSettingPO);
        componentConnector(groupId, updateField.getId(), updateField1.getId(), AutoEndBranchTypeEnum.SUCCESS);
        mdmTableNifiSettingPO.updateFieldForCodeProcessorId = updateField1.getId();
        res.add(updateField1);
        //数据入库
        ProcessorEntity putDatabaseRecord = createPutDatabaseRecord(appGroupId, config, groupId, dto, targetDbPoolId, synchronousTypeEnum, mdmTableNifiSettingPO);
        mdmTableNifiSettingPO.saveTargetDbProcessorId = putDatabaseRecord.getId();
        res.add(putDatabaseRecord);
        //连接器
        componentsConnector(groupId, putDatabaseRecord.getId(), supervisionId, autoEndBranchTypeEnums);
        componentConnector(groupId, updateField1.getId(), putDatabaseRecord.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, updateField1.getId(), supervisionId, autoEndBranchTypeEnums);
        res.add(updateField);
        //stg同步mdm
        ProcessorEntity syncTableEntity = createSyncTable(config, groupId, targetDbPoolId, dto);
        componentConnector(groupId, putDatabaseRecord.getId(), syncTableEntity.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, syncTableEntity.getId(), supervisionId, autoEndBranchTypeEnums);
        res.add(syncTableEntity);
        mdmTableNifiSettingPO.mdmToStgProcessorId = syncTableEntity.getId();

        String lastId = "";
        //查询条数数据
        ProcessorEntity queryNumbers = queryNumbersProcessor(dto, config, groupId, targetDbPoolId);
        mdmTableNifiSettingPO.queryNumbersProcessorId = queryNumbers.getId();
        res.add(queryNumbers);
        //连接器
        componentConnector(groupId, syncTableEntity.getId(), queryNumbers.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, syncTableEntity.getId(), supervisionId, autoEndBranchTypeEnums);
        //转json
        ProcessorEntity numberToJsonRes = convertJsonProcessor(groupId, 0, 14);
        mdmTableNifiSettingPO.convertNumbersToJsonProcessorId = numberToJsonRes.getId();
        res.add(numberToJsonRes);
        //连接器
        componentConnector(groupId, queryNumbers.getId(), numberToJsonRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, queryNumbers.getId(), supervisionId, autoEndBranchTypeEnums);
        //定义占位符
        ProcessorEntity evaluateJsons = evaluateNumbersProcessor(groupId);
        mdmTableNifiSettingPO.setNumbersProcessorId = evaluateJsons.getId();
        res.add(evaluateJsons);
        //连接器
        componentConnector(groupId, numberToJsonRes.getId(), evaluateJsons.getId(), AutoEndBranchTypeEnum.SUCCESS);
        componentsConnector(groupId, numberToJsonRes.getId(), supervisionId, autoEndBranchTypeEnums);
        //更新日志
        ProcessorEntity updateLogProcessor = updateLogProcedure(config, groupId, cfgDbPoolId);
        mdmTableNifiSettingPO.saveNumbersProcessorId = updateLogProcessor.getId();
        res.add(updateLogProcessor);
        //连接器
        componentConnector(groupId, evaluateJsons.getId(), updateLogProcessor.getId(), AutoEndBranchTypeEnum.MATCHED);
        ProcessorEntity publishKafkaForPipelineProcessor = createPublishKafkaForPipelineProcessor(groupId, 16);
        mdmTableNifiSettingPO.publishKafkaPipelineProcessorId = publishKafkaForPipelineProcessor.getId();
        res.add(publishKafkaForPipelineProcessor);
        //连接器
        componentConnector(groupId, evaluateJsons.getId(), publishKafkaForPipelineProcessor.getId(), AutoEndBranchTypeEnum.MATCHED);
        componentsConnector(groupId, publishKafkaForPipelineProcessor.getId(), supervisionId, autoEndBranchTypeEnums);
        lastId = updateLogProcessor.getId();
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
            String outputPortId = buildPortComponent(config.taskGroupConfig.appName, groupId, processorX, processorY,
                    PortComponentEnum.COMPONENT_OUTPUT_PORT_COMPONENT);

            // 创建output_port connection(组)
            String componentOutputPortConnectionId = "";
            componentOutputPortConnectionId = buildPortConnection(groupId,
                    groupId, outputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
                    groupId, lastId, ConnectableDTO.TypeEnum.PROCESSOR,
                    3, PortComponentEnum.COMPONENT_OUTPUT_PORT_CONNECTION);

            // 创建output connection(任务)
            String taskOutputPortConnectionId = buildPortConnection(groupEntityId,
                    groupEntityId, tableOutputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
                    taskGroupEntityId, outputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
                    2, PortComponentEnum.TASK_OUTPUT_PORT_CONNECTION);

            mdmTableNifiSettingPO.processorOutputPortConnectId = componentOutputPortConnectionId;
            mdmTableNifiSettingPO.tableOutputPortConnectId = taskOutputPortConnectionId;
            mdmTableNifiSettingPO.processorOutputPortId = outputPortId;
        }

        mdmTableNifiSettingPO.tableInputPortId = tableInputPortId;
        mdmTableNifiSettingPO.tableOutputPortId = tableOutputPortId;
        mdmTableNifiSettingPO.processorInputPortId = inputPortId;
        mdmTableNifiSettingPO.nifiCustomWorkflowDetailId = dto.workflowDetailId;
        mdmTableNifiSettingPO.selectSql = config.processorConfig.sourceExecSqlQuery;
        mdmTableNifiSettingPO.type = dto.type.getValue();
        mdmTableNifiSettingPO.syncMode = config.targetDsConfig.syncMode;
        mdmNifiSettingPO.nifiCustomWorkflowId = dto.nifiCustomWorkflowId;
        mdmNifiSettingService.saveOrUpdate(mdmNifiSettingPO);
        res.addAll(processorEntities);
        if (dto.groupStructureId == null) {
            res.add(consumeKafkaProcessor);
        }
        mdmTableNifiSettingService.saveOrUpdate(mdmTableNifiSettingPO);
        return res;
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

    /**
     * 执行sql delete组件
     *
     * @param config         数据接入配置
     * @param groupId        组id
     * @param targetDbPoolId ods连接池id
     * @return 组件对象
     */
    private ProcessorEntity execDeleteSqlProcessor(AccessMdmConfigDTO config, String groupId, String targetDbPoolId, SynchronousTypeEnum synchronousTypeEnum, BuildMdmNifiFlowDTO dto) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Exec Target Delete";
        querySqlDto.details = "query_phase";
        querySqlDto.groupId = groupId;
        //接入需要数据校验,查的是mdm表,其他的不变
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataTargetMdmId));
        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            DataSourceDTO data = fiDataDataSource.data;
            IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(data.conType);
            querySqlDto.querySql = dbCommand.delMdmField(dto, config, groupId);
        } else {
            log.error("userclient无法查询到mdm库的连接信息");
            throw new FkException(ResultEnum.ERROR);
        }
        querySqlDto.dbConnectionId = targetDbPoolId;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(7);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<String>());
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }

    /**
     * createPublishKafkaForPipelineProcessor
     * @param groupId   组id
     * @return 组件对象
     */
    public ProcessorEntity createPublishKafkaForPipelineProcessor(String groupId, int position) {
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
        buildPublishKafkaProcessorDTO.positionDTO = NifiPositionHelper.buildXYPositionDTO(1,position);
        buildPublishKafkaProcessorDTO.TopicName = pipelineTopicName;
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildPublishKafkaProcessor(buildPublishKafkaProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    private ProcessorEntity updateLogProcedure(AccessMdmConfigDTO config, String groupId, String cfgDbPoolId) {
        BuildCallDbProcedureProcessorDTO callDbProcedureProcessorDTO = new BuildCallDbProcedureProcessorDTO();
        callDbProcedureProcessorDTO.name = "CallDbLogProcedure";
        callDbProcedureProcessorDTO.details = "insert_phase";
        callDbProcedureProcessorDTO.groupId = groupId;
        //调用sql,存日志
        String executsql1 = "UPDATE tb_etl_log SET `status` =1,enddate='${" + NifiConstants.AttrConstants.END_TIME + "}',datarows='${" + NifiConstants.AttrConstants.NUMBERS + "}',topic_name='${" + NifiConstants.AttrConstants.KAFKA_TOPIC + "}' ";
        executsql1 += "WHERE\n" +
                "\tcode='${fidata_batch_code}' and tablename='" + config.targetDsConfig.targetTableName + "';\n";
        executsql1 += "update tb_etl_Incremental  set incremental_objectivescore_start='${incremental_objectivescore_end}', enable_flag=2 " +
                "where object_name = '" + config.targetDsConfig.targetTableName + "' ;";
        callDbProcedureProcessorDTO.dbConnectionId = cfgDbPoolId;
        callDbProcedureProcessorDTO.executsql = executsql1;
        callDbProcedureProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(16);
        callDbProcedureProcessorDTO.haveNextOne = false;
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildCallDbProcedureProcess(callDbProcedureProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
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

    private ProcessorEntity queryNumbersProcessor(BuildMdmNifiFlowDTO dto, AccessMdmConfigDTO config, String groupId, String targetDbPoolId) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Query numbers Field";
        querySqlDto.details = "insert_phase";
        querySqlDto.groupId = groupId;
        //接入需要数据校验,查的是mdm表,其他的不变
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataTargetMdmId));
        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            DataSourceDTO data = fiDataDataSource.data;
            IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(data.conType);
            querySqlDto.querySql = dbCommand.queryMdmNumbersField(dto, config, groupId);
        } else {
            log.error("userclient无法查询到mdm库的连接信息");
            throw new FkException(ResultEnum.ERROR);
        }
        querySqlDto.dbConnectionId = targetDbPoolId;
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(13);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<>());
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
    }
    private ProcessorEntity createSyncTable(AccessMdmConfigDTO config, String groupId, String targetDbPoolId, BuildMdmNifiFlowDTO buildNifiFlow) {
        BuildCallDbProcedureProcessorDTO callDbProcedureProcessorDTO = new BuildCallDbProcedureProcessorDTO();
        callDbProcedureProcessorDTO.name = "SyncTableProcedure";
        callDbProcedureProcessorDTO.details = "insert_phase";
        callDbProcedureProcessorDTO.groupId = groupId;
        String executsql = "";
        String syncMode = syncModeTypeEnum.getNameByValue(config.targetDsConfig.syncMode);
        log.info("同步类型为:" + syncMode + config.targetDsConfig.syncMode);
        ResultEntity<Object> accessDefaultSql = mdmClient.getAccessDefaultSql(buildNifiFlow.modelId.intValue(), buildNifiFlow.entityId.intValue());

        if (accessDefaultSql.code == ResultEnum.SUCCESS.getCode()) {
            executsql = accessDefaultSql.getData().toString();
        } else {
            log.error("mdmclient无法查询到默认同步sql信息");
            throw new FkException(ResultEnum.ERROR);
        }
        callDbProcedureProcessorDTO.dbConnectionId = targetDbPoolId;
        log.info("SQL预览语句：{}", JSON.toJSONString(buildNifiFlow.syncStgToMdmSql));
        callDbProcedureProcessorDTO.executsql = StringUtils.isNotEmpty(buildNifiFlow.syncStgToMdmSql) ? buildNifiFlow.syncStgToMdmSql : executsql;
        callDbProcedureProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(12);
        callDbProcedureProcessorDTO.haveNextOne = true;
        callDbProcedureProcessorDTO.sqlPreQuery = buildNifiFlow.customScriptBefore;
        callDbProcedureProcessorDTO.sqlPostQuery = buildNifiFlow.customScriptAfter;
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildCallDbProcedureProcess(callDbProcedureProcessorDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
    }

    private ProcessorEntity createPutDatabaseRecord(String appGroupId, AccessMdmConfigDTO config, String groupId, BuildMdmNifiFlowDTO dto, String targetDbPoolId, SynchronousTypeEnum synchronousTypeEnum, MdmTableNifiSettingPO mdmTableNifiSettingPO) {
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
        sourceFieldName = config.mdmPublishFieldDTOList.stream().map(e -> e.fieldName).collect(Collectors.toList());
        if (StringUtils.isNotEmpty(dto.generateVersionSql)) {
            sourceFieldName.add(NifiConstants.AttrConstants.FI_VERSION);
        }
        sourceFieldName.add("fidata_batch_code");
        sourceFieldName.add("fidata_flow_batch_code");
        sourceFieldName.add("fidata_version_id");
        sourceFieldName.add("fidata_import_type");
        sourceFieldName.add("fidata_create_time");
        sourceFieldName.add("fidata_create_user");
        sourceFieldName.add("fidata_update_time");
        sourceFieldName.add("fidata_update_user");
        sourceFieldName.add("fidata_del_flag");

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
        mdmTableNifiSettingPO.putDatabaseRecordId = id;
        PutDatabaseRecordDTO putDatabaseRecordDTO = new PutDatabaseRecordDTO();
        putDatabaseRecordDTO.name = "executeSQLRecord";
        putDatabaseRecordDTO.details = "executeSQLRecord";
        putDatabaseRecordDTO.groupId = groupId;
        //putDatabaseRecordDTO.databaseConnectionPoolingService=config.targetDsConfig.componentId;
        putDatabaseRecordDTO.databaseConnectionPoolingService = targetDbPoolId;
        putDatabaseRecordDTO.databaseType = "PostgreSQL";//数据库类型,定义枚举
        putDatabaseRecordDTO.recordReader = id;
        putDatabaseRecordDTO.statementType = "INSERT";
        putDatabaseRecordDTO.putDbRecordTranslateFieldNames = "false";
        //得到stg表名
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataTargetMdmId));
        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            DataSourceDTO dataSource = fiDataDataSource.data;
            IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(dataSource.conType);
            String stgTableName = "stg_"+dto.modelName+"_"+dto.entityName;
            //pg不需要这个配置,默认true,SQL server是false
            if (Objects.equals(dataSource.conType, DataSourceTypeEnum.SQLSERVER)) {
                putDatabaseRecordDTO.putDbRecordTranslateFieldNames = "false";
            } else if (Objects.equals(dataSource.conType, DataSourceTypeEnum.POSTGRESQL)) {
                putDatabaseRecordDTO.putDbRecordTranslateFieldNames = "true";
            }
            if (Objects.equals(dataSource.conType, DataSourceTypeEnum.SQLSERVER)) {
                putDatabaseRecordDTO.TableName = stgTableName;
                putDatabaseRecordDTO.schemaName = "dbo";
            } else if (Objects.equals(dataSource.conType, DataSourceTypeEnum.POSTGRESQL)) {
                putDatabaseRecordDTO.TableName = stgTableName;
                putDatabaseRecordDTO.schemaName = "public";
            }

        } else {
            log.error("userclient无法查询到ods库的连接信息");
            throw new FkException(ResultEnum.ERROR);
        }
        putDatabaseRecordDTO.concurrentTasks = ConcurrentTasks;
        putDatabaseRecordDTO.synchronousTypeEnum = synchronousTypeEnum;
        putDatabaseRecordDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(11);
        putDatabaseRecordDTO.setPutDbRecordQuotedTableIdentifiers(true);
        BusinessResult<ProcessorEntity> res = componentsBuild.buildPutDatabaseRecordProcess(putDatabaseRecordDTO);
        verifyProcessorResult(res);
        return res.data;
    }

    private ProcessorEntity createUpdateField(String mdmGroupId, AccessMdmConfigDTO config, String groupId, BuildMdmNifiFlowDTO dto, MdmTableNifiSettingPO mdmTableNifiSettingPO) {
        //两个控制器服务,和一个组件,先把配置搞出来
        BuildUpdateRecordDTO buildUpdateRecordDTO = new BuildUpdateRecordDTO();
        Map<String, String> buildParameter = new HashMap<>();
        String groupStructureId = dto.groupStructureId;
        BuildAvroRecordSetWriterServiceDTO buildAvroRecordSetWriterServiceDTO = new BuildAvroRecordSetWriterServiceDTO();
        buildAvroRecordSetWriterServiceDTO.details = "transition_phase";
        buildAvroRecordSetWriterServiceDTO.name = "AvroRecordSetWriterServiceTransition";
        if (groupStructureId != null) {
            buildAvroRecordSetWriterServiceDTO.groupId = groupStructureId;
        } else {
            buildAvroRecordSetWriterServiceDTO.groupId = mdmGroupId;
        }
        List<String> sourceFieldName = new ArrayList<>();
        sourceFieldName = config.mdmPublishFieldDTOList.stream().map(e -> e.fieldName).collect(Collectors.toList());
        String schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);
        buildAvroRecordSetWriterServiceDTO.schemaArchitecture = schemaArchitecture;
        buildAvroRecordSetWriterServiceDTO.schemaWriteStrategy = "avro-embedded";
        buildAvroRecordSetWriterServiceDTO.schemaAccessStrategy = "schema-text-property";
        BusinessResult<ControllerServiceEntity> avroRecordSetWriterService = componentsBuild.buildAvroRecordSetWriterService(buildAvroRecordSetWriterServiceDTO);
        mdmTableNifiSettingPO.convertAvroRecordSetWriterId = avroRecordSetWriterService.data.getId();
        //--------------------------------------
        BuildAvroReaderServiceDTO buildAvroReaderServiceDTO = new BuildAvroReaderServiceDTO();
        buildAvroReaderServiceDTO.details = "transition_phase";
        buildAvroReaderServiceDTO.name = "PutDatabaseRecordTransition";
        if (groupStructureId != null) {
            buildAvroReaderServiceDTO.groupId = groupStructureId;
        } else {
            buildAvroReaderServiceDTO.groupId = mdmGroupId;
        }
        List<AccessAttributeDTO> modelPublishFieldDTOS = config.mdmPublishFieldDTOList;
        for (AccessAttributeDTO accessAttributeDTO : modelPublishFieldDTOS) {
            if (!Objects.equals(accessAttributeDTO.fieldName, accessAttributeDTO.getSourceFieldName())) {
                buildParameter.put("/" + accessAttributeDTO.fieldName, "/" + accessAttributeDTO.getSourceFieldName());
            }
        }
        buildParameter.put("/" + modelPublishFieldDTOS.get(0).fieldName, "/" + modelPublishFieldDTOS.get(0).getSourceFieldName());
        sourceFieldName = config.mdmPublishFieldDTOList.stream().map(e -> e.getSourceFieldName()).collect(Collectors.toList());

        schemaArchitecture = buildSchemaArchitecture(sourceFieldName, config.processorConfig.targetTableName);
        buildAvroReaderServiceDTO.schemaText = schemaArchitecture;
        BusinessResult<ControllerServiceEntity> avroReaderService = componentsBuild.buildAvroReaderService(buildAvroReaderServiceDTO);
        mdmTableNifiSettingPO.convertPutDatabaseRecordId = avroReaderService.data.getId();
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
        buildUpdateRecordDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(9);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildUpdateRecord(buildUpdateRecordDTO);
        verifyProcessorResult(processorEntityBusinessResult);
        return processorEntityBusinessResult.data;
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

    public List<ProcessorEntity> buildDeltaTimeProcessorEntity(List<DeltaTimeDTO> deltaTimes, String groupId, String sourceId, List<ProcessorEntity> res, MdmTableNifiSettingPO mdmTableNifiSettingPO) {
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
                            mdmTableNifiSettingPO.queryStratTimeProcessorId = null;
                            mdmTableNifiSettingPO.convertStratTimeToJsonProcessorId = null;
                            mdmTableNifiSettingPO.setStratTimeProcessorId = null;

                        } else if (Objects.equals(dto.deltaTimeParameterTypeEnum, DeltaTimeParameterTypeEnum.VARIABLE)) {
                            //该变量的值为表达式,需要去数据源查,需要加三个组件
                            ProcessorEntity processorEntity = queryIncrementTimeProcessor(dto.variableValue, groupId, sourceId);
                            ProcessorEntity jsonRes = convertJsonProcessor(groupId, 0, 5);
                            List<String> strings = new ArrayList<>();
                            strings.add(NifiConstants.AttrConstants.INCREMENT_DB_FIELD_START);
                            ProcessorEntity evaluateJson = evaluateTimeVariablesProcessor(groupId, strings);
                            processorEntities.add(processorEntity);
                            processorEntities.add(jsonRes);
                            processorEntities.add(evaluateJson);
                            mdmTableNifiSettingPO.queryStratTimeProcessorId = processorEntity.getId();
                            mdmTableNifiSettingPO.convertStratTimeToJsonProcessorId = jsonRes.getId();
                            mdmTableNifiSettingPO.setStratTimeProcessorId = evaluateJson.getId();
                            componentConnector(groupId, processorEntity.getId(), jsonRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
                            componentConnector(groupId, jsonRes.getId(), evaluateJson.getId(), AutoEndBranchTypeEnum.SUCCESS);
                            connectId = evaluateJson.getId();
                        } else {
                            //该变量的值未定义,未定义的话用前面情况5查出来的值,此时不用加组件
                            mdmTableNifiSettingPO.queryStratTimeProcessorId = null;
                            mdmTableNifiSettingPO.convertStratTimeToJsonProcessorId = null;
                            mdmTableNifiSettingPO.setStratTimeProcessorId = null;
                        }
                    } else if (Objects.equals(dto.systemVariableTypeEnum, SystemVariableTypeEnum.END_TIME)) {
                        if (Objects.equals(dto.deltaTimeParameterTypeEnum, DeltaTimeParameterTypeEnum.CONSTANT)) {
                            //该变量的值为常量,常量的话去增量表里查,需要在保存并发布的时候向增量表插入一条数据,此时不用添加组件
                            mdmTableNifiSettingPO.queryStratTimeProcessorId = null;
                            mdmTableNifiSettingPO.convertStratTimeToJsonProcessorId = null;
                            mdmTableNifiSettingPO.setStratTimeProcessorId = null;
                        } else if (Objects.equals(dto.deltaTimeParameterTypeEnum, DeltaTimeParameterTypeEnum.VARIABLE)) {
                            //该变量的值为表达式,需要去数据源查,需要加三个组件
                            ProcessorEntity processorEntity = queryIncrementTimeProcessor(dto.variableValue, groupId, sourceId);
                            ProcessorEntity jsonRes = convertJsonProcessor(groupId, 0, 5);
                            List<String> strings = new ArrayList<>();
                            strings.add(NifiConstants.AttrConstants.INCREMENT_DB_FIELD_END);
                            ProcessorEntity evaluateJson = evaluateTimeVariablesProcessor(groupId, strings);
                            processorEntities.add(processorEntity);
                            processorEntities.add(jsonRes);
                            processorEntities.add(evaluateJson);
                            mdmTableNifiSettingPO.queryEndTimeProcessorId = processorEntity.getId();
                            mdmTableNifiSettingPO.convertEndTimeToJsonProcessorId = jsonRes.getId();
                            mdmTableNifiSettingPO.setEndTimeProcessorId = evaluateJson.getId();
                            if (connectId != "") {
                                componentConnector(groupId, connectId, processorEntity.getId(), AutoEndBranchTypeEnum.MATCHED);
                            }
                            componentConnector(groupId, processorEntity.getId(), jsonRes.getId(), AutoEndBranchTypeEnum.SUCCESS);
                            componentConnector(groupId, jsonRes.getId(), evaluateJson.getId(), AutoEndBranchTypeEnum.SUCCESS);
                        } else {
                            //该变量的值未定义,未定义的话用前面情况5查出来的值,此时不用加组件
                            mdmTableNifiSettingPO.queryStratTimeProcessorId = null;
                            mdmTableNifiSettingPO.convertStratTimeToJsonProcessorId = null;
                            mdmTableNifiSettingPO.setStratTimeProcessorId = null;
                        }
                    }
                }
            }
        }
        res.addAll(processorEntities);
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
     * 插入日志组件
     *
     * @param groupId  组id
     * @param dbPoolId 连接池id
     * @param dto      dto
     * @param config   config
     * @return 组件对象
     */
    private ProcessorEntity putLogProcessor(String groupId, String dbPoolId, BuildMdmNifiFlowDTO dto, AccessMdmConfigDTO config) {
        BuildPutSqlProcessorDTO putSqlDto = new BuildPutSqlProcessorDTO();
        putSqlDto.name = "Put Log to Config Db";
        putSqlDto.details = "query_phase";
        putSqlDto.groupId = groupId;
        putSqlDto.dbConnectionId = dbPoolId;
        putSqlDto.sqlStatement = buildLogSql(dto, config.processorConfig.sourceExecSqlQuery);
        putSqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(6);

        BusinessResult<ProcessorEntity> putSqlRes = componentsBuild.buildPutSqlProcess(putSqlDto);
        verifyProcessorResult(putSqlRes);
        return putSqlRes.data;
    }
    /**
     * 创建日志sql
     *
     * @return sql
     */
    private String buildLogSql(BuildMdmNifiFlowDTO dto, String selectSql) {
        String filedValues = "";
        // filedValues += dto.queryStartTime == null ? ",'0000-01-01 00:00:00'" : (",'" + dto.queryStartTime + "'");
        //filedValues += dto.queryEndTime == null ? ",now()" : (",'" + dto.queryEndTime + "'");
        filedValues += ",'${incremental_objectivescore_start}','${incremental_objectivescore_end}'";
        if (dto.selectSql != null && dto.selectSql != "") {
            filedValues += ",\"" + dto.selectSql.replaceAll("\"", "\\\\\"") + "\"";
        } else {
            filedValues += ",\"" + selectSql.replaceAll("\"", "\\\\\"") + "\"";
        }

        return "INSERT INTO tb_etl_log ( tablename, startdate, `status`,query_start_time,query_end_time,query_sql,code) " +
                "VALUES ('" + dto.tableName + "', '${" + NifiConstants.AttrConstants.START_TIME + "}', 1" + filedValues + ",'${fidata_batch_code}')";
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
        dto.positionDTO = NifiPositionHelper.buildYPositionDTO(5);
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
    private ProcessorEntity queryIncrementFieldProcessor(AccessMdmConfigDTO config, String groupId, String cfgDbPoolId, BuildMdmNifiFlowDTO dto) {
        BuildExecuteSqlProcessorDTO querySqlDto = new BuildExecuteSqlProcessorDTO();
        querySqlDto.name = "Query Increment Field";
        querySqlDto.details = "query_phase";
        querySqlDto.groupId = groupId;
        querySqlDto.querySql = buildIncrementSql(config.processorConfig.targetTableName);
        querySqlDto.dbConnectionId = cfgDbPoolId;
        /*querySqlDto.scheduleExpression = config.processorConfig.scheduleExpression;
        querySqlDto.scheduleType = config.processorConfig.scheduleType;*/
        querySqlDto.positionDTO = NifiPositionHelper.buildYPositionDTO(3);
        BusinessResult<ProcessorEntity> querySqlRes = componentsBuild.buildExecuteSqlProcess(querySqlDto, new ArrayList<String>());
        verifyProcessorResult(querySqlRes);
        return querySqlRes.data;
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

    private ProcessorEntity createExecuteSQLRecord(String appGroupId, AccessMdmConfigDTO config, String groupId, BuildMdmNifiFlowDTO dto, String sourceDbPoolId, MdmTableNifiSettingPO mdmTableNifiSettingPO) {
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

        sourceFieldName = config.mdmPublishFieldDTOList.stream().map(AccessAttributeDTO::getSourceFieldName).collect(Collectors.toList());
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
        mdmTableNifiSettingPO.avroRecordSetWriterId = id;
        ExecuteSQLRecordDTO executeSQLRecordDTO = new ExecuteSQLRecordDTO();
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
        executeSQLRecordDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(8);
        BusinessResult<ProcessorEntity> res = componentsBuild.buildExecuteSQLRecordProcess(executeSQLRecordDTO);
        verifyProcessorResult(res);
        return res.data;
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
    /**
     * createConsumeKafkaProcessor
     *
     * @param configDTO 数据接入配置
     * @param groupId   组id
     * @return 组件对象
     */
    private ProcessorEntity createConsumeKafkaProcessor(AccessMdmConfigDTO configDTO, BuildMdmNifiFlowDTO dto, String groupId) {
        BuildConsumeKafkaProcessorDTO buildConsumeKafkaProcessorDTO = new BuildConsumeKafkaProcessorDTO();
        buildConsumeKafkaProcessorDTO.name = "ConsumeKafka";
        buildConsumeKafkaProcessorDTO.details = "query_phase";
        buildConsumeKafkaProcessorDTO.groupId = groupId;
        //管道id
        buildConsumeKafkaProcessorDTO.GroupId = "dmp.nifi.mdm.pipeline";
        buildConsumeKafkaProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(1);
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
    public List<ProcessorEntity> pipelineSupervision(String groupId, List<ProcessorEntity> processorEntities, String cfgDbPoolId, MdmTableNifiSettingPO mdmTableNifiSettingPO) {
        List<ProcessorEntity> processorEntityList = new ArrayList<>();
        //查询
        ProcessorEntity processorEntity = queryForPipelineSupervision(groupId, cfgDbPoolId);
        mdmTableNifiSettingPO.queryForSupervisionProcessorId = processorEntity.getId();
        processorEntityList.add(processorEntity);
        //转string convertJsonForSupervision
        ProcessorEntity convertJsonForSupervision = specificSymbolProcessor(groupId, null);
        mdmTableNifiSettingPO.convertJsonForSupervisionProcessorId = convertJsonForSupervision.getId();
        componentConnector(groupId, processorEntity.getId(), convertJsonForSupervision.getId(), AutoEndBranchTypeEnum.SUCCESS);
        processorEntityList.add(convertJsonForSupervision);
        //发消息
        ProcessorEntity publishKafkaForSupervisionProcessor = createPublishKafkaForSupervisionProcessor(groupId, 6);
        mdmTableNifiSettingPO.publishKafkaForSupervisionProcessorId = publishKafkaForSupervisionProcessor.getId();
        componentConnector(groupId, convertJsonForSupervision.getId(), publishKafkaForSupervisionProcessor.getId(), AutoEndBranchTypeEnum.SUCCESS);
        processorEntityList.add(publishKafkaForSupervisionProcessor);
        return processorEntityList;
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
     * createPublishKafkaProcessor
     *
     * @param configDTO 数据接入配置
     * @param groupId   组id
     * @param dto
     * @return 组件对象
     */
    public ProcessorEntity createPublishKafkaProcessor(AccessMdmConfigDTO configDTO, BuildMdmNifiFlowDTO dto, String groupId, int position, boolean createProcessor) {
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
        tableTopicDTO.tableId = Math.toIntExact(dto.entityId);
        tableTopicDTO.tableType = dto.type.getValue();
        tableTopicDTO.topicName = MqConstants.TopicPrefix.TOPIC_MDM_PREFIX + dto.type.getValue() + "." + dto.modelId + "." + dto.entityId;
        tableTopicDTO.topicType = TopicTypeEnum.DAILY_NIFI_FLOW.getValue();
        if (Objects.equals(dto.type, OlapTableEnum.KPI)) {
            tableTopicDTO.topicName = MqConstants.TopicPrefix.TOPIC_MDM_PREFIX + OlapTableEnum.KPI.getValue() + "." + dto.modelId + "." + dto.entityId;
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
     * 启用组件
     *
     * @param groupId    groupId
     * @param processors 需要启用的组件
     */
    private void enabledProcessor(String groupId, List<ProcessorEntity> processors) {
        componentsBuild.enabledProcessor(groupId, processors);
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
}
