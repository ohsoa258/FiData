package com.fisk.task.service.nifi.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.davis.client.model.BulletinEntity;
import com.davis.client.model.ProcessGroupEntity;
import com.davis.client.model.ProcessorEntity;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.consumeserveice.client.ConsumeServeiceClient;
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
import com.fisk.dataservice.dto.tableservice.TableServiceEmailDTO;
import com.fisk.system.client.UserClient;
import com.fisk.task.dto.AccessDataSuccessAndFailCountDTO;
import com.fisk.task.dto.daconfig.OverLoadCodeDTO;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.nifi.NifiStageMessageDTO;
import com.fisk.task.dto.pipeline.NifiStageDTO;
import com.fisk.task.dto.query.PipelineTableQueryDTO;
import com.fisk.task.entity.*;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.pipeline.IPipelineTaskPublishCenter;
import com.fisk.task.listener.postgre.datainput.IbuildTable;
import com.fisk.task.listener.postgre.datainput.impl.BuildFactoryHelper;
import com.fisk.task.map.NifiStageMap;
import com.fisk.task.mapper.NifiStageMapper;
import com.fisk.task.mapper.PipelineTableLogMapper;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.po.TableTopicPO;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelLog;
import com.fisk.task.service.dispatchLog.IPipelStageLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.service.nifi.INifiStage;
import com.fisk.task.service.nifi.IPipelineTableLog;
import com.fisk.task.service.pipeline.IEtlLog;
import com.fisk.task.service.pipeline.ITableTopicService;
import com.fisk.task.service.task.ITBETLIncremental;
import com.fisk.task.utils.KafkaTemplateHelper;
import com.fisk.task.utils.LocalDateUtil;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    @Value("${consumer-server-enable}")
    private Boolean consumerServerEnable;
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
    @Value("${nifi.pipeline.dispatch-email-url-prefix}")
    String dispatchEmailUrlPrefix;
    @Resource
    ConsumeServeiceClient consumeServeiceClient;
    @Resource
    IPipelLog iPipelLog;
    @Resource
    private IPipelineTableLog iPipelineTableLog;
    @Resource
    private IEtlLog etlLog;
    @Resource
    ITableTopicService tableTopicService;
    @Resource
    RedisUtil redisUtil;
    @Value("${nifi.pipeline.maxTime}")
    public String maxTime;
    @Resource
    IEtlLog iEtlLog;


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
    public void saveNifiStage(String data, Acknowledgment acke) {
        log.info("阶段日志保存:" + data);
        try {
            NifiStagePO nifiStagePO = new NifiStagePO();
            String inputString = data;

            // 找到 message 字段的位置
            int start = inputString.indexOf("\"message\":\"") + 11; // 11 是 "message":" 的长度
            int end = inputString.indexOf("\",\"pipelJobTraceId\"", start);

            // 提取 message 值并替换双引号
            String message = inputString.substring(start, end).replace("\"", "\\\"");
            data = inputString.substring(0, start) + message + inputString.substring(end);
            //转成集合
            data = "[" + data + "]";
            List<NifiStageMessageDTO> nifiStageMessages = JSON.parseArray(data, NifiStageMessageDTO.class);
            for (NifiStageMessageDTO nifiStageMessageDTO : nifiStageMessages) {
                try {
                    List<BulletinEntity> bulletins = new ArrayList<>();
                    if (nifiStageMessageDTO.groupId != null) {
                        ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(nifiStageMessageDTO.groupId);
                        bulletins = processGroup.getBulletins();
                        if (bulletins != null && bulletins.size() != 0) {
                            nifiStageMessageDTO.message = bulletins.get(0).getBulletin().getMessage();
                        }
                    }
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
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String format = simpleDateFormat.format(new Date());
                        LocalDateTime now = LocalDateTime.now();
                        if (consumerServerEnable && Objects.equals(type, OlapTableEnum.DATASERVICES.getValue())) {

                            //错误日志修复
                            LambdaQueryWrapper<PipelTaskLogPO> queryWrapper = new LambdaQueryWrapper<>();
                            queryWrapper.eq(PipelTaskLogPO::getTaskTraceId, nifiStageMessageDTO.pipelTaskTraceId)
                                    .eq(PipelTaskLogPO::getType, DispatchLogEnum.taskend.getValue())
                                    .eq(PipelTaskLogPO::getTableType, OlapTableEnum.DATASERVICES.getValue());
                            PipelTaskLogPO pipelTaskLogPO = iPipelTaskLog.getOne(queryWrapper);

                            if (pipelTaskLogPO != null) {
                                pipelTaskLogPO.setMsg(NifiStageTypeEnum.RUN_FAILED.getName() + " - " + format + " - ErrorMessage:" + nifiStageMessageDTO.message);
                                iPipelTaskLog.updateById(pipelTaskLogPO);
                            } else {
                                TableServiceEmailDTO tableServiceEmailDTO = new TableServiceEmailDTO();
                                tableServiceEmailDTO.appId = appId;
                                tableServiceEmailDTO.msg = NifiStageTypeEnum.RUN_FAILED.getName() + " - " + format + " - ErrorMessage:" + nifiStageMessageDTO.message;
                                tableServiceEmailDTO.result = "【运行失败】";
                                tableServiceEmailDTO.pipelTraceId = nifiStageMessageDTO.pipelTraceId;
                                List<PipelTaskLogPO> pos = iPipelTaskLog.query()
                                        .eq("task_trace_id", nifiStageMessageDTO.pipelTaskTraceId)
                                        .eq("type", DispatchLogEnum.taskstart.getValue())
                                        .list();

                                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(pos)) {
                                    PipelTaskLogPO taskLogPO = pos.get(0);
                                    try {
                                        Date date = new Date();
                                        Date parse = simpleDateFormat.parse(taskLogPO.msg.substring(7, 26));
                                        Long second = (date.getTime() - parse.getTime()) / 1000 % 60;
                                        Long minutes = (date.getTime() - parse.getTime()) / (60 * 1000) % 60;
                                        tableServiceEmailDTO.duration = minutes + "m " + second + "s";
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                                tableServiceEmailDTO.url = "【" + dispatchEmailUrlPrefix + "/#/DataFactory/pipelineSettings?pipelTraceId="
                                        + tableServiceEmailDTO.pipelTraceId + "】";
                                try {
                                    Map<String, String> hashMap = new HashMap<>();
                                    hashMap.put("数据分发表服务名称", "");
                                    hashMap.put("表名", String.valueOf(tableAccessId));
                                    hashMap.put("运行结果", tableServiceEmailDTO.result);
                                    hashMap.put("运行时长", tableServiceEmailDTO.duration);
                                    hashMap.put("运行详情", tableServiceEmailDTO.msg);
                                    hashMap.put("TraceID", tableServiceEmailDTO.pipelTraceId);
                                    hashMap.put("页面地址", tableServiceEmailDTO.url);
                                    tableServiceEmailDTO.body = hashMap;
                                    consumeServeiceClient.tableServiceSendEmails(tableServiceEmailDTO);
                                } catch (Exception e) {
                                    log.error("发邮件出错,但是不影响主流程。异常如下：" + e);
                                }
                                PipelTaskLogPO pipelTaskLogPO1 = new PipelTaskLogPO();
                                pipelTaskLogPO1.setTaskTraceId(nifiStageMessageDTO.pipelTaskTraceId);
                                pipelTaskLogPO1.setType(DispatchLogEnum.taskend.getValue());
                                pipelTaskLogPO1.setTableId(tableAccessId);
                                pipelTaskLogPO1.setMsg(NifiStageTypeEnum.RUN_FAILED.getName() + " - " + format + " - ErrorMessage:" + tableServiceEmailDTO.msg);
                                pipelTaskLogPO1.setTableType(type);
                                iPipelTaskLog.save(pipelTaskLogPO1);
                            }
                        } else if (consumerServerEnable && Objects.equals(type, OlapTableEnum.DATA_SERVICE_API.getValue())) {
                            TableServiceEmailDTO tableServiceEmailDTO = new TableServiceEmailDTO();
                            tableServiceEmailDTO.appId = appId;
                            tableServiceEmailDTO.msg = NifiStageTypeEnum.RUN_FAILED.getName() + " - " + format + " - ErrorMessage:" + nifiStageMessageDTO.message;
                            tableServiceEmailDTO.result = "【运行失败】";
                            tableServiceEmailDTO.pipelTraceId = nifiStageMessageDTO.pipelTraceId;
                            List<PipelTaskLogPO> pos = iPipelTaskLog.query()
                                    .eq("task_trace_id", nifiStageMessageDTO.pipelTaskTraceId)
                                    .eq("type", DispatchLogEnum.taskstart.getValue())
                                    .list();

                            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(pos)) {
                                PipelTaskLogPO taskLogPO = pos.get(0);
                                try {
                                    Date date = new Date();
                                    Date parse = simpleDateFormat.parse(taskLogPO.msg.substring(7, 26));
                                    Long second = (date.getTime() - parse.getTime()) / 1000 % 60;
                                    Long minutes = (date.getTime() - parse.getTime()) / (60 * 1000) % 60;
                                    tableServiceEmailDTO.duration = minutes + "m " + second + "s";
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            tableServiceEmailDTO.url = "【" + dispatchEmailUrlPrefix + "/#/DataFactory/pipelineSettings?pipelTraceId="
                                    + tableServiceEmailDTO.pipelTraceId + "】";
                            try {
                                Map<String, String> hashMap = new HashMap<>();
                                hashMap.put("数据分发api服务名称", "");
                                hashMap.put("表名", String.valueOf(tableAccessId));
                                hashMap.put("运行结果", tableServiceEmailDTO.result);
                                hashMap.put("运行时长", tableServiceEmailDTO.duration);
                                hashMap.put("运行详情", tableServiceEmailDTO.msg);
                                hashMap.put("TraceID", tableServiceEmailDTO.pipelTraceId);
                                hashMap.put("页面地址", tableServiceEmailDTO.url);
                                tableServiceEmailDTO.body = hashMap;
                                consumeServeiceClient.tableServiceSendEmails(tableServiceEmailDTO);
                            } catch (Exception e) {
                                log.error("发邮件出错,但是不影响主流程。异常如下：" + e);
                            }
                            PipelTaskLogPO pipelTaskLogPO1 = new PipelTaskLogPO();
                            pipelTaskLogPO1.setTaskTraceId(nifiStageMessageDTO.pipelTaskTraceId);
                            pipelTaskLogPO1.setType(DispatchLogEnum.taskend.getValue());
                            pipelTaskLogPO1.setTableId(tableAccessId);
                            pipelTaskLogPO1.setMsg(NifiStageTypeEnum.RUN_FAILED.getName() + " - " + format + " - ErrorMessage:" + nifiStageMessageDTO.message);
                            pipelTaskLogPO1.setTableType(type);
                            iPipelTaskLog.save(pipelTaskLogPO1);
                        }else if (Objects.equals(type, OlapTableEnum.MDM_DATA_ACCESS.getValue())) {
                            //错误日志修复
                            LambdaQueryWrapper<PipelTaskLogPO> queryWrapper = new LambdaQueryWrapper<>();
                            queryWrapper.eq(PipelTaskLogPO::getTaskTraceId, nifiStageMessageDTO.pipelTaskTraceId)
                                    .eq(PipelTaskLogPO::getType, DispatchLogEnum.taskend.getValue())
                                    .eq(PipelTaskLogPO::getTableType, OlapTableEnum.MDM_DATA_ACCESS.getValue());
                            PipelTaskLogPO pipelTaskLogPO = iPipelTaskLog.getOne(queryWrapper);

                            if (pipelTaskLogPO != null) {
                                pipelTaskLogPO.setMsg(NifiStageTypeEnum.RUN_FAILED.getName() + " - " + format + " - ErrorMessage:" + nifiStageMessageDTO.message);
                                iPipelTaskLog.updateById(pipelTaskLogPO);
                            } else {
                                TableServiceEmailDTO tableServiceEmailDTO = new TableServiceEmailDTO();
                                tableServiceEmailDTO.appId = appId;
                                tableServiceEmailDTO.msg = NifiStageTypeEnum.RUN_FAILED.getName() + " - " + format + " - ErrorMessage:" + nifiStageMessageDTO.message;
                                tableServiceEmailDTO.result = "【运行失败】";
                                tableServiceEmailDTO.pipelTraceId = nifiStageMessageDTO.pipelTraceId;
                                List<PipelTaskLogPO> pos = iPipelTaskLog.query()
                                        .eq("task_trace_id", nifiStageMessageDTO.pipelTaskTraceId)
                                        .eq("type", DispatchLogEnum.taskstart.getValue())
                                        .list();

                                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(pos)) {
                                    PipelTaskLogPO taskLogPO = pos.get(0);
                                    try {
                                        Date date = new Date();
                                        Date parse = simpleDateFormat.parse(taskLogPO.msg.substring(7, 26));
                                        Long second = (date.getTime() - parse.getTime()) / 1000 % 60;
                                        Long minutes = (date.getTime() - parse.getTime()) / (60 * 1000) % 60;
                                        tableServiceEmailDTO.duration = minutes + "m " + second + "s";
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            PipelTaskLogPO pipelTaskLogPO1 = new PipelTaskLogPO();
                            pipelTaskLogPO1.setTaskTraceId(nifiStageMessageDTO.pipelTaskTraceId);
                            pipelTaskLogPO1.setType(DispatchLogEnum.taskend.getValue());
                            pipelTaskLogPO1.setTableId(tableAccessId);
                            pipelTaskLogPO1.setMsg(NifiStageTypeEnum.RUN_FAILED.getName() + " - " + format + " - ErrorMessage:" + nifiStageMessageDTO.message);
                            pipelTaskLogPO1.setTableType(type);
                            iPipelTaskLog.save(pipelTaskLogPO1);

                            LambdaQueryWrapper<TBETLlogPO> queryWrapper1 = new LambdaQueryWrapper<>();
                            queryWrapper1.eq(TBETLlogPO::getCode, nifiStageMessageDTO.pipelTaskTraceId);
                            TBETLlogPO one = iEtlLog.getOne(queryWrapper1);
                            one.setEnddate(now);
                            one.setErrordesc(NifiStageTypeEnum.RUN_FAILED.getName() + " - " + format + " - ErrorMessage:" + nifiStageMessageDTO.message);
                            one.setStatus(2);
                            iEtlLog.updateById(one);
                        }
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
                            if (Objects.equals(Integer.parseInt(topic[4]), OlapTableEnum.CUSTOMIZESCRIPT.getValue())) {
                                sendPublishCenter(nifiStageMessageDTO, itselfPort, MqConstants.QueueConstants.BUILD_TASK_OVER_FLOW);
                            } else {
                                LambdaQueryWrapper<TableTopicPO> queryWrapper = new LambdaQueryWrapper<>();
                                queryWrapper.eq(TableTopicPO::getTopicName, topicName).eq(TableTopicPO::getDelFlag, 1);
                                TableTopicPO topicPO = tableTopicService.getOne(queryWrapper);
                                Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + nifiStageMessageDTO.pipelTraceId);
                                TaskHierarchyDTO taskHierarchy = JSON.parseObject(hmget.get(topicPO.getComponentId().toString()).toString(), TaskHierarchyDTO.class);
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String format = simpleDateFormat.format(new Date());
                                //错误日志修复
//                                LambdaQueryWrapper<PipelTaskLogPO> taskLogWrapper = new LambdaQueryWrapper<>();
//                                taskLogWrapper.eq(PipelTaskLogPO::getTaskTraceId, nifiStageMessageDTO.pipelTaskTraceId)
//                                        .eq(PipelTaskLogPO::getType, DispatchLogEnum.taskend.getValue());
//                                PipelTaskLogPO pipelTaskLogPO = iPipelTaskLog.getOne(taskLogWrapper);
                                String json = (String) redisUtil.get(RedisKeyEnum.PIPEL_END_TASK_TRACE_ID.getName() + ":" + nifiStageMessageDTO.pipelTaskTraceId);
                                PipelTaskLogPO pipelTaskLogPO = JSON.parseObject(json, PipelTaskLogPO.class);
                                if (pipelTaskLogPO != null) {
                                    pipelTaskLogPO.setMsg(NifiStageTypeEnum.RUN_FAILED.getName() + " - " + format + " - ErrorMessage:" + nifiStageMessageDTO.message);
                                    ZoneId zoneId = ZoneId.systemDefault();
                                    LocalDateTime localDateTime = new Date().toInstant().atZone(zoneId).toLocalDateTime();
                                    pipelTaskLogPO.setCreateTime(localDateTime);
                                    iPipelTaskLog.updateById(pipelTaskLogPO);
                                    redisUtil.set(RedisKeyEnum.PIPEL_END_TASK_TRACE_ID.getName() + ":" + nifiStageMessageDTO.pipelTaskTraceId, JSON.toJSONString(pipelTaskLogPO), Long.parseLong(maxTime));
                                } else {
                                    LambdaQueryWrapper<PipelTaskLogPO> taskLogWrapper = new LambdaQueryWrapper<>();
                                    taskLogWrapper.eq(PipelTaskLogPO::getTaskTraceId, nifiStageMessageDTO.pipelTaskTraceId)
                                            .eq(PipelTaskLogPO::getType, DispatchLogEnum.taskend.getValue());
                                    pipelTaskLogPO = iPipelTaskLog.getOne(taskLogWrapper);
                                    if (pipelTaskLogPO == null) {
                                        pipelTaskLogPO = new PipelTaskLogPO();
                                        pipelTaskLogPO.setTaskId(taskHierarchy.getId().toString());
                                        pipelTaskLogPO.setJobTraceId(nifiStageMessageDTO.pipelJobTraceId);
                                        pipelTaskLogPO.setTableId(tableAccessId);
                                        pipelTaskLogPO.setTableType(type);
                                        pipelTaskLogPO.setType(DispatchLogEnum.taskend.getValue());
                                        pipelTaskLogPO.setTaskTraceId(nifiStageMessageDTO.pipelTaskTraceId);
                                        pipelTaskLogPO.setMsg(NifiStageTypeEnum.RUN_FAILED.getName() + " - " + format + " - ErrorMessage:" + nifiStageMessageDTO.message);
                                        iPipelTaskLog.save(pipelTaskLogPO);
                                    } else {
                                        iPipelTaskLog.updateById(pipelTaskLogPO);
                                    }
                                    redisUtil.set(RedisKeyEnum.PIPEL_END_TASK_TRACE_ID.getName() + ":" + nifiStageMessageDTO.pipelTaskTraceId, JSON.toJSONString(pipelTaskLogPO), Long.parseLong(maxTime));
                                }
                                String pipelJobLogJson = (String) redisUtil.get(RedisKeyEnum.PIPEL_END_JOB_TRACE_ID.getName() + ":" + nifiStageMessageDTO.pipelJobTraceId);
                                PipelJobLogPO pipelJobLogPO = JSON.parseObject(pipelJobLogJson, PipelJobLogPO.class);
                                if (pipelJobLogPO != null) {
                                    String failedTime = simpleDateFormat.format(new Date());
                                    pipelJobLogPO.setMsg(NifiStageTypeEnum.RUN_FAILED.getName() + " - " + failedTime);
                                    ZoneId zoneId = ZoneId.systemDefault();
                                    LocalDateTime localDateTime = new Date().toInstant().atZone(zoneId).toLocalDateTime();
                                    pipelJobLogPO.setCreateTime(localDateTime);
                                    iPipelJobLog.updateById(pipelJobLogPO);
                                    redisUtil.set(RedisKeyEnum.PIPEL_END_JOB_TRACE_ID.getName() + ":" + nifiStageMessageDTO.pipelJobTraceId, JSON.toJSONString(pipelJobLogPO), Long.parseLong(maxTime));

                                }

                                sendPublishCenter(nifiStageMessageDTO, itselfPort, MqConstants.QueueConstants.TASK_PUBLIC_CENTER_TOPIC_NAME);
                            }
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

                    nifiStagePO.comment = nifiStageMessageDTO.message;
                    if (nifiStageMessageDTO.nifiStageDTO != null) {
                        NifiStageDTO nifiStageDTO = nifiStageMessageDTO.nifiStageDTO;
                        nifiStagePO = NifiStageMap.INSTANCES.dtoToPo(nifiStageDTO);
                    } else {
                        if (bulletins != null && bulletins.size() != 0) {
                            String sourceId = bulletins.get(0).getSourceId();
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
                    try {
                        iPipelJobLog.exceptionHandlingLog(dispatchExceptionHandlingDTO);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }

                }
            }
        } catch (Exception e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
        } finally {
            if (acke != null) {
                acke.acknowledge();
            }
        }
    }

    /**
     * 保存Nifi阶段日志 - 数据接入restfulapi存储操作日志
     *
     * @param data
     * @param acke
     */
    @Override
    public void saveNifiStageForAccessApi(String data, Acknowledgment acke) {
        log.info("阶段日志保存:" + data);
        try {
            NifiStagePO nifiStagePO = new NifiStagePO();
            //转成集合
            data = "[" + data + "]";
            List<NifiStageMessageDTO> nifiStageMessages = JSON.parseArray(data, NifiStageMessageDTO.class);
            for (NifiStageMessageDTO nifiStageMessageDTO : nifiStageMessages) {
                try {
                    List<BulletinEntity> bulletins = new ArrayList<>();
                    if (nifiStageMessageDTO.groupId != null) {
                        ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(nifiStageMessageDTO.groupId);
                        bulletins = processGroup.getBulletins();
                        if (bulletins != null && bulletins.size() != 0) {
                            nifiStageMessageDTO.message = bulletins.get(0).getBulletin().getMessage();
                        }
                    }
                    String topicName = nifiStageMessageDTO.topic;
                    if (StringUtils.isEmpty(nifiStageMessageDTO.pipelStageTraceId)) {
                        nifiStageMessageDTO.pipelStageTraceId = UUID.randomUUID().toString();
                    }
                    String[] topic = topicName.split("\\.");
                    int type = 0;
                    int tableAccessId = 0;
                    int appId = 0;
                    //分类,长度为6的是普通调度,其他的是管道调度 4 6 7
                    if (topic.length == 6) {
                        tableAccessId = Integer.parseInt(topic[5]);
                        type = Integer.parseInt(topic[3]);
                        appId = Integer.parseInt(topic[4]);
                    }

                    nifiStagePO.comment = nifiStageMessageDTO.message;
                    if (nifiStageMessageDTO.nifiStageDTO != null) {
                        NifiStageDTO nifiStageDTO = nifiStageMessageDTO.nifiStageDTO;
                        nifiStagePO = NifiStageMap.INSTANCES.dtoToPo(nifiStageDTO);
                    } else {
                        if (bulletins != null && bulletins.size() != 0) {
                            String sourceId = bulletins.get(0).getSourceId();
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
                    nifiStagePO.pipelineTableLogId = Math.toIntExact(pipelineTableLogPO.id);

                } catch (Exception e) {
                    DispatchExceptionHandlingDTO dispatchExceptionHandlingDTO = DispatchExceptionHandlingDTO.builder().build();
                    dispatchExceptionHandlingDTO.comment = nifiStagePO.comment;
                    dispatchExceptionHandlingDTO.pipelTraceId = nifiStageMessageDTO.pipelTraceId;
                    dispatchExceptionHandlingDTO.pipelJobTraceId = nifiStageMessageDTO.pipelJobTraceId;
                    dispatchExceptionHandlingDTO.pipelStageTraceId = nifiStageMessageDTO.pipelStageTraceId;
                    dispatchExceptionHandlingDTO.pipelTaskTraceId = nifiStageMessageDTO.pipelTaskTraceId;
                    log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
                    try {
                        iPipelJobLog.exceptionHandlingLog(dispatchExceptionHandlingDTO);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }

                }
            }
        } catch (Exception e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
        } finally {
            if (acke != null) {
                acke.acknowledge();
            }
        }
    }

    public void sendPublishCenter(NifiStageMessageDTO nifiStageMessage, NifiCustomWorkflowDetailDTO itselfPort, String topic) {
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
        try {
            iPipelJobLog.exceptionHandlingLog(dispatchExceptionHandlingDTO);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        kafkaTemplateHelper.sendMessageAsync(topic, param);
    }

    @Override
    public Object overlayCodePreview(OverLoadCodeDTO dto) {
        IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(dto.dataSourceType);
        return dbCommand.assemblySql(dto.config, dto.synchronousTypeEnum, dto.funcName, dto.buildNifiFlow);
    }

    /**
     * 数据接入--首页展示信息--当日接入数据总量
     *
     * @return
     */
    @Override
    public Long accessDataTotalCount() {
        LambdaQueryWrapper<PipelineTableLogPO> wrapper = new LambdaQueryWrapper<>();
        List<Integer> types = new ArrayList<>();
//        3 物理表
        types.add(3);
//        11 RESTFULAPI
        types.add(11);
        //开始时间
        Date start = LocalDateUtil.strToDateLong(LocalDateUtil.dateToStr(new Date(), Locale.CHINA) + " 00:00:00");
        //结束时间
        Date end = LocalDateUtil.strToDateLong(LocalDateUtil.dateToStr(new Date(), Locale.CHINA) + " 23:59:59");
        wrapper.select(PipelineTableLogPO::getCounts)
                .in(PipelineTableLogPO::getTableType, types)
//                .eq(PipelineTableLogPO::getTableType, 3)
//                .eq(PipelineTableLogPO::getTableType, 11)
                .between(PipelineTableLogPO::getCreateTime, start, end);
        List<PipelineTableLogPO> list = iPipelineTableLog.list(wrapper);
        long count = 0;
        for (PipelineTableLogPO po : list) {
            if (po != null) {
                if (po.getCounts() != null){
                    count += po.getCounts();
                }
            }
        }

        //查询nifi同步的数据量
        LambdaQueryWrapper<TBETLlogPO> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.select(TBETLlogPO::getDatarows)
                .between(TBETLlogPO::getCreatetime, start, end);
        List<TBETLlogPO> list1 = etlLog.list(wrapper1);
        long count1 = 0;
        for (TBETLlogPO tbetLlogPO : list1) {
            if (tbetLlogPO != null) {
                if (tbetLlogPO.getDatarows() != null) {
                    count1 += tbetLlogPO.getDatarows();
                }
            }
        }

        return count + count1;
    }

    /**
     * 数据接入--首页展示信息--当日接入数据的成功次数和失败次数
     *
     * @return
     */
    @Override
    public AccessDataSuccessAndFailCountDTO accessDataSuccessAndFailCount() {
        //查询成功的
        LambdaQueryWrapper<PipelineTableLogPO> wrapper = new LambdaQueryWrapper<>();
        ArrayList<Integer> types = new ArrayList<>();
        //3 物理表
        types.add(3);
        //11 RESTFULAPI
        types.add(11);
        //开始时间
        Date start = LocalDateUtil.strToDateLong(LocalDateUtil.dateToStr(new Date(), Locale.CHINA) + " 00:00:00");
        //结束时间
        Date end = LocalDateUtil.strToDateLong(LocalDateUtil.dateToStr(new Date(), Locale.CHINA) + " 23:59:59");
        wrapper.select(PipelineTableLogPO::getId)
                .in(PipelineTableLogPO::getTableType, types)
                //3成功
                .eq(PipelineTableLogPO::getState, 3)
                .between(PipelineTableLogPO::getCreateTime, start, end);
        int sCount = iPipelineTableLog.count(wrapper);
//        List<PipelineTableLogPO> successList = iPipelineTableLog.list(wrapper);

        //查询失败的
        LambdaQueryWrapper<PipelineTableLogPO> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.select(PipelineTableLogPO::getId)
                .in(PipelineTableLogPO::getTableType, types)
                //4失败
                .eq(PipelineTableLogPO::getState, 4)
                .between(PipelineTableLogPO::getCreateTime, start, end);
        int fCount = iPipelineTableLog.count(wrapper1);
//        List<PipelineTableLogPO> failureList = iPipelineTableLog.list(wrapper1);

        //查询nifi同步的数据 当天的成功次数和失败次数
        LambdaQueryWrapper<TBETLlogPO> wrapper2 = new LambdaQueryWrapper<>();
        //1成功的
        wrapper2.select(TBETLlogPO::getId)
                .eq(TBETLlogPO::getStatus, 1)
                .between(TBETLlogPO::getCreatetime, start, end)
                .between(TBETLlogPO::getStartdate, start, end);
        int successCount = etlLog.count(wrapper2);
//        int successCount = etlLog.list(wrapper2).size();

        LambdaQueryWrapper<TBETLlogPO> wrapper3 = new LambdaQueryWrapper<>();
        //2失败的
        wrapper3.select(TBETLlogPO::getId)
                .eq(TBETLlogPO::getStatus, 2)
                .between(TBETLlogPO::getCreatetime, start, end);
        int failCount = etlLog.count(wrapper3);
//        int failCount = etlLog.list(wrapper3).size();

        AccessDataSuccessAndFailCountDTO dto = new AccessDataSuccessAndFailCountDTO();
        dto.setSuccessCount(sCount + successCount);
//        dto.setSuccessCount(successList.size() + successCount);
        dto.setFailCount(fCount + failCount);
//        dto.setFailCount(failureList.size() + failCount);
        return dto;
    }


}
