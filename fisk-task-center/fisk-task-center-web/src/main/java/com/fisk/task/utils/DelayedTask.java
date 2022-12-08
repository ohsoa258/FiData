package com.fisk.task.utils;

import com.alibaba.fastjson.JSON;
import com.davis.client.model.ProcessGroupEntity;
import com.davis.client.model.ProcessGroupStatusDTO;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.framework.mdc.MDCHelper;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.api.ApiImportDataDTO;
import com.fisk.dataaccess.dto.api.PipelApiDispatchDTO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.DispatchJobHierarchyDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.TaskHierarchyDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.ExecScriptDTO;
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
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author cfk
 */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Component
public class DelayedTask extends TimerTask {
    @Value("${nifi.pipeline.waitTime}")
    private String waitTime;

    KafkaTemplateHelper kafkaTemplateHelper;

    DataAccessClient dataAccessClient;

    private DataFactoryClient dataFactoryClient;

    IOlap iOlap;

    IPipelJobLog iPipelJobLog;

    IPipelLog iPipelLog;

    IPipelTaskLog iPipelTaskLog;

    RedisUtil redisUtil;

    IPipelineTaskPublishCenter iPipelineTaskPublishCenter;

    ITableNifiSettingService iTableNifiSettingService;

    PublishTaskController publishTaskController;

    private String param;

    private String groupId;

    private String value;

    private String message;

    private String upTaskId;

    /**
     * @param message                    上级报错信息
     * @param value                      验证是否还有延时任务  值为topic+uuid
     * @param groupId                    上级所在任务组,如果是nifi流程的话就有
     * @param param                      本级topic
     * @param kafkaTemplateHelper
     * @param dataFactoryClient
     * @param iOlap
     * @param iPipelJobLog
     * @param iPipelLog
     * @param iPipelTaskLog
     * @param redisUtil
     * @param iTableNifiSettingService
     * @param dataAccessClient
     * @param iPipelineTaskPublishCenter
     * @param publishTaskController
     */
    public DelayedTask(String upTaskId, String message, String value, String groupId, String param, KafkaTemplateHelper kafkaTemplateHelper,
                       DataFactoryClient dataFactoryClient,
                       IOlap iOlap, IPipelJobLog iPipelJobLog, IPipelLog iPipelLog,
                       IPipelTaskLog iPipelTaskLog, RedisUtil redisUtil,
                       ITableNifiSettingService iTableNifiSettingService,
                       DataAccessClient dataAccessClient,
                       IPipelineTaskPublishCenter iPipelineTaskPublishCenter,
                       PublishTaskController publishTaskController
    ) {
        this.upTaskId = upTaskId;
        this.message = message;
        this.value = value;
        this.groupId = groupId;
        this.param = param;
        this.kafkaTemplateHelper = kafkaTemplateHelper;
        this.dataAccessClient = dataAccessClient;
        this.dataFactoryClient = dataFactoryClient;
        this.iOlap = iOlap;
        this.iPipelJobLog = iPipelJobLog;
        this.iPipelLog = iPipelLog;
        this.iPipelTaskLog = iPipelTaskLog;
        this.redisUtil = redisUtil;
        this.iTableNifiSettingService = iTableNifiSettingService;
        this.iPipelineTaskPublishCenter = iPipelineTaskPublishCenter;
        this.publishTaskController = publishTaskController;

    }

    @Override
    public void run() {

        try {
            Thread.sleep(100);
            boolean exist = redisUtil.hasKey(param);
            String delayedTask="";
            if (exist) {
                log.info("key还没失效" + param);
                delayedTask = redisUtil.get(RedisKeyEnum.DELAYED_TASK.getName() + ":" + param).toString();
                log.info("打印两个值:{},{}", delayedTask, value);
                if (!Objects.equals(delayedTask, value)) {
                    return;
                }
            }
            log.info("比较结果:{},{}",exist,!Objects.equals(delayedTask, value));
            Timer timer = new Timer();
            //只有是nifi处理的任务才有这个groupId
            if (StringUtils.isNotEmpty(groupId)) {
                ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(groupId);
                ProcessGroupStatusDTO status = processGroup.getStatus();
                //flowFilesQueued 组内流文件数量,如果为0代表组内所有流文件执行完,没有正在执行的组件
                Integer flowFilesQueued = status.getAggregateSnapshot().getFlowFilesQueued();
                if (!Objects.equals(flowFilesQueued, 0)) {
                    String value = UUID.randomUUID().toString();
                    //刷新时间和创建key或者修改value,会产生延时任务
                    redisUtil.set(RedisKeyEnum.DELAYED_TASK.getName() + ":" + param, value, 3000);
                    DelayedTask delayedTaskAgain = new DelayedTask(upTaskId, message, value, groupId, param, kafkaTemplateHelper, dataFactoryClient, iOlap, iPipelJobLog, iPipelLog, iPipelTaskLog, redisUtil, iTableNifiSettingService, dataAccessClient, iPipelineTaskPublishCenter, publishTaskController);
                    timer.schedule(delayedTaskAgain, Long.parseLong(waitTime) * 1000);
                }
                TableNifiSettingPO tableNifiSetting = iTableNifiSettingService.query().eq("table_component_id", groupId).one();

            }


        } catch (Exception e) {
            log.error("查看组状态报错");
        }
        // 用户做自己的业务处理即可,注意message.toString()可以获取失效的key
        String expiredKey = param;
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
                NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(split[3], Integer.parseInt(split[4]), null, Integer.valueOf(split[6]));
                if (Integer.parseInt(split[4]) == OlapTableEnum.CUSTOMIZESCRIPT.getValue()) {
                    //没有表id就把任务id扔进去
                    nifiGetPortHierarchy.nifiCustomWorkflowDetailId = Long.valueOf(split[6]);
                }
                TaskHierarchyDTO data =
                        iPipelineTaskPublishCenter.getNifiPortHierarchy(nifiGetPortHierarchy, pipelTraceId);
                NifiCustomWorkflowDetailDTO itselfPort = data.itselfPort;
                thisPipelJobTraceId = iPipelineTaskPublishCenter.getDispatchJobHierarchy(pipelTraceId, String.valueOf(itselfPort.pid)).jobTraceId;
                JobName = itselfPort.componentsName;
                Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + pipelTraceId);
                TaskHierarchyDTO taskHierarchy = JSON.parseObject(hmget.get(upTaskId).toString(), TaskHierarchyDTO.class);
                log.info("上一个task的状态" + taskHierarchy.taskStatus.getName());
                if (Objects.equals(taskHierarchy.taskStatus, DispatchLogEnum.taskfailure) || Objects.equals(taskHierarchy.taskStatus, DispatchLogEnum.taskpass)) {
                    failureAndSkipping(pipelTraceId, upTaskId, String.valueOf(itselfPort.id), param);
                    return;
                }
                List<Long> inportList = data.inportList.stream().distinct().collect(Collectors.toList());

                log.info("此处查到的所有上游id:" + JSON.toJSONString(inportList));
                for (Long inportId : inportList) {
                    //上一级的信息
                    NifiGetPortHierarchyDTO nextHierarchy = new NifiGetPortHierarchyDTO();
                    nextHierarchy.nifiCustomWorkflowDetailId = inportId;
                    nextHierarchy.workflowId = split[3];
                    TaskHierarchyDTO taskUp = iPipelineTaskPublishCenter.getNifiPortHierarchy(nextHierarchy, pipelTraceId);
                    NifiCustomWorkflowDetailDTO dto = taskUp.itselfPort;
                    //PipelJobLogPO byPipelTraceId = iPipelJobLog.getByPipelTraceId(pipelTraceId, dto.pid);
                    DispatchJobHierarchyDTO jobHierarchy = iPipelineTaskPublishCenter.getDispatchJobHierarchy(pipelTraceId, String.valueOf(dto.pid));
                    //TaskHierarchyDTO upTaskHierarchy = iPipelineTaskPublishCenter.getTaskHierarchy(pipelTraceId, String.valueOf(dto.id));
                    PipelTaskLogPO byPipelJobTraceId = iPipelTaskLog.getByPipelJobTraceId(jobHierarchy.jobTraceId, dto.id);
                    //String pipelTaskTraceId = byPipelJobTraceId.taskTraceId;
                    // 1.要记录上一个task结束
                    Map<Integer, Object> taskMap = new HashMap<>();
                    upJobName = dto.componentsName;
                    Map<Object, Object> pipelTask = redisUtil.getAndDel(RedisKeyEnum.PIPEL_TASK.getName() + ":" + inportId);
                    Object endTime = pipelTask.get(DispatchLogEnum.taskend.getName());
                    Object count = pipelTask.get(DispatchLogEnum.taskcount.getName());
                    //upJobName + "-" + dto.tableOrder + " " +
                    taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + (endTime != null ? endTime.toString() : simpleDateFormat.format(new Date())) + " - 同步条数 : " + (Objects.isNull(count) ? 0 : count));
                    //taskMap.put(DispatchLogEnum.taskend.getValue(), upJobName + "-" + dto.tableOrder + " " + (endTime != null ? endTime.toString() : simpleDateFormat.format(new Date())));
                    ChannelDataEnum value = ChannelDataEnum.getValue(dto.componentType);
                    OlapTableEnum olapTableEnum = ChannelDataEnum.getOlapTableEnum(value.getValue());
                    iPipelTaskLog.savePipelTaskLog(pipelTraceId, jobHierarchy.jobTraceId, byPipelJobTraceId.taskTraceId, taskMap, String.valueOf(inportId), dto.tableId, olapTableEnum.getValue());
                    if (!Objects.equals(itselfPort.pid, dto.pid)) {
                        //说明这个组结束了
                        //2.记录上一个job结束
                        Map<Integer, Object> upJobMap = new HashMap<>();
                        //upJobMap.put(DispatchLogEnum.jobstate.getValue(), upJobName + " " + NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());  upJobName
                        upJobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + (endTime != null ? endTime.toString() : simpleDateFormat.format(new Date())));
                        iPipelJobLog.savePipelJobLog(pipelTraceId, upJobMap, split[3], jobHierarchy.jobTraceId, String.valueOf(dto.pid));
                        ifEndJob = true;
                    } else {
                        upPipelJobTraceId = jobHierarchy.jobTraceId;
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
                    iPipelTaskLog.savePipelTaskLog(pipelTraceId, thisPipelJobTraceId, thisPipelTaskTraceId, thisTaskMap, String.valueOf(itselfPort.id), split[6], Integer.parseInt(split[4]));
                } else {
                    //4.记录这个task开始
                    Map<Integer, Object> thisTaskMap = new HashMap<>();
                    //thisTaskMap.put(DispatchLogEnum.taskstate.getValue(), JobName + "-" + itselfPort.tableOrder + " " + NifiStageTypeEnum.RUNNING.getName());
                    thisTaskMap.put(DispatchLogEnum.taskstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                    log.info("第七处调用保存task日志,记录这个task开始" + thisPipelTaskTraceId);
                    iPipelTaskLog.savePipelTaskLog(pipelTraceId, upPipelJobTraceId, thisPipelTaskTraceId, thisTaskMap, String.valueOf(itselfPort.id), split[6], Integer.parseInt(split[4]));
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
                } else if (Objects.equals(type, OlapTableEnum.CUSTOMIZESCRIPT.getValue())) {
                    ExecScriptDTO execScript = new ExecScriptDTO();
                    execScript.pipelJobTraceId = thisPipelJobTraceId;
                    execScript.pipelTaskTraceId = thisPipelTaskTraceId;
                    execScript.pipelTraceId = pipelTraceId;
                    execScript.taskId = split[6];
                    kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_EXEC_SCRIPT_FLOW, JSON.toJSONString(execScript));
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
                        //-----------------------------------------------------------------------
                        Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + pipelTraceId);
                        TaskHierarchyDTO taskHierarchy = JSON.parseObject(hmget.get(upTaskId).toString(), TaskHierarchyDTO.class);
                        log.info("上一个task的状态" + taskHierarchy.taskStatus.getName());
                        if (Objects.equals(taskHierarchy.taskStatus, DispatchLogEnum.taskfailure) || Objects.equals(taskHierarchy.taskStatus, DispatchLogEnum.taskpass)) {
                            failureAndSkipping(pipelTraceId, upTaskId, taskId, param);
                            return;
                        }
                        //-----------------------------------------------------------------------

                        JobName = dto.componentsName;
                        pipelName = dto.workflowName;
                        //PipelJobLogPO byPipelTraceId = iPipelJobLog.getByPipelTraceId(pipelTraceId, dto.pid);
                        DispatchJobHierarchyDTO dispatchJobHierarchy = iPipelineTaskPublishCenter.getDispatchJobHierarchy(pipelTraceId, String.valueOf(dto.pid));
                        PipelTaskLogPO byPipelJobTraceId = iPipelTaskLog.getByPipelJobTraceId(dispatchJobHierarchy.jobTraceId, dto.id);
                        //记录task结束
                        Map<Integer, Object> taskMap = new HashMap<>();
                        //taskMap.put(DispatchLogEnum.taskstate.getValue(), JobName + "-" + dto.tableOrder + " " + NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());
                        Map<Object, Object> pipelTask = redisUtil.getAndDel(RedisKeyEnum.PIPEL_TASK.getName() + ":" + byPipelJobTraceId.taskId);
                        log.info(taskId + "拿打印条数287" + JSON.toJSONString(pipelTask));
                        Object endTime = pipelTask.get(DispatchLogEnum.taskend.getName());
                        Object count = pipelTask.get(DispatchLogEnum.taskcount.getName());
                        taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + (endTime != null ? endTime.toString() : simpleDateFormat.format(new Date())) + " - 同步条数 : " + (Objects.isNull(count) ? 0 : count));
                        log.info("第八处调用保存task日志");
                        iPipelTaskLog.savePipelTaskLog(pipelTraceId, byPipelJobTraceId.jobTraceId, byPipelJobTraceId.taskTraceId, taskMap, byPipelJobTraceId.taskId, null, 0);
                        //记录job结束
                        Map<Integer, Object> upJobMap = new HashMap<>();
                        //upJobMap.put(DispatchLogEnum.jobstate.getValue(), JobName + NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());
                        upJobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + (endTime != null ? endTime.toString() : simpleDateFormat.format(new Date())));
                        log.info("这个job的结束:" + byPipelJobTraceId.jobTraceId);
                        iPipelJobLog.savePipelJobLog(pipelTraceId, upJobMap, pipelId, byPipelJobTraceId.jobTraceId, String.valueOf(dto.pid));
                    }
                    //记录管道结束
                    Map<Integer, Object> pipelMap = new HashMap<>();
                    Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_JOB_TRACE_ID.getName() + ":" + pipelTraceId);
                    boolean success = true;
                    Iterator<Map.Entry<Object, Object>> nodeMap = hmget.entrySet().iterator();
                    while (nodeMap.hasNext()) {
                        Map.Entry<Object, Object> next = nodeMap.next();
                        DispatchJobHierarchyDTO jobHierarchy = JSON.parseObject(next.getValue().toString(), DispatchJobHierarchyDTO.class);
                        if (Objects.equals(jobHierarchy.jobStatus, NifiStageTypeEnum.PASS) || Objects.equals(jobHierarchy.jobStatus, NifiStageTypeEnum.RUN_FAILED)) {
                            success = false;
                        }
                    }
                    if (success) {
                        pipelMap.put(DispatchLogEnum.pipelend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + simpleDateFormat.format(new Date()));
                    } else {
                        pipelMap.put(DispatchLogEnum.pipelend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + simpleDateFormat.format(new Date()));
                    }

                    //PipelMap.put(DispatchLogEnum.pipelstate.getValue(), pipelName + " " + NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());
                    log.info("这个管道的结束:" + pipelTraceId);
                    //redisUtil.del(RedisKeyEnum.PIPEL_TRACE_ID.getName() + ":" + pipelTraceId);
                    //redisUtil.del(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + pipelTraceId);
                    log.info("第二处调用保存job日志");
                    iPipelJobLog.savePipelLog(pipelTraceId, pipelMap, pipelId);
                    iPipelLog.savePipelLog(pipelTraceId, pipelMap, pipelId);
                }
                MDCHelper.removePipelTraceId();
            } else if (expiredKey.startsWith("nowExec")) {
                //手动调度记录结束
                String[] split = expiredKey.split(",");
                String taskTraceId = split[0].substring(7);
                String[] split1 = split[1].split("\\.");
                HashMap<Integer, Object> taskMap = new HashMap<>();
                TableNifiSettingPO tableNifiSetting = iTableNifiSettingService.getByTableId(Long.parseLong(split1[5]), Long.parseLong(split1[3]));
                //taskMap.put(DispatchLogEnum.taskstate.getValue(), tableNifiSetting.tableName + " " + NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());
                Map<Object, Object> pipelTask = redisUtil.getAndDel(RedisKeyEnum.PIPEL_TASK.getName() + ":" + taskTraceId);
                log.info(taskTraceId + "拿打印条数336" + JSON.toJSONString(pipelTask));
                Object endTime = pipelTask.get(DispatchLogEnum.taskend.getName());
                Object count = pipelTask.get(DispatchLogEnum.taskcount.getName());
                taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + (endTime != null ? endTime.toString() : simpleDateFormat.format(new Date())) + " : 同步条数 : " + (Objects.isNull(count) ? 0 : count));
                log.info("第九处调用保存task日志");
                iPipelTaskLog.savePipelTaskLog(null, null, taskTraceId, taskMap, null, split1[5], Integer.parseInt(split1[3]));
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

    /**
     * 参数要有本次调度的traceid和本节点任务id
     */
    public void failureAndSkipping(String pipelTraceId, String upTaskId, String thisTaskId, String topic) {
        log.info("本次失效调度参数{},{},{},{}", pipelTraceId, upTaskId, thisTaskId, topic);
        if (topic.startsWith("nowExec")) {
            return;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + pipelTraceId);
        TaskHierarchyDTO upTaskHierarchy = JSON.parseObject(hmget.get(upTaskId).toString(), TaskHierarchyDTO.class);
        NifiCustomWorkflowDetailDTO upTask = upTaskHierarchy.itselfPort;
        TaskHierarchyDTO thisTaskHierarchy = JSON.parseObject(hmget.get(thisTaskId).toString(), TaskHierarchyDTO.class);
        NifiCustomWorkflowDetailDTO thisTask = thisTaskHierarchy.itselfPort;
        String jobTraceId = UUID.randomUUID().toString();
        String ThisJobTraceId = UUID.randomUUID().toString();
        String taskTraceId = UUID.randomUUID().toString();
        List<PipelJobLogPO> upList = iPipelJobLog.query().eq("pipel_trace_id", pipelTraceId).eq("component_id", upTask.pid).list();
        if (CollectionUtils.isNotEmpty(upList)) {
            jobTraceId = upList.get(0).jobTraceId;
        }
        List<PipelTaskLogPO> list = iPipelTaskLog.query().eq("job_trace_id", jobTraceId).eq("task_id", upTask.id).list();
        if (CollectionUtils.isNotEmpty(list)) {
            taskTraceId = list.get(0).taskTraceId;
        }
        if (Objects.equals(upTaskHierarchy.taskStatus, DispatchLogEnum.taskpass)) {
            Map<Integer, Object> upTaskMap = new HashMap<>();
            upTaskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.PASS.getName() + " - " + simpleDateFormat.format(new Date()));
            // 上一级task结束--跳过 发送卡夫卡消息到任务发布中心
            ChannelDataEnum channel = ChannelDataEnum.getValue(upTask.componentType);
            OlapTableEnum olapTableEnum = ChannelDataEnum.getOlapTableEnum(channel.getValue());
            iPipelTaskLog.savePipelTaskLog(pipelTraceId, jobTraceId, taskTraceId, upTaskMap, upTaskId, upTask.tableId, olapTableEnum.getValue());
            if (!Objects.equals(upTask.pid, thisTask.pid)) {
                // 上一个job的结束--跳过
                Map<Integer, Object> upJobMap = new HashMap<>();
                upJobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.PASS.getName() + " - " + simpleDateFormat.format(new Date()));
                iPipelJobLog.savePipelJobLog(pipelTraceId, upJobMap, String.valueOf(upTaskHierarchy.pipelineId), jobTraceId, String.valueOf(upTask.pid));
            } else {
                ThisJobTraceId = jobTraceId;
            }

        } else if (Objects.equals(upTaskHierarchy.taskStatus, DispatchLogEnum.taskfailure)) {
            // 本级失败,创建延时队列,发送卡夫卡消息到任务发布中心,但是上级做失败,本级记跳过
            Map<Integer, Object> upTaskMap = new HashMap<>();
            upTaskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + simpleDateFormat.format(new Date()));
            // 上一级task结束--跳过 发送卡夫卡消息到任务发布中心
            ChannelDataEnum channel = ChannelDataEnum.getValue(upTask.componentType);
            OlapTableEnum olapTableEnum = ChannelDataEnum.getOlapTableEnum(channel.getValue());
            iPipelTaskLog.savePipelTaskLog(pipelTraceId, jobTraceId, taskTraceId, upTaskMap, upTaskId, upTask.tableId, olapTableEnum.getValue());
            if (!Objects.equals(upTask.pid, thisTask.pid)) {
                // 上一个job的结束--跳过
                Map<Integer, Object> upJobMap = new HashMap<>();
                upJobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + simpleDateFormat.format(new Date()));
                iPipelJobLog.savePipelJobLog(pipelTraceId, upJobMap, String.valueOf(upTaskHierarchy.pipelineId), jobTraceId, String.valueOf(upTask.pid));
            }
        }
        KafkaReceiveDTO kafkaReceive = new KafkaReceiveDTO();
        kafkaReceive.nifiCustomWorkflowDetailId = Long.valueOf(thisTaskId);
        kafkaReceive.pipelTraceId = pipelTraceId;
        kafkaReceive.pipelJobTraceId = ThisJobTraceId;
        kafkaReceive.pipelTaskTraceId = UUID.randomUUID().toString();
        kafkaReceive.topicType = TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue();
        if (StringUtils.isNotEmpty(thisTask.tableId)) {
            kafkaReceive.tableId = Integer.valueOf(thisTask.tableId);
        }
        ChannelDataEnum channel = ChannelDataEnum.getValue(thisTask.componentType);
        OlapTableEnum olapTableEnum = ChannelDataEnum.getOlapTableEnum(channel.getValue());
        kafkaReceive.tableType = olapTableEnum.getValue();
        String[] split = topic.split(",");
        kafkaReceive.topic = split[0];
        if (topic.startsWith("fiskgd")) {
            return;
        }
        kafkaTemplateHelper.sendMessageAsync("my-topic", JSON.toJSONString(kafkaReceive));

    }

    public void recordPipelEnd(String expiredKey,String JobName,String pipelName){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //整个管道记录结束
        String pipelTraceId = expiredKey.substring(7);
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
                //PipelJobLogPO byPipelTraceId = iPipelJobLog.getByPipelTraceId(pipelTraceId, dto.pid);
                DispatchJobHierarchyDTO dispatchJobHierarchy = iPipelineTaskPublishCenter.getDispatchJobHierarchy(pipelTraceId, String.valueOf(dto.pid));
                PipelTaskLogPO byPipelJobTraceId = iPipelTaskLog.getByPipelJobTraceId(dispatchJobHierarchy.jobTraceId, dto.id);
                //记录task结束
                Map<Integer, Object> taskMap = new HashMap<>();
                //taskMap.put(DispatchLogEnum.taskstate.getValue(), JobName + "-" + dto.tableOrder + " " + NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());
                Map<Object, Object> pipelTask = redisUtil.getAndDel(RedisKeyEnum.PIPEL_TASK.getName() + ":" + byPipelJobTraceId.taskId);
                log.info(taskId + "拿打印条数287" + JSON.toJSONString(pipelTask));
                Object endTime = pipelTask.get(DispatchLogEnum.taskend.getName());
                Object count = pipelTask.get(DispatchLogEnum.taskcount.getName());
                taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + (endTime != null ? endTime.toString() : simpleDateFormat.format(new Date())) + " - 同步条数 : " + (Objects.isNull(count) ? 0 : count));
                log.info("第八处调用保存task日志");
                iPipelTaskLog.savePipelTaskLog(pipelTraceId, byPipelJobTraceId.jobTraceId, byPipelJobTraceId.taskTraceId, taskMap, byPipelJobTraceId.taskId, null, 0);
                //记录job结束
                Map<Integer, Object> upJobMap = new HashMap<>();
                //upJobMap.put(DispatchLogEnum.jobstate.getValue(), JobName + NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());
                upJobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + (endTime != null ? endTime.toString() : simpleDateFormat.format(new Date())));
                log.info("这个job的结束:" + byPipelJobTraceId.jobTraceId);
                iPipelJobLog.savePipelJobLog(pipelTraceId, upJobMap, pipelId, byPipelJobTraceId.jobTraceId, String.valueOf(dto.pid));
            }
            //记录管道结束
            Map<Integer, Object> pipelMap = new HashMap<>();
            Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_JOB_TRACE_ID.getName() + ":" + pipelTraceId);
            boolean success = true;
            Iterator<Map.Entry<Object, Object>> nodeMap = hmget.entrySet().iterator();
            while (nodeMap.hasNext()) {
                Map.Entry<Object, Object> next = nodeMap.next();
                DispatchJobHierarchyDTO jobHierarchy = JSON.parseObject(next.getValue().toString(), DispatchJobHierarchyDTO.class);
                if (Objects.equals(jobHierarchy.jobStatus, NifiStageTypeEnum.PASS) || Objects.equals(jobHierarchy.jobStatus, NifiStageTypeEnum.RUN_FAILED)) {
                    success = false;
                }
            }
            if (success) {
                pipelMap.put(DispatchLogEnum.pipelend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + simpleDateFormat.format(new Date()));
            } else {
                pipelMap.put(DispatchLogEnum.pipelend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + simpleDateFormat.format(new Date()));
            }

            //PipelMap.put(DispatchLogEnum.pipelstate.getValue(), pipelName + " " + NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());
            log.info("这个管道的结束:" + pipelTraceId);
            //redisUtil.del(RedisKeyEnum.PIPEL_TRACE_ID.getName() + ":" + pipelTraceId);
            //redisUtil.del(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + pipelTraceId);
            log.info("第二处调用保存job日志");
            iPipelJobLog.savePipelLog(pipelTraceId, pipelMap, pipelId);
            iPipelLog.savePipelLog(pipelTraceId, pipelMap, pipelId);
        }
        MDCHelper.removePipelTraceId();
    }
}
