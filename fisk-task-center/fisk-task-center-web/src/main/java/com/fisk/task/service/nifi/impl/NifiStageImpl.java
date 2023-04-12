package com.fisk.task.service.nifi.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.davis.client.model.BulletinEntity;
import com.davis.client.model.ProcessGroupEntity;
import com.davis.client.model.ProcessorEntity;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.app.LogMessageFilterVO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.TaskHierarchyDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.businessarea.BusinessAreaQueryTableDTO;
import com.fisk.datamodel.dto.businessarea.BusinessAreaTableDetailDTO;
import com.fisk.system.client.UserClient;
import com.fisk.task.dto.daconfig.OverLoadCodeDTO;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.nifi.NifiStageMessageDTO;
import com.fisk.task.dto.pipeline.NifiStageDTO;
import com.fisk.task.dto.query.PipelineTableQueryDTO;
import com.fisk.task.entity.NifiStagePO;
import com.fisk.task.entity.PipelineTableLogPO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.pipeline.IPipelineTaskPublishCenter;
import com.fisk.task.listener.postgre.datainput.IbuildTable;
import com.fisk.task.listener.postgre.datainput.impl.BuildFactoryHelper;
import com.fisk.task.map.NifiStageMap;
import com.fisk.task.map.NifiStageMapImpl;
import com.fisk.task.mapper.NifiStageMapper;
import com.fisk.task.mapper.PipelineTableLogMapper;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelStageLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.service.nifi.INifiStage;
import com.fisk.task.utils.KafkaTemplateHelper;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cfk
 */
@Service
@Slf4j
public class NifiStageImpl extends ServiceImpl<NifiStageMapper, NifiStagePO> implements INifiStage {

    @Resource
    NifiStageMapper nifiStageMapper;
    @Resource
    TableNifiSettingServiceImpl tableNifiSettingService;
    @Resource
    OlapImpl olap;
    @Resource
    DataFactoryClient dataFactoryClient;
    @Resource
    PipelineTableLogMapper pipelineTableLog;
    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    DataModelClient dataModelClient;
    @Resource
    IPipelTaskLog iPipelTaskLog;
    @Resource
    IPipelStageLog iPipelStageLog;
    @Resource
    IPipelJobLog iPipelJobLog;
    @Resource
    IPipelineTaskPublishCenter iPipelineTaskPublishCenter;
    @Resource
    KafkaTemplateHelper kafkaTemplateHelper;
    @Resource
    UserClient userClient;


    @Override
    public List<NifiStageDTO> getNifiStage(List<NifiCustomWorkflowDetailDTO> list) {
        List<NifiStageDTO> nifiStages = new ArrayList<>();
        PipelineTableQueryDTO pipelineTableQuery = new PipelineTableQueryDTO();
        BusinessAreaQueryTableDTO businessAreaQueryTable = new BusinessAreaQueryTableDTO();
        for (NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetail : list) {
            QueryWrapper<NifiStagePO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(NifiStagePO::getComponentId, nifiCustomWorkflowDetail.id);
            List<NifiStagePO> nifiStagePos = nifiStageMapper.selectList(queryWrapper);
            if (CollectionUtils.isNotEmpty(nifiStagePos)) {
                List<NifiStageDTO> nifiStageDtos = NifiStageMap.INSTANCES.listPoToDto(nifiStagePos);
                nifiStages.addAll(nifiStageDtos);
            }
        }
        for (NifiStageDTO nifiStage : nifiStages) {
            Integer pipelineTableLogId = nifiStage.pipelineTableLogId;
            PipelineTableLogPO pipelineTableLogPO = pipelineTableLog.selectById(pipelineTableLogId);
            Integer tableId = pipelineTableLogPO.tableId;
            Integer appId = pipelineTableLogPO.appId;
            Integer tableType = pipelineTableLogPO.tableType;
            if (OlapTableEnum.PHYSICS.getValue() == tableType) {
                pipelineTableQuery.apiId = Long.valueOf(tableId);
                pipelineTableQuery.appId = appId;
                ResultEntity<List<LogMessageFilterVO>> tableNames = dataAccessClient.getTableNameListByAppIdAndApiId(pipelineTableQuery);
                if (tableNames.code == ResultEnum.SUCCESS.getCode() && CollectionUtils.isNotEmpty(tableNames.data)) {
                    LogMessageFilterVO logMessage = tableNames.data.get(0);
                    String tableName = logMessage.tableName;
                    nifiStage.tableName = tableName;
                }
            } else if (OlapTableEnum.FACT.getValue() == tableType || OlapTableEnum.DIMENSION.getValue() == tableType) {
                businessAreaQueryTable.businessId = appId;
                businessAreaQueryTable.tableEnum = OlapTableEnum.getNameByValue(tableType);
                businessAreaQueryTable.tableId = tableId;
                ResultEntity<BusinessAreaTableDetailDTO> businessAreaTableDetail = dataModelClient.getBusinessAreaTableDetail(businessAreaQueryTable);
                if (businessAreaTableDetail.code == ResultEnum.SUCCESS.getCode() && Objects.nonNull(businessAreaTableDetail.data)) {
                    nifiStage.tableName = businessAreaTableDetail.data.tableName;
                }
            }
        }
        //通过组件id查到nifi阶段
        return nifiStages.stream().sorted(Comparator.comparing(NifiStageDTO::getCreateTime).reversed()).collect(Collectors.toList());
    }

    @Override
    public NifiStagePO saveNifiStage(String data, Acknowledgment acke) {
        log.info("阶段日志保存:" + data);
        NifiStagePO nifiStagePO = new NifiStagePO();
        String pipleName = "";
        String JobName = "";
        //转成集合
        data = "[" + data + "]";
        List<NifiStageMessageDTO> nifiStageMessages = JSON.parseArray(data, NifiStageMessageDTO.class);
        for (NifiStageMessageDTO nifiStageMessageDTO : nifiStageMessages) {
            try {
                String topicName = nifiStageMessageDTO.topic;
                if (StringUtils.isEmpty(nifiStageMessageDTO.pipelStageTraceId)) {
                    nifiStageMessageDTO.pipelStageTraceId = UUID.randomUUID().toString();
                }
                String[] topic = topicName.split("\\.");
                int type = 0;
                Integer tableAccessId = 0;
                Integer appId = 0;
                //分类,长度为6的是普通调度,其他的是管道调度 4 6 7
                if (topic.length == 6) {
                    tableAccessId = Integer.valueOf(topic[5]);
                    type = Integer.parseInt(topic[3]);
                    appId = Integer.valueOf(topic[4]);
                } else if (topic.length == 7) {
                    String pipelineId = topic[3];
                    //通过应用简称+表类别+表id,查到组件id
                    tableAccessId = Integer.valueOf(topic[6]);
                    type = Integer.parseInt(topic[4]);
                    appId = Integer.valueOf(topic[5]);
                    NifiGetPortHierarchyDTO nifiGetPortHierarchyDTO = olap.getNifiGetPortHierarchy(pipelineId, type, null, tableAccessId);
                    if (Objects.equals(Integer.parseInt(topic[4]), OlapTableEnum.CUSTOMIZESCRIPT.getValue())) {
                        //没有表id就把任务id扔进去
                        nifiGetPortHierarchyDTO.nifiCustomWorkflowDetailId = Long.valueOf(topic[6]);
                    }
                    //三个阶段,默认正在运行
                    TaskHierarchyDTO nIfiPortHierarchy = iPipelineTaskPublishCenter.getNifiPortHierarchy(nifiGetPortHierarchyDTO, nifiStageMessageDTO.pipelTraceId);
                    NifiCustomWorkflowDetailDTO itselfPort = nIfiPortHierarchy.itselfPort;
                    nifiStagePO.componentId = Math.toIntExact(itselfPort.id);
                    log.info("失败调用发布中心");
                    if (!StringUtils.isEmpty(nifiStageMessageDTO.message)) {
                        sendPublishCenter(nifiStageMessageDTO, itselfPort);
                    }
                } else if (topic.length == 4) {
                    //长度为4的只可能为nifi流程,可以通过groupid区分
                    String pipelineId = topic[3];
                    TableNifiSettingPO tableNifiSettingPO = tableNifiSettingService.query()
                            .eq("table_component_id", nifiStageMessageDTO.groupId).eq("del_flag", 1).one();
                    //通过应用简称+表类别+表id,查到组件id
                    String tableName = tableNifiSettingPO.tableName;
                    tableAccessId = tableNifiSettingPO.tableAccessId;
                    type = tableNifiSettingPO.type;
                    appId = tableNifiSettingPO.appId;
                    NifiGetPortHierarchyDTO nifiGetPortHierarchyDTO = olap.getNifiGetPortHierarchy(pipelineId, type, tableName, tableAccessId);
                    //三个阶段,默认正在运行
                    TaskHierarchyDTO nIfiPortHierarchy = iPipelineTaskPublishCenter.getNifiPortHierarchy(nifiGetPortHierarchyDTO, nifiStageMessageDTO.pipelTraceId);
                    NifiCustomWorkflowDetailDTO itselfPort = nIfiPortHierarchy.itselfPort;
                    nifiStagePO.componentId = Math.toIntExact(itselfPort.id);
                }

                if (nifiStageMessageDTO.message == null || "".equals(nifiStageMessageDTO.message)) {
                    nifiStagePO.comment = "运行成功";
                    nifiStagePO.queryPhase = NifiStageTypeEnum.SUCCESSFUL_RUNNING.getValue();
                    nifiStagePO.insertPhase = NifiStageTypeEnum.SUCCESSFUL_RUNNING.getValue();
                    nifiStagePO.transitionPhase = NifiStageTypeEnum.SUCCESSFUL_RUNNING.getValue();
                } else {
                    nifiStagePO.comment = nifiStageMessageDTO.message;
                    if (nifiStageMessageDTO.nifiStageDTO != null) {
                        NifiStageDTO nifiStageDTO = nifiStageMessageDTO.nifiStageDTO;
                        nifiStagePO = NifiStageMap.INSTANCES.dtoToPo(nifiStageDTO);
                    } else {
                        ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(nifiStageMessageDTO.groupId);
                        List<BulletinEntity> bulletins = processGroup.getBulletins();
                        if (bulletins != null && bulletins.size() != 0) {
                            String sourceId = bulletins.get(bulletins.size() - 1).getSourceId();
                            ProcessorEntity processor = NifiHelper.getProcessorsApi().getProcessor(sourceId);
                            String description = processor.getComponent().getConfig().getComments();
                            if (Objects.equals(description, NifiStageTypeEnum.QUERY_PHASE.getName())) {
                                nifiStagePO.queryPhase = NifiStageTypeEnum.RUN_FAILED.getValue();
                                nifiStagePO.insertPhase = NifiStageTypeEnum.NOT_RUN.getValue();
                                nifiStagePO.transitionPhase = NifiStageTypeEnum.NOT_RUN.getValue();
                            } else if (Objects.equals(description, NifiStageTypeEnum.TRANSITION_PHASE.getName())) {
                                nifiStagePO.queryPhase = NifiStageTypeEnum.SUCCESSFUL_RUNNING.getValue();
                                nifiStagePO.insertPhase = NifiStageTypeEnum.NOT_RUN.getValue();
                                nifiStagePO.transitionPhase = NifiStageTypeEnum.RUN_FAILED.getValue();
                            } else if (Objects.equals(description, NifiStageTypeEnum.INSERT_PHASE.getName())) {
                                nifiStagePO.queryPhase = NifiStageTypeEnum.SUCCESSFUL_RUNNING.getValue();
                                nifiStagePO.insertPhase = NifiStageTypeEnum.SUCCESSFUL_RUNNING.getValue();
                                nifiStagePO.insertPhase = NifiStageTypeEnum.RUN_FAILED.getValue();
                            }
                        }
                    }
                }


                PipelineTableLogPO pipelineTableLogPO = new PipelineTableLogPO();
                pipelineTableLogPO.comment = nifiStagePO.comment;
                pipelineTableLogPO.componentId = nifiStagePO.componentId;
                pipelineTableLogPO.tableId = tableAccessId;
                pipelineTableLogPO.tableType = type;
                pipelineTableLogPO.appId = appId;
                if (Objects.equals(nifiStagePO.insertPhase, NifiStageTypeEnum.RUN_FAILED.getValue()) ||
                        Objects.equals(nifiStagePO.queryPhase, NifiStageTypeEnum.RUN_FAILED.getValue()) ||
                        Objects.equals(nifiStagePO.transitionPhase, NifiStageTypeEnum.RUN_FAILED.getValue())) {
                    pipelineTableLogPO.state = NifiStageTypeEnum.RUN_FAILED.getValue();
                } else if (Objects.equals(nifiStagePO.insertPhase, NifiStageTypeEnum.RUNNING.getValue()) ||
                        Objects.equals(nifiStagePO.queryPhase, NifiStageTypeEnum.RUNNING.getValue()) ||
                        Objects.equals(nifiStagePO.transitionPhase, NifiStageTypeEnum.RUNNING.getValue())) {
                    pipelineTableLogPO.state = NifiStageTypeEnum.RUNNING.getValue();
                } else if (Objects.equals(nifiStagePO.insertPhase, NifiStageTypeEnum.SUCCESSFUL_RUNNING.getValue()) &&
                        Objects.equals(nifiStagePO.queryPhase, NifiStageTypeEnum.SUCCESSFUL_RUNNING.getValue()) &&
                        Objects.equals(nifiStagePO.transitionPhase, NifiStageTypeEnum.SUCCESSFUL_RUNNING.getValue())) {
                    pipelineTableLogPO.state = NifiStageTypeEnum.SUCCESSFUL_RUNNING.getValue();
                } else if (Objects.equals(nifiStagePO.insertPhase, NifiStageTypeEnum.NOT_RUN.getValue()) &&
                        Objects.equals(nifiStagePO.queryPhase, NifiStageTypeEnum.NOT_RUN.getValue()) &&
                        Objects.equals(nifiStagePO.transitionPhase, NifiStageTypeEnum.NOT_RUN.getValue())) {
                    pipelineTableLogPO.state = NifiStageTypeEnum.NOT_RUN.getValue();
                }
                pipelineTableLogPO.counts = nifiStageMessageDTO.counts;
                pipelineTableLogPO.endTime = nifiStageMessageDTO.endTime;
                pipelineTableLogPO.startTime = nifiStageMessageDTO.startTime;
                pipelineTableLogPO.appId = appId;
                if (nifiStagePO.componentId == null || nifiStagePO.componentId == 0) {
                    pipelineTableLogPO.dispatchType = 0;
                } else {
                    pipelineTableLogPO.dispatchType = 1;
                }
                pipelineTableLog.insert(pipelineTableLogPO);

                //----------------------------------------------
                HashMap<Integer, Object> taskMap = new HashMap<>();
                //nifiStageMessageDTO.pipelTaskTraceId
                taskMap.put(DispatchLogEnum.taskcount.getValue(), nifiStageMessageDTO.counts);
                taskMap.put(DispatchLogEnum.entrydate.getValue(), nifiStageMessageDTO.entryDate);

                HashMap<Integer, Object> stagekMap = new HashMap<>();
                stagekMap.put(DispatchLogEnum.taskcomment.getValue(), nifiStagePO.comment);
                stagekMap.put(DispatchLogEnum.entrydate.getValue(), nifiStageMessageDTO.entryDate);
                if (!StringUtils.isEmpty(nifiStageMessageDTO.pipelTaskTraceId)) {
                    iPipelStageLog.savePipelTaskStageLog(nifiStageMessageDTO.pipelStageTraceId, nifiStageMessageDTO.pipelTaskTraceId, stagekMap);
                }
                if (StringUtils.isEmpty(nifiStagePO.comment) || !nifiStagePO.comment.contains("成功")) {
                    taskMap.put(DispatchLogEnum.taskcomment.getValue(), nifiStagePO.comment);
                    DispatchExceptionHandlingDTO dispatchExceptionHandlingDTO = DispatchExceptionHandlingDTO.builder().build();
                    dispatchExceptionHandlingDTO.comment = nifiStagePO.comment;
                    dispatchExceptionHandlingDTO.pipelTraceId = nifiStageMessageDTO.pipelTraceId;
                    dispatchExceptionHandlingDTO.pipelJobTraceId = nifiStageMessageDTO.pipelJobTraceId;
                    dispatchExceptionHandlingDTO.pipelStageTraceId = nifiStageMessageDTO.pipelStageTraceId;
                    dispatchExceptionHandlingDTO.pipelTaskTraceId = nifiStageMessageDTO.pipelTaskTraceId;
                    iPipelJobLog.exceptionHandlingLog(dispatchExceptionHandlingDTO);
                }
                //-----------------------------------------------
                nifiStagePO.pipelineTableLogId = Math.toIntExact(pipelineTableLogPO.id);

                //this.save(nifiStagePO);

            } catch (Exception e) {
                DispatchExceptionHandlingDTO dispatchExceptionHandlingDTO = DispatchExceptionHandlingDTO.builder().build();
                dispatchExceptionHandlingDTO.comment = nifiStagePO.comment;
                dispatchExceptionHandlingDTO.pipelTraceId = nifiStageMessageDTO.pipelTraceId;
                dispatchExceptionHandlingDTO.pipelJobTraceId = nifiStageMessageDTO.pipelJobTraceId;
                dispatchExceptionHandlingDTO.pipelStageTraceId = nifiStageMessageDTO.pipelStageTraceId;
                dispatchExceptionHandlingDTO.pipelTaskTraceId = nifiStageMessageDTO.pipelTaskTraceId;
                log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
                iPipelJobLog.exceptionHandlingLog(dispatchExceptionHandlingDTO);

            } finally {
                if (acke != null) {
                    acke.acknowledge();
                }
            }
        }
        return nifiStagePO;
    }

    public void sendPublishCenter(NifiStageMessageDTO nifiStageMessage, NifiCustomWorkflowDetailDTO itselfPort) {
        KafkaReceiveDTO kafkaReceive = KafkaReceiveDTO.builder().build();
        kafkaReceive.topicType = TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue();
        kafkaReceive.topic = nifiStageMessage.topic;
        kafkaReceive.tableId = StringUtils.isEmpty(itselfPort.tableId) ? null : Integer.parseInt(itselfPort.tableId);
        kafkaReceive.nifiCustomWorkflowDetailId = itselfPort.id;
        ChannelDataEnum channel = ChannelDataEnum.getValue(itselfPort.componentType);
        OlapTableEnum olapTableEnum = ChannelDataEnum.getOlapTableEnum(channel.getValue());
        kafkaReceive.tableType = olapTableEnum.getValue();
        kafkaReceive.pipelTaskTraceId = nifiStageMessage.pipelTaskTraceId;
        kafkaReceive.pipelJobTraceId = nifiStageMessage.pipelJobTraceId;
        kafkaReceive.pipelTraceId = nifiStageMessage.pipelTraceId;
        kafkaReceive.message = nifiStageMessage.message;
        String param = JSON.toJSONString(kafkaReceive);
        log.info("失败调用发布中心的参数:" + param);
        DispatchExceptionHandlingDTO dispatchExceptionHandlingDTO = DispatchExceptionHandlingDTO.builder().build();
        dispatchExceptionHandlingDTO.comment = nifiStageMessage.message;
        dispatchExceptionHandlingDTO.pipelTraceId = nifiStageMessage.pipelTraceId;
        dispatchExceptionHandlingDTO.pipelJobTraceId = nifiStageMessage.pipelJobTraceId;
        dispatchExceptionHandlingDTO.pipelStageTraceId = nifiStageMessage.pipelStageTraceId;
        dispatchExceptionHandlingDTO.pipelTaskTraceId = nifiStageMessage.pipelTaskTraceId;
        iPipelJobLog.exceptionHandlingLog(dispatchExceptionHandlingDTO);
        kafkaTemplateHelper.sendMessageAsync("my-topic", param);
    }

    @Override
    public Object overlayCodePreview(OverLoadCodeDTO dto) {
        IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(dto.dataSourceType);
        return dbCommand.assemblySql(dto.config, dto.synchronousTypeEnum, dto.funcName, dto.buildNifiFlow);
    }


}
