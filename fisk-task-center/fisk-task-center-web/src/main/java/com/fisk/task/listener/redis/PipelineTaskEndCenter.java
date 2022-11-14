package com.fisk.task.listener.redis;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.api.ApiImportDataDTO;
import com.fisk.dataaccess.dto.api.PipelApiDispatchDTO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.entity.PipelJobLogPO;
import com.fisk.task.entity.PipelTaskLogPO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.pipeline.IPipelineTaskPublishCenter;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.service.nifi.IOlap;
import com.fisk.task.service.nifi.ITableNifiSettingService;
import com.fisk.task.utils.KafkaTemplateHelper;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class PipelineTaskEndCenter extends KeyExpirationEventMessageListener {
    @Resource
    KafkaTemplateHelper kafkaTemplateHelper;
    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    private DataFactoryClient dataFactoryClient;
    @Resource
    IOlap iOlap;
    @Resource
    IPipelJobLog iPipelJobLog;
    @Resource
    IPipelLog iPipelLog;
    @Resource
    IPipelTaskLog iPipelTaskLog;
    @Resource
    RedisUtil redisUtil;
    @Resource
    IPipelineTaskPublishCenter iPipelineTaskPublishCenter;
    @Resource
    ITableNifiSettingService iTableNifiSettingService;


    public PipelineTaskEndCenter(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 针对redis数据失效事件，进行数据处理
     *
     * @param message
     * @param pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 用户做自己的业务处理即可,注意message.toString()可以获取失效的key
        String expiredKey = message.toString();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("即将调用的节点:" + expiredKey);
        String thisPipelTaskTraceId = UUID.randomUUID().toString();
        String thisPipelJobTraceId = UUID.randomUUID().toString();
        String thisPipelStageTraceId = UUID.randomUUID().toString();
        String upPipelJobTraceId = "";
        String pipelTraceId = "";
        String upJobName = "";
        String JobName = "";
        String pipelName = "";
        try {
            //用户key失效不做处理
            if (expiredKey.toLowerCase().startsWith(MqConstants.TopicPrefix.TOPIC_PREFIX)) {
                //分割
                String[] split1 = expiredKey.split(",");
                String topic = split1[0];
                String[] split = topic.split("\\.");
                pipelTraceId = split1[1];
                boolean ifEndJob = false;
                //查找本次需要结束的任务
                NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(split[3], Integer.valueOf(split[4]), null, Integer.valueOf(split[6]));
                NifiPortsHierarchyDTO data =
                        iPipelineTaskPublishCenter.getNifiPortHierarchy(nifiGetPortHierarchy, pipelTraceId);
                NifiCustomWorkflowDetailDTO itselfPort = data.itselfPort;
                JobName = itselfPort.componentsName;
                List<NifiCustomWorkflowDetailDTO> inportList = data.inportList;
                for (NifiCustomWorkflowDetailDTO dto : inportList) {
                    //上一级的信息
                    PipelJobLogPO byPipelTraceId = iPipelJobLog.getByPipelTraceId(pipelTraceId, dto.pid);
                    PipelTaskLogPO byPipelJobTraceId = iPipelTaskLog.getByPipelJobTraceId(byPipelTraceId.jobTraceId, dto.id);
                    String pipelTaskTraceId = byPipelJobTraceId.taskTraceId;
                    // 1.要记录上一个task结束
                    Map<Integer, Object> taskMap = new HashMap<>();
                    upJobName = dto.componentsName;
                    Map<Object, Object> pipelTask = redisUtil.getAndDel(RedisKeyEnum.PIPEL_TASK.getName() + ":" + byPipelJobTraceId.taskId);
                    Object endTime = pipelTask.get(DispatchLogEnum.taskend.getName());
                    Object count = pipelTask.get(DispatchLogEnum.taskcount.getName());
                    //upJobName + "-" + dto.tableOrder + " " +
                    taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + (endTime != null ? endTime.toString() : simpleDateFormat.format(new Date())) + " - 同步条数 : " + (Objects.isNull(count) ? 0 : count));
                    //taskMap.put(DispatchLogEnum.taskend.getValue(), upJobName + "-" + dto.tableOrder + " " + (endTime != null ? endTime.toString() : simpleDateFormat.format(new Date())));
                    ChannelDataEnum value = ChannelDataEnum.getValue(dto.componentType);
                    OlapTableEnum olapTableEnum = ChannelDataEnum.getOlapTableEnum(value.getValue());
                    log.info("第五处调用保存task日志,记录上一个task结束" + byPipelJobTraceId.taskTraceId);
                    iPipelTaskLog.savePipelTaskLog(byPipelJobTraceId.jobTraceId, byPipelJobTraceId.taskTraceId, taskMap, byPipelJobTraceId.taskId, dto.tableId, olapTableEnum.getValue());
                    if (!Objects.equals(itselfPort.pid, dto.pid)) {
                        //说明这个组结束了
                        //2.记录上一个job结束
                        Map<Integer, Object> upJobMap = new HashMap<>();
                        //upJobMap.put(DispatchLogEnum.jobstate.getValue(), upJobName + " " + NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());  upJobName
                        upJobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + (endTime != null ? endTime.toString() : simpleDateFormat.format(new Date())));
                        log.info("上一个job的结束:" + byPipelJobTraceId.jobTraceId);
                        iPipelJobLog.savePipelJobLog(pipelTraceId, upJobMap, split[3], byPipelJobTraceId.jobTraceId, byPipelTraceId.componentId);
                        ifEndJob = true;
                    } else {
                        upPipelJobTraceId = byPipelJobTraceId.jobTraceId;
                    }
                }
                if (ifEndJob) {
                    //3.记录这个job开始
                    Map<Integer, Object> thisJobMap = new HashMap<>();
                    //thisJobMap.put(DispatchLogEnum.jobstate.getValue(), JobName + " " + NifiStageTypeEnum.RUNNING.getName()); JobName
                    thisJobMap.put(DispatchLogEnum.jobstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                    log.info("这个job的开始:" + thisPipelJobTraceId);
                    iPipelJobLog.savePipelJobLog(pipelTraceId, thisJobMap, split[3], thisPipelJobTraceId, String.valueOf(itselfPort.pid));
                    //4.记录这个task开始
                    Map<Integer, Object> thisTaskMap = new HashMap<>();
                    //thisTaskMap.put(DispatchLogEnum.taskstate.getValue(), JobName + "-" + itselfPort.tableOrder + " " + NifiStageTypeEnum.RUNNING.getName());
                    thisTaskMap.put(DispatchLogEnum.taskstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                    log.info("第六处调用保存task日志这个task开始" + thisPipelTaskTraceId);
                    iPipelTaskLog.savePipelTaskLog(thisPipelJobTraceId, thisPipelTaskTraceId, thisTaskMap, String.valueOf(itselfPort.id), split[6], Integer.parseInt(split[4]));
                } else {
                    //4.记录这个task开始
                    Map<Integer, Object> thisTaskMap = new HashMap<>();
                    //thisTaskMap.put(DispatchLogEnum.taskstate.getValue(), JobName + "-" + itselfPort.tableOrder + " " + NifiStageTypeEnum.RUNNING.getName());
                    thisTaskMap.put(DispatchLogEnum.taskstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                    log.info("第七处调用保存task日志,记录这个task开始" + thisPipelTaskTraceId);
                    iPipelTaskLog.savePipelTaskLog(upPipelJobTraceId, thisPipelTaskTraceId, thisTaskMap, String.valueOf(itselfPort.id), split[6], Integer.parseInt(split[4]));
                    thisPipelJobTraceId = upPipelJobTraceId;
                }
                //此时,expiredKey就是即将要调用的节点,需要发消息topic_name就是expiredKey
                String tableType = split[4];
                int type = Integer.parseInt(tableType);
                if (Objects.equals(type, OlapTableEnum.PHYSICS_API.getValue())) {
                    //非实时api发布
                    ApiImportDataDTO apiImportDataDTO = new ApiImportDataDTO();
                    apiImportDataDTO.workflowId = split[3];
                    apiImportDataDTO.appId = Long.parseLong(split[5]);
                    apiImportDataDTO.apiId = Long.parseLong(split[6]);
                    apiImportDataDTO.pipelTraceId = pipelTraceId;
                    apiImportDataDTO.pipelJobTraceId = thisPipelJobTraceId;
                    apiImportDataDTO.pipelTaskTraceId = thisPipelTaskTraceId;
                    apiImportDataDTO.pipelStageTraceId = thisPipelStageTraceId;
                    PipelApiDispatchDTO pipelApiDispatchDTO = new PipelApiDispatchDTO();
                    pipelApiDispatchDTO.apiId = Long.parseLong(split[6]);
                    pipelApiDispatchDTO.appId = Long.parseLong(split[5]);
                    pipelApiDispatchDTO.workflowId = split[3];
                    pipelApiDispatchDTO.pipelineId = data.pipelineId;
                    apiImportDataDTO.pipelApiDispatch = JSON.toJSONString(pipelApiDispatchDTO);
                    dataAccessClient.importData(apiImportDataDTO);
                } else {
                    KafkaReceiveDTO kafkaReceiveDTO = new KafkaReceiveDTO();
                    kafkaReceiveDTO.pipelTraceId = pipelTraceId;
                    kafkaReceiveDTO.pipelJobTraceId = thisPipelJobTraceId;
                    kafkaReceiveDTO.pipelTaskTraceId = thisPipelTaskTraceId;
                    kafkaReceiveDTO.pipelStageTraceId = thisPipelStageTraceId;
                    kafkaReceiveDTO.fidata_batch_code = pipelTraceId;
                    kafkaReceiveDTO.start_time = simpleDateFormat.format(new Date());
                    kafkaReceiveDTO.topicType = TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue();
                    log.info("发送的topic4:{},内容:{}", split1[0], JSON.toJSONString(kafkaReceiveDTO));
                    kafkaTemplateHelper.sendMessageAsync(split1[0], JSON.toJSONString(kafkaReceiveDTO));
                }
            } else if (expiredKey.startsWith("fiskgd")) {
                //整个管道记录结束
                pipelTraceId = expiredKey.substring(7);
                String pipelId = "";
                List<PipelJobLogPO> list = iPipelJobLog.query().eq("pipel_trace_id", pipelTraceId).eq("del_flag", 1)
                        .isNotNull("pipel_id").orderByDesc("create_time").list();
                if (CollectionUtils.isNotEmpty(list)) {
                    pipelId = list.get(0).pipelId;
                    ResultEntity<List<NifiCustomWorkflowDetailDTO>> nifiPortTaskLastListById = dataFactoryClient.getNifiPortTaskLastListById(Long.valueOf(pipelId));
                    List<NifiCustomWorkflowDetailDTO> data = nifiPortTaskLastListById.data;
                    for (NifiCustomWorkflowDetailDTO dto : data) {
                        String taskId = String.valueOf(dto.id);
                        JobName = dto.componentsName;
                        pipelName = dto.workflowName;
                        PipelJobLogPO byPipelTraceId = iPipelJobLog.getByPipelTraceId(pipelTraceId, dto.pid);
                        PipelTaskLogPO byPipelJobTraceId = iPipelTaskLog.getByPipelJobTraceId(byPipelTraceId.jobTraceId, dto.id);
                        //记录task结束
                        Map<Integer, Object> taskMap = new HashMap<>();
                        //taskMap.put(DispatchLogEnum.taskstate.getValue(), JobName + "-" + dto.tableOrder + " " + NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());
                        Map<Object, Object> pipelTask = redisUtil.getAndDel(RedisKeyEnum.PIPEL_TASK.getName() + ":" + byPipelJobTraceId.taskId);
                        Object endTime = pipelTask.get(DispatchLogEnum.taskend.getName());
                        Object count = pipelTask.get(DispatchLogEnum.taskcount.getName());
                        taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + (endTime != null ? endTime.toString() : simpleDateFormat.format(new Date())) + " - 同步条数 : " + (Objects.isNull(count) ? 0 : count));
                        log.info("第八处调用保存task日志");
                        iPipelTaskLog.savePipelTaskLog(byPipelJobTraceId.jobTraceId, byPipelJobTraceId.taskTraceId, taskMap, byPipelJobTraceId.taskId, null, 0);
                        //记录job结束
                        Map<Integer, Object> upJobMap = new HashMap<>();
                        //upJobMap.put(DispatchLogEnum.jobstate.getValue(), JobName + NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());
                        upJobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + (endTime != null ? endTime.toString() : simpleDateFormat.format(new Date())));
                        log.info("这个job的结束:" + byPipelJobTraceId.jobTraceId);
                        iPipelJobLog.savePipelJobLog(pipelTraceId, upJobMap, byPipelTraceId.pipelId, byPipelJobTraceId.jobTraceId, byPipelTraceId.componentId);
                    }
                    //记录管道结束
                    Map<Integer, Object> PipelMap = new HashMap<>();
                    PipelMap.put(DispatchLogEnum.pipelend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + simpleDateFormat.format(new Date()));
                    //PipelMap.put(DispatchLogEnum.pipelstate.getValue(), pipelName + " " + NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());
                    log.info("这个管道的结束:" + pipelTraceId);
                    redisUtil.del(RedisKeyEnum.PIPEL_TRACE_ID.getName() + pipelTraceId);
                    log.info("第二处调用保存job日志");
                    iPipelJobLog.savePipelLog(pipelTraceId, PipelMap, pipelId);
                    iPipelLog.savePipelLog(pipelTraceId, PipelMap, pipelId);
                }
            } else if (expiredKey.startsWith("nowExec")) {
                //手动调度记录结束
                String[] split = expiredKey.split(",");
                String taskTraceId = split[0].substring(7);
                String[] split1 = split[1].split("\\.");
                HashMap<Integer, Object> taskMap = new HashMap<>();
                TableNifiSettingPO tableNifiSetting = iTableNifiSettingService.getByTableId(Long.parseLong(split1[5]), Long.parseLong(split1[3]));
                //taskMap.put(DispatchLogEnum.taskstate.getValue(), tableNifiSetting.tableName + " " + NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());
                Map<Object, Object> pipelTask = redisUtil.getAndDel(RedisKeyEnum.PIPEL_TASK.getName() + ":" + taskTraceId);
                Object endTime = pipelTask.get(DispatchLogEnum.taskend.getName());
                Object count = pipelTask.get(DispatchLogEnum.taskcount.getName());
                taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + (endTime != null ? endTime.toString() : simpleDateFormat.format(new Date())) + " : 同步条数 : " + (Objects.isNull(count) ? 0 : count));
                log.info("第九处调用保存task日志");
                iPipelTaskLog.savePipelTaskLog(null, taskTraceId, taskMap, null, split1[5], Integer.parseInt(split1[3]));
            }
        } catch (Exception e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
            DispatchExceptionHandlingDTO dto = new DispatchExceptionHandlingDTO();
            dto.pipelTraceId = pipelTraceId;
            dto.pipelJobTraceId = thisPipelJobTraceId;
            dto.pipelTaskTraceId = thisPipelTaskTraceId;
            dto.comment = "查找下一级报错";
            dto.pipleName = pipelName;
            dto.JobName = JobName;
            iPipelJobLog.exceptionHandlingLog(dto);
        }
    }
}