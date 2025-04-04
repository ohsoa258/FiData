package com.fisk.task.pipeline2;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.consumeserveice.client.ConsumeServeiceClient;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.datafactory.dto.customworkflowdetail.DispatchJobHierarchyDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyNextDTO;
import com.fisk.datafactory.dto.tasknifi.TaskHierarchyDTO;
import com.fisk.dataservice.dto.tableservice.TableServiceEmailDTO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.BuildTableApiServiceDTO;
import com.fisk.task.dto.task.BuildTableServiceDTO;
import com.fisk.task.entity.PipelTaskLogPO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.pipeline.IPipelineTaskPublishCenter;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.service.nifi.IJdbcBuild;
import com.fisk.task.service.nifi.IOlap;
import com.fisk.task.service.nifi.ITableNifiSettingService;
import com.fisk.task.utils.KafkaTemplateHelper;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.fisk.task.pipeline2.HeartbeatService.buildDispatchExceptionHandling;

/**
 * 任务结束中心
 *
 * @author cfk
 */
@Slf4j
@Component
public class MissionEndCenter {
    @Value("${consumer-server-enable}")
    private Boolean consumerServerEnable;
    @Value("${pipeline-async-switch}")
    private Boolean pipelineAsyncSwitch;
    @Resource
    IOlap iOlap;
    @Resource
    IPipelineTaskPublishCenter iPipelineTaskPublishCenter;
    @Resource
    RedisUtil redisUtil;
    @Resource
    IPipelTaskLog iPipelTaskLog;
    @Resource
    IPipelJobLog iPipelJobLog;
    @Resource
    IPipelLog iPipelLog;
    @Resource
    KafkaTemplateHelper kafkaTemplateHelper;
    @Resource
    ConsumeServeiceClient consumeServeiceClient;
    @Resource
    UserClient userClient;

    @Resource
    private DataFactoryClient dataFactoryClient;
    @Resource
    ITableNifiSettingService iTableNifiSettingService;
    @Resource
    IJdbcBuild iJdbcBuild;
    @Value("${nifi.pipeline.dispatch-email-url-prefix}")
    private String dispatchEmailUrlPrefix;


    public void missionEndCenter(String data, Acknowledgment acke) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            log.info("missionEndCenter参数:{}", data);
            data = "[" + data + "]";
            List<String> msg = JSON.parseArray(data, String.class);
            for (String dto : msg) {

                KafkaReceiveDTO kafkaReceive = JSON.parseObject(dto, KafkaReceiveDTO.class);
                try {
                    Boolean setnx;
                    do {
                        Thread.sleep(300);
//                    log.info("missionEndCenter获取锁PipelLock:{}",kafkaReceive.pipelTraceId);
                        setnx = redisUtil.setnx("PipelLock:" + kafkaReceive.pipelTraceId, 100, TimeUnit.SECONDS);
                    } while (!setnx);
                    String topic = kafkaReceive.topic;
                    String pipelTraceId = kafkaReceive.pipelTraceId;
                    String[] split = topic.split("\\.");
                    String tableId = "";
                    if (split.length == 7) {
                        String pipelineId = split[3];
                        String pipelJobTraceId = kafkaReceive.pipelJobTraceId;
                        if (!Objects.equals(Integer.parseInt(split[4]), OlapTableEnum.CUSTOMIZESCRIPT.getValue()) &&
                                !Objects.equals(Integer.parseInt(split[4]), OlapTableEnum.SFTPFILECOPYTASK.getValue()) &&
                                !Objects.equals(Integer.parseInt(split[4]), OlapTableEnum.POWERBIDATASETREFRESHTASK.getValue())
                        ) {
                            //没有表id就把任务id扔进去
                            tableId = split[6];
                        }
                        NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(pipelineId, Integer.parseInt(split[4]), null, Integer.parseInt(StringUtils.isEmpty(tableId) ? "0" : tableId));
                        if (Objects.equals(Integer.parseInt(split[4]), OlapTableEnum.CUSTOMIZESCRIPT.getValue()) ||
                                Objects.equals(Integer.parseInt(split[4]), OlapTableEnum.SFTPFILECOPYTASK.getValue()) ||
                                Objects.equals(Integer.parseInt(split[4]), OlapTableEnum.POWERBIDATASETREFRESHTASK.getValue())
                        ) {
                            //没有表id就把任务id扔进去
                            nifiGetPortHierarchy.nifiCustomWorkflowDetailId = Long.valueOf(split[6]);
                        }
                        TaskHierarchyDTO nifiPortHierarchy = iPipelineTaskPublishCenter.getNifiPortHierarchy(nifiGetPortHierarchy, kafkaReceive.pipelTraceId);
                        NifiCustomWorkflowDetailDTO itselfPort = nifiPortHierarchy.itselfPort;
                        DispatchJobHierarchyDTO dispatchJobHierarchy = iPipelineTaskPublishCenter.getDispatchJobHierarchyByTaskId(pipelTraceId, String.valueOf(itselfPort.id));
                        Map<Integer, Object> taskMap = new HashMap<>();
                        String format = simpleDateFormat.format(new Date());
                        TaskHierarchyDTO taskHierarchyDto = iPipelineTaskPublishCenter.getTaskHierarchy(pipelTraceId, String.valueOf(nifiPortHierarchy.id));
                        log.info("任务结束中心本节点状态:{},{}", taskHierarchyDto.id, taskHierarchyDto.taskStatus);
                        if (Objects.equals(taskHierarchyDto.taskStatus, DispatchLogEnum.taskfailure)) {

                            taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + format + " - ErrorMessage:" + kafkaReceive.getMessage());

                        } else if (Objects.equals(taskHierarchyDto.taskStatus, DispatchLogEnum.taskpass)) {

                            //taskMap.put(DispatchLogEnum.taskstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + format);
                            taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.PASS.getName() + " - " + format);

                        } else if (Objects.nonNull(taskHierarchyDto.itselfPort) && !taskHierarchyDto.itselfPort.forbidden) {
                            //禁止运行
                            taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.FORBIDDEN.getName() + " - " + format);
                        } else if (!Objects.equals(taskHierarchyDto.taskStatus, DispatchLogEnum.taskpass) && !Objects.equals(taskHierarchyDto.taskStatus, DispatchLogEnum.taskfailure)) {
                            Map<Object, Object> pipelTask = redisUtil.getAndDel(RedisKeyEnum.PIPEL_TASK.getName() + ":" + kafkaReceive.pipelTaskTraceId);
                            log.info(itselfPort.id + "拿打印条数89" + JSON.toJSONString(pipelTask));
                            Object endTime = pipelTask.get(DispatchLogEnum.taskend.getName());
                            Object count = pipelTask.get(DispatchLogEnum.taskcount.getName());
                            taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + (endTime != null ? endTime.toString() : simpleDateFormat.format(new Date())) + " - 同步条数 : " + (Objects.nonNull(kafkaReceive.numbers) ? (Objects.isNull(count) || "null".equals(count) ? 0 : count) : kafkaReceive.numbers));
                        }
                        iPipelTaskLog.savePipelTaskLog(pipelTraceId, pipelJobTraceId, nifiPortHierarchy.taskTraceId, taskMap, String.valueOf(nifiPortHierarchy.id), itselfPort.tableId, Integer.parseInt(split[4]));
                        if (pipelineAsyncSwitch) {
                            //查找当前组件内的最后一个task(因为最后一个节点的nextList会有下一个组件第一个任务)
                            TaskHierarchyDTO lastTaskHierarchyDTO = null;
                            Map<Object, Object> hmTask = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + pipelTraceId);
                            boolean flag = true;
                            int num = 0;
                            List<String> ds = new ArrayList<>();

                            Iterator<Map.Entry<Object, Object>> taskNodeMap = hmTask.entrySet().iterator();
                            while (taskNodeMap.hasNext()) {
                                Map.Entry<Object, Object> next = taskNodeMap.next();
                                TaskHierarchyDTO taskNodeHierarchyDTO = JSON.parseObject(next.getValue().toString(), TaskHierarchyDTO.class);
                                if (taskNodeHierarchyDTO.itselfPort.pid.intValue() == taskHierarchyDto.itselfPort.pid) {
                                    num++;
//                                    log.info("任务结束中心本job节点task状态:{}", taskNodeHierarchyDTO);
                                    if (taskNodeHierarchyDTO.taskStatus == DispatchLogEnum.tasknorun ||
                                            taskNodeHierarchyDTO.taskStatus == DispatchLogEnum.taskstart) {
                                        flag = false;
                                        ds.add(String.valueOf(taskNodeHierarchyDTO.itselfPort.id));
                                    }
                                    if (taskNodeHierarchyDTO.pipeEndFlag) {
                                        lastTaskHierarchyDTO = taskNodeHierarchyDTO;
                                    }
                                }
                            }
                            log.info("当前组件JobTraceId:{} 执行进度:{},当前任务taskTraceId:{},未执行管道详情id:{}", kafkaReceive.pipelJobTraceId, (num - ds.size()) + "/" + num, kafkaReceive.pipelTaskTraceId, JSON.toJSONString(ds));
                            //判断组件内任务是否全部执行完毕
                            if (flag) {
                                //记录本节点的job的结束
                                Map<Integer, Object> jobMap = new HashMap<>();
                                if (Objects.equals(dispatchJobHierarchy.jobStatus, NifiStageTypeEnum.RUN_FAILED)) {
                                    jobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + simpleDateFormat.format(new Date()));
                                } else if (Objects.equals(dispatchJobHierarchy.jobStatus, NifiStageTypeEnum.PASS)) {
                                    jobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.PASS.getName() + " - " + simpleDateFormat.format(new Date()));
                                } else if (!dispatchJobHierarchy.forbidden) {
                                    //job禁止运行
                                    jobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.FORBIDDEN.getName() + " - " + simpleDateFormat.format(new Date()));
                                } else {
                                    jobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + simpleDateFormat.format(new Date()));
                                }
                                log.info("记录本节点的job的结束,JobTraceId:{}", kafkaReceive.pipelJobTraceId);
                                iPipelJobLog.savePipelJobLog(pipelTraceId, jobMap, pipelineId, dispatchJobHierarchy.jobTraceId, String.valueOf(dispatchJobHierarchy.id));
                                //判断当前组件后面是否还有组件
                                if (CollectionUtils.isNotEmpty(lastTaskHierarchyDTO.nextList)) {
                                    //发送消息给任务发布中心  topic是 : task.build.task.publish
                                    log.info("任务结束中心发送给任务发布中心的参数:{}", JSON.toJSONString(kafkaReceive));
                                    kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_TASK_PUBLISH_FLOW, JSON.toJSONString(kafkaReceive));
                                } else {
                                    //记录管道结束
                                    log.info("尝试记录管道结束");
                                    Map<Integer, Object> pipelMap = new HashMap<>();
                                    Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_JOB_TRACE_ID.getName() + ":" + pipelTraceId);
                                    boolean success = true;
                                    boolean ifNext = true;
                                    Long jobId = 0L;
                                    Iterator<Map.Entry<Object, Object>> jobNodeMap = hmget.entrySet().iterator();
                                    while (jobNodeMap.hasNext()) {
                                        Map.Entry<Object, Object> next = jobNodeMap.next();
                                        DispatchJobHierarchyDTO jobHierarchy = JSON.parseObject(next.getValue().toString(), DispatchJobHierarchyDTO.class);
                                        if (Objects.equals(jobHierarchy.jobStatus, NifiStageTypeEnum.PASS) || Objects.equals(jobHierarchy.jobStatus, NifiStageTypeEnum.RUN_FAILED)) {
                                            success = false;
                                        }
                                        //判断所有组件是否全部执行完毕
                                        if (!jobHierarchy.jobProcessed) {
                                            ifNext = false;
                                            jobId = jobHierarchy.id;
                                            break;
                                        }
                                    }
                                    if (ifNext) {
                                        log.info("开始记录管道结束");
                                        String status = NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName();
                                        String endDateTime = simpleDateFormat.format(new Date());
                                        if (success) {
                                            pipelMap.put(DispatchLogEnum.pipelend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + endDateTime);
                                        } else {
                                            pipelMap.put(DispatchLogEnum.pipelend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + endDateTime);
                                            status = NifiStageTypeEnum.RUN_FAILED.getName();
                                        }
                                        log.info("consumerServerEnable参数，{}", consumerServerEnable);
                                        if (consumerServerEnable) {
                                            // 通过管道id,查询关联表服务
                                            ResultEntity<List<BuildTableServiceDTO>> tableResult = consumeServeiceClient.getTableListByPipelineId(Integer.valueOf(pipelineId));
                                            if (tableResult != null && tableResult.code == ResultEnum.SUCCESS.getCode() && CollectionUtils.isNotEmpty(tableResult.data)) {
                                                List<BuildTableServiceDTO> list = tableResult.data;
                                                for (BuildTableServiceDTO buildTableService : list) {
                                                    if (buildTableService.enable == 0) {
                                                        continue;
                                                    }
                                                    KafkaReceiveDTO kafkaRkeceive = KafkaReceiveDTO.builder().build();
                                                    kafkaRkeceive.topic = MqConstants.TopicPrefix.TOPIC_PREFIX + OlapTableEnum.DATASERVICES.getValue() + "." + buildTableService.tableAppId + "." + buildTableService.id;
                                                    kafkaRkeceive.start_time = simpleDateFormat.format(new Date());
                                                    kafkaRkeceive.pipelTaskTraceId = UUID.randomUUID().toString();
                                                    kafkaRkeceive.fidata_batch_code = kafkaRkeceive.pipelTaskTraceId;
                                                    kafkaRkeceive.pipelStageTraceId = UUID.randomUUID().toString();
                                                    kafkaRkeceive.ifTaskStart = true;
                                                    kafkaRkeceive.topicType = TopicTypeEnum.DAILY_NIFI_FLOW.getValue();
                                                    //pc.universalPublish(kafkaRkeceiveDTO);
                                                    log.info("表服务关联触发流程参数:{}", JSON.toJSONString(kafkaRkeceive));
                                                    kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_TASK_PUBLISH_FLOW, JSON.toJSONString(kafkaRkeceive));
                                                }
                                            }
                                            // 通过管道id,查询关联api服务
                                            ResultEntity<List<BuildTableApiServiceDTO>> tableApiResult = consumeServeiceClient.getTableApiListByPipelineId(Integer.valueOf(pipelineId));
                                            if (tableApiResult != null && tableApiResult.code == ResultEnum.SUCCESS.getCode() && CollectionUtils.isNotEmpty(tableApiResult.data)) {
                                                List<BuildTableApiServiceDTO> list = tableApiResult.data;
                                                for (BuildTableApiServiceDTO buildTableApiService : list) {
                                                    if (buildTableApiService.enable == 0) {
                                                        continue;
                                                    }
                                                    KafkaReceiveDTO kafkaRkeceive = KafkaReceiveDTO.builder().build();
                                                    kafkaRkeceive.topic = MqConstants.TopicPrefix.TOPIC_PREFIX + OlapTableEnum.DATA_SERVICE_API.getValue() + "." + buildTableApiService.getAppId() + "." + buildTableApiService.id;
                                                    kafkaRkeceive.start_time = simpleDateFormat.format(new Date());
                                                    kafkaRkeceive.pipelTaskTraceId = UUID.randomUUID().toString();
                                                    kafkaRkeceive.fidata_batch_code = kafkaRkeceive.pipelTaskTraceId;
                                                    kafkaRkeceive.pipelStageTraceId = UUID.randomUUID().toString();
                                                    kafkaRkeceive.ifTaskStart = true;
                                                    kafkaRkeceive.topicType = TopicTypeEnum.DAILY_NIFI_FLOW.getValue();
                                                    //pc.universalPublish(kafkaRkeceiveDTO);
                                                    log.info("表服务关联触发流程参数:{}", JSON.toJSONString(kafkaRkeceive));
                                                    kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_TASK_PUBLISH_FLOW, JSON.toJSONString(kafkaRkeceive));
                                                }
                                            }
                                        }
                                        iPipelLog.savePipelLog(pipelTraceId, pipelMap, pipelineId);
                                        NifiCustomWorkflowDTO nifiCustomWorkflowDTO = new NifiCustomWorkflowDTO();
                                        nifiCustomWorkflowDTO.setId(Long.parseLong(pipelineId));
                                        nifiCustomWorkflowDTO.setStatus(1);
                                        nifiCustomWorkflowDTO.setLastStatus(status);
                                        nifiCustomWorkflowDTO.setLastDateTime(endDateTime);
                                        dataFactoryClient.updatePublishStatus(nifiCustomWorkflowDTO);
                                    } else {
                                        log.info("管道尚未结束:jobId:{}", jobId);
                                    }
                                }
                            } else {
                                log.info("组件内其余任务未执行完毕,当前任务:pipelTraceId:{},pipelJobTraceId:{},pipelTaskTraceId:{}", kafkaReceive.pipelTraceId, kafkaReceive.pipelJobTraceId, kafkaReceive.pipelTaskTraceId);
                            }
                        } else {
                            //同步顺序按执行管道的方法
                            // 先检查本级状态,判断是否应该记本级所在job的结束,或者管道结束
                            List<NifiPortsHierarchyNextDTO> nextList = nifiPortHierarchy.nextList;
                            if (CollectionUtils.isNotEmpty(nextList)) {
                                NifiPortsHierarchyNextDTO nifiPortsHierarchyNext = nextList.get(0);
                                TaskHierarchyDTO taskHierarchy = iPipelineTaskPublishCenter.getTaskHierarchy(pipelTraceId, String.valueOf(nifiPortsHierarchyNext.itselfPort));
                                NifiCustomWorkflowDetailDTO itselfPort1 = taskHierarchy.itselfPort;
                                if (!Objects.equals(itselfPort1.pid, itselfPort.pid)) {
                                    //记录本节点的job的结束
                                    Map<Integer, Object> jobMap = new HashMap<>();
                                    log.info("任务结束中心本节点所在组状态:{},{}", dispatchJobHierarchy.id, dispatchJobHierarchy.jobStatus);
                                    if (Objects.equals(dispatchJobHierarchy.jobStatus, NifiStageTypeEnum.RUN_FAILED)) {
                                        jobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + simpleDateFormat.format(new Date()));
                                    } else if (Objects.equals(dispatchJobHierarchy.jobStatus, NifiStageTypeEnum.PASS)) {
                                        jobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.PASS.getName() + " - " + simpleDateFormat.format(new Date()));
                                    } else if (!dispatchJobHierarchy.forbidden) {
                                        //job禁止运行
                                        jobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.FORBIDDEN.getName() + " - " + simpleDateFormat.format(new Date()));
                                    } else {
                                        jobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + simpleDateFormat.format(new Date()));
                                    }
                                    iPipelJobLog.savePipelJobLog(pipelTraceId, jobMap, pipelineId, dispatchJobHierarchy.jobTraceId, String.valueOf(dispatchJobHierarchy.id));
                                }
                                // 第三步 发送消息给任务发布中心  topic是 : task.build.task.publish
                                log.info("任务结束中心发送给任务发布中心的参数:{}", JSON.toJSONString(kafkaReceive));
                                kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_TASK_PUBLISH_FLOW, JSON.toJSONString(kafkaReceive));
                            } else {
                                //记录本节点的job的结束
                                Map<Integer, Object> jobMap = new HashMap<>();
                                if (Objects.equals(dispatchJobHierarchy.jobStatus, NifiStageTypeEnum.RUN_FAILED)) {
                                    jobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + simpleDateFormat.format(new Date()));
                                } else if (Objects.equals(dispatchJobHierarchy.jobStatus, NifiStageTypeEnum.PASS)) {
                                    jobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.PASS.getName() + " - " + simpleDateFormat.format(new Date()));
                                } else if (!dispatchJobHierarchy.forbidden) {
                                    //job禁止运行
                                    jobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.FORBIDDEN.getName() + " - " + simpleDateFormat.format(new Date()));
                                } else {
                                    jobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + simpleDateFormat.format(new Date()));
                                }
                                iPipelJobLog.savePipelJobLog(pipelTraceId, jobMap, pipelineId, dispatchJobHierarchy.jobTraceId, String.valueOf(dispatchJobHierarchy.id));
                                //记录管道结束
                                log.info("尝试记录管道结束");
                                Map<Integer, Object> pipelMap = new HashMap<>();
                                Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_JOB_TRACE_ID.getName() + ":" + pipelTraceId);
                                boolean success = true;
                                boolean ifNext = true;
                                Long jobId = 0L;
                                Iterator<Map.Entry<Object, Object>> nodeMap = hmget.entrySet().iterator();
                                while (nodeMap.hasNext()) {
                                    Map.Entry<Object, Object> next = nodeMap.next();
                                    DispatchJobHierarchyDTO jobHierarchy = JSON.parseObject(next.getValue().toString(), DispatchJobHierarchyDTO.class);
                                    if (Objects.equals(jobHierarchy.jobStatus, NifiStageTypeEnum.PASS) || Objects.equals(jobHierarchy.jobStatus, NifiStageTypeEnum.RUN_FAILED)) {
                                        success = false;
                                    }
                                    if (!jobHierarchy.jobProcessed) {
                                        ifNext = false;
                                        jobId = jobHierarchy.id;
                                        break;
                                    }
                                }
                                if (ifNext) {
                                    log.info("开始记录管道结束");
                                    String status =NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName();
                                    String endDateTime = simpleDateFormat.format(new Date());
                                    if (success) {
                                        pipelMap.put(DispatchLogEnum.pipelend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + endDateTime);
                                    } else {
                                        pipelMap.put(DispatchLogEnum.pipelend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + endDateTime);
                                        status = NifiStageTypeEnum.RUN_FAILED.getName();
                                    }
                                    log.info("consumerServerEnable参数，{}", consumerServerEnable);
                                    if (consumerServerEnable) {
                                        // 通过管道id,查询关联表服务
                                        ResultEntity<List<BuildTableServiceDTO>> tableResult = consumeServeiceClient.getTableListByPipelineId(Integer.valueOf(pipelineId));
                                        if (tableResult != null && tableResult.code == ResultEnum.SUCCESS.getCode() && CollectionUtils.isNotEmpty(tableResult.data)) {
                                            List<BuildTableServiceDTO> list = tableResult.data;
                                            for (BuildTableServiceDTO buildTableService : list) {
                                                if (buildTableService.enable == 0) {
                                                    continue;
                                                }
                                                KafkaReceiveDTO kafkaRkeceive = KafkaReceiveDTO.builder().build();
                                                kafkaRkeceive.topic = MqConstants.TopicPrefix.TOPIC_PREFIX + OlapTableEnum.DATASERVICES.getValue() + "." + buildTableService.tableAppId + "." + buildTableService.id;
                                                kafkaRkeceive.start_time = simpleDateFormat.format(new Date());
                                                kafkaRkeceive.pipelTaskTraceId = UUID.randomUUID().toString();
                                                kafkaRkeceive.fidata_batch_code = kafkaRkeceive.pipelTaskTraceId;
                                                kafkaRkeceive.pipelStageTraceId = UUID.randomUUID().toString();
                                                kafkaRkeceive.ifTaskStart = true;
                                                kafkaRkeceive.topicType = TopicTypeEnum.DAILY_NIFI_FLOW.getValue();
                                                //pc.universalPublish(kafkaRkeceiveDTO);
                                                log.info("表服务关联触发流程参数:{}", JSON.toJSONString(kafkaRkeceive));
                                                kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_TASK_PUBLISH_FLOW, JSON.toJSONString(kafkaRkeceive));
                                            }
                                        }
                                        // 通过管道id,查询关联api服务
                                        ResultEntity<List<BuildTableApiServiceDTO>> tableApiResult = consumeServeiceClient.getTableApiListByPipelineId(Integer.valueOf(pipelineId));
                                        if (tableApiResult != null && tableApiResult.code == ResultEnum.SUCCESS.getCode() && CollectionUtils.isNotEmpty(tableApiResult.data)) {
                                            List<BuildTableApiServiceDTO> list = tableApiResult.data;
                                            for (BuildTableApiServiceDTO buildTableApiService : list) {
                                                if (buildTableApiService.enable == 0) {
                                                    continue;
                                                }
                                                KafkaReceiveDTO kafkaRkeceive = KafkaReceiveDTO.builder().build();
                                                kafkaRkeceive.topic = MqConstants.TopicPrefix.TOPIC_PREFIX + OlapTableEnum.DATA_SERVICE_API.getValue() + "." + buildTableApiService.getAppId() + "." + buildTableApiService.id;
                                                kafkaRkeceive.start_time = simpleDateFormat.format(new Date());
                                                kafkaRkeceive.pipelTaskTraceId = UUID.randomUUID().toString();
                                                kafkaRkeceive.fidata_batch_code = kafkaRkeceive.pipelTaskTraceId;
                                                kafkaRkeceive.pipelStageTraceId = UUID.randomUUID().toString();
                                                kafkaRkeceive.ifTaskStart = true;
                                                kafkaRkeceive.topicType = TopicTypeEnum.DAILY_NIFI_FLOW.getValue();
                                                //pc.universalPublish(kafkaRkeceiveDTO);
                                                log.info("表服务关联触发流程参数:{}", JSON.toJSONString(kafkaRkeceive));
                                                kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_TASK_PUBLISH_FLOW, JSON.toJSONString(kafkaRkeceive));
                                            }
                                        }
                                    }
                                    iPipelLog.savePipelLog(pipelTraceId, pipelMap, pipelineId);
                                    NifiCustomWorkflowDTO nifiCustomWorkflowDTO = new NifiCustomWorkflowDTO();
                                    nifiCustomWorkflowDTO.setId(Long.parseLong(pipelineId));
                                    nifiCustomWorkflowDTO.setStatus(1);
                                    nifiCustomWorkflowDTO.setLastStatus(status);
                                    nifiCustomWorkflowDTO.setLastDateTime(endDateTime);
                                    dataFactoryClient.updatePublishStatus(nifiCustomWorkflowDTO);
                                } else {
                                    log.info("管道尚未结束:jobId:{}", jobId);
                                }
                            }
                        }
                    } else if (split.length == 6) {
                        if (Objects.equals(kafkaReceive.topicType, TopicTypeEnum.DAILY_NIFI_FLOW.getValue())) {
                            Map<Integer, Object> taskMap = new HashMap<>();
                            String format = simpleDateFormat.format(new Date());
                            taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + format + " - 同步条数 : " + (Objects.isNull(kafkaReceive.numbers) ? 0 : kafkaReceive.numbers));
                            iPipelTaskLog.savePipelTaskLog(null, null, kafkaReceive.pipelTaskTraceId, taskMap, null, split[5], Integer.parseInt(split[3]));
                            //-------------------------------------------------------------
                            //如果dmp_dw数仓类型是doris，则不再删除临时temp表
                            ResultEntity<DataSourceDTO> resultEntity = userClient.getFiDataDataSourceById(1);
                            DataSourceTypeEnum conType = resultEntity.getData().getConType();
                            if (!conType.equals(DataSourceTypeEnum.DORIS)){
                                //如果是事实维度表要删掉临时表
                                if (Objects.equals(Integer.parseInt(split[3]), OlapTableEnum.DIMENSION.getValue())
                                        || Objects.equals(Integer.parseInt(split[3]), OlapTableEnum.FACT.getValue())
                                ) {
                                    TableNifiSettingPO tableNifiSetting = iTableNifiSettingService.query().eq("table_access_id", split[5]).eq("type", split[3]).one();
                                    String tableName = tableNifiSetting.tableName;
                                    String dropSql = "DROP TABLE IF EXISTS temp_" + tableName;
                                    dropSql += ";DROP TABLE IF EXISTS stg_" + tableName;
                                    iJdbcBuild.postgreBuildTable(dropSql, BusinessTypeEnum.DATAMODEL);
                                }
                            }


                            log.info("开始执行脚本");
                            log.info("consumerServerEnable参数，{}", consumerServerEnable);
                            if (consumerServerEnable && Objects.equals(Integer.parseInt(split[3]), OlapTableEnum.DATASERVICES.getValue())) {
                                log.info("确定是表服务");
                                TableServiceEmailDTO tableServiceEmailDTO = new TableServiceEmailDTO();
                                tableServiceEmailDTO.appType = 1;
                                tableServiceEmailDTO.appId = Integer.parseInt(split[4]);
                                tableServiceEmailDTO.msg = taskMap.get(DispatchLogEnum.taskend.getValue()).toString();
                                tableServiceEmailDTO.result = "【运行成功】";
                                tableServiceEmailDTO.pipelTraceId = pipelTraceId;
                                List<PipelTaskLogPO> pos = iPipelTaskLog.query()
                                        .eq("task_trace_id", kafkaReceive.pipelTaskTraceId)
                                        .eq("type", DispatchLogEnum.taskstart.getValue())
                                        .list();
                                if (CollectionUtils.isNotEmpty(pos)) {
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
                                    hashMap.put("表名", String.valueOf(Integer.valueOf(split[5])));
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
                            }
                            //-------------------------------------------------------------
                        }
                    }
                    redisUtil.del("PipelLock:" + kafkaReceive.pipelTraceId);
                } catch (Exception e) {
                    log.error("任务结束中心报错:{}", StackTraceHelper.getStackTraceInfo(e));
                    redisUtil.del("PipelLock:" + kafkaReceive.pipelTraceId);
                    DispatchExceptionHandlingDTO exceptionDto = buildDispatchExceptionHandling(kafkaReceive);
                    try {
                        iPipelJobLog.exceptionHandlingLog(exceptionDto);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        } catch (Exception e) {
            log.error("任务结束中心报错:{}", StackTraceHelper.getStackTraceInfo(e));
        } finally {
            if (acke != null) {
                acke.acknowledge();
            }
        }


    }

    public void execSql(String customScriptAfter, Integer targetDbId) {
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(targetDbId);
        if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
            DataSourceDTO dataSource = fiDataDataSource.data;
            Connection conn = null;
            Statement st = null;
            try {
                Class.forName(dataSource.conType.getDriverName());
                conn = DriverManager.getConnection(dataSource.conStr, dataSource.conAccount, dataSource.conPassword);
                st = conn.createStatement();
                //无需判断ddl语句执行结果,因为如果执行失败会进catch
                log.info("开始执行脚本:{}", customScriptAfter);
                st.execute(customScriptAfter);
            } catch (Exception e) {
                log.error(StackTraceHelper.getStackTraceInfo(e));
                throw new FkException(ResultEnum.ERROR);
            } finally {
                try {
                    st.close();
                    conn.close();
                } catch (SQLException e) {
                    log.error(StackTraceHelper.getStackTraceInfo(e));
                    throw new FkException(ResultEnum.ERROR);
                }

            }
        } else {
            log.error("userclient无法查询到目标库的连接信息");
            throw new FkException(ResultEnum.ERROR);
        }

    }


}
