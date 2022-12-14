package com.fisk.task.pipeline2;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.datafactory.dto.customworkflowdetail.DispatchJobHierarchyDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyNextDTO;
import com.fisk.datafactory.dto.tasknifi.TaskHierarchyDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.pipeline.IPipelineTaskPublishCenter;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.service.nifi.IOlap;
import com.fisk.task.utils.KafkaTemplateHelper;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 任务结束中心
 *
 * @author cfk
 */
@Slf4j
@Component
public class MissionEndCenter {
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


    public void missionEndCenter(String data, Acknowledgment acke) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            log.info("missionEndCenter参数:{}", data);
            data = "[" + data + "]";
            List<String> msg = JSON.parseArray(data, String.class);
            for (String dto : msg) {

                KafkaReceiveDTO kafkaReceive = JSON.parseObject(dto, KafkaReceiveDTO.class);
                String topic = kafkaReceive.topic;
                String pipelTraceId = kafkaReceive.pipelTraceId;
                String[] split = topic.split("\\.");
                String tableId = "";
                if (split.length == 7) {
                    String pipelineId = split[3];
                    String pipelJobTraceId = kafkaReceive.pipelJobTraceId;
                    if (!Objects.equals(Integer.parseInt(split[4]), OlapTableEnum.CUSTOMIZESCRIPT.getValue())) {
                        //没有表id就把任务id扔进去
                        tableId = split[6];
                    }
                    NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(pipelineId, Integer.parseInt(split[4]), null, Integer.parseInt(StringUtils.isEmpty(tableId) ? "0" : tableId));
                    if (Objects.equals(Integer.parseInt(split[4]), OlapTableEnum.CUSTOMIZESCRIPT.getValue())) {
                        //没有表id就把任务id扔进去
                        nifiGetPortHierarchy.nifiCustomWorkflowDetailId = Long.valueOf(split[6]);
                    }
                    TaskHierarchyDTO nifiPortHierarchy = iPipelineTaskPublishCenter.getNifiPortHierarchy(nifiGetPortHierarchy, kafkaReceive.pipelTraceId);
                    List<NifiPortsHierarchyNextDTO> nextList = nifiPortHierarchy.nextList;
                    NifiCustomWorkflowDetailDTO itselfPort = nifiPortHierarchy.itselfPort;
                    DispatchJobHierarchyDTO dispatchJobHierarchy = iPipelineTaskPublishCenter.getDispatchJobHierarchyByTaskId(pipelTraceId, String.valueOf(itselfPort.id));
                    Map<Integer, Object> taskMap = new HashMap<>();
                    String format = simpleDateFormat.format(new Date());
                    TaskHierarchyDTO taskHierarchyDto = iPipelineTaskPublishCenter.getTaskHierarchy(pipelTraceId, String.valueOf(nifiPortHierarchy.id));
                    log.info("任务结束中心本节点状态:{},{}", taskHierarchyDto.id, taskHierarchyDto.taskStatus);
                    if (Objects.equals(taskHierarchyDto.taskStatus, DispatchLogEnum.taskfailure)) {

                        taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + format);

                    } else if (Objects.equals(taskHierarchyDto.taskStatus, DispatchLogEnum.taskpass)) {

                        //taskMap.put(DispatchLogEnum.taskstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + format);
                        taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.PASS.getName() + " - " + format);

                    } else if (!Objects.equals(taskHierarchyDto.taskStatus, DispatchLogEnum.taskpass) && !Objects.equals(taskHierarchyDto.taskStatus, DispatchLogEnum.taskfailure)) {
                        Map<Object, Object> pipelTask = redisUtil.getAndDel(RedisKeyEnum.PIPEL_TASK.getName() + ":" + kafkaReceive.pipelTaskTraceId);
                        log.info(itselfPort.id + "拿打印条数89" + JSON.toJSONString(pipelTask));
                        Object endTime = pipelTask.get(DispatchLogEnum.taskend.getName());
                        Object count = pipelTask.get(DispatchLogEnum.taskcount.getName());
                        taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + (endTime != null ? endTime.toString() : simpleDateFormat.format(new Date())) + " - 同步条数 : " + (Objects.isNull(count) ? 0 : count));
                    }
                    iPipelTaskLog.savePipelTaskLog(pipelTraceId, pipelJobTraceId, nifiPortHierarchy.taskTraceId, taskMap, String.valueOf(nifiPortHierarchy.id), itselfPort.tableId, Integer.parseInt(split[4]));
                    // 先检查本级状态,判断是否应该记本级所在job的结束,或者管道结束
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
                            } else {
                                jobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + simpleDateFormat.format(new Date()));
                            }
                            iPipelJobLog.savePipelJobLog(pipelTraceId, jobMap, pipelineId, dispatchJobHierarchy.jobTraceId, String.valueOf(dispatchJobHierarchy.id));
                        }
                        // 第三步 发送消息给任务发布中心  topic是 : task.build.task.publish
                        log.info("任务结束中心发送给任务发布中心的参数:{}", JSON.toJSONString(kafkaReceive));
                        kafkaTemplateHelper.sendMessageAsync("task.build.task.publish", JSON.toJSONString(kafkaReceive));
                    } else {
                        //记录本节点的job的结束
                        Map<Integer, Object> jobMap = new HashMap<>();
                        if (Objects.equals(dispatchJobHierarchy.jobStatus, NifiStageTypeEnum.RUN_FAILED)) {
                            jobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + simpleDateFormat.format(new Date()));
                        } else if (Objects.equals(dispatchJobHierarchy.jobStatus, NifiStageTypeEnum.PASS)) {
                            jobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.PASS.getName() + " - " + simpleDateFormat.format(new Date()));
                        } else {
                            jobMap.put(DispatchLogEnum.jobend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + simpleDateFormat.format(new Date()));
                        }
                        iPipelJobLog.savePipelJobLog(pipelTraceId, jobMap, pipelineId, dispatchJobHierarchy.jobTraceId, String.valueOf(dispatchJobHierarchy.id));
                        //记录管道结束
                        log.info("尝试记录管道结束");
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
                            if (!jobHierarchy.jobProcessed) {
                                return;
                            }
                        }
                        log.info("开始记录管道结束");
                        if (success) {
                            pipelMap.put(DispatchLogEnum.pipelend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + simpleDateFormat.format(new Date()));
                        } else {
                            pipelMap.put(DispatchLogEnum.pipelend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + simpleDateFormat.format(new Date()));
                        }
                        iPipelLog.savePipelLog(pipelTraceId, pipelMap, pipelineId);
                    }

                } else if (split.length == 6) {
                    if (Objects.equals(kafkaReceive.topicType, TopicTypeEnum.DAILY_NIFI_FLOW.getValue())) {
                        Map<Integer, Object> taskMap = new HashMap<>();
                        String format = simpleDateFormat.format(new Date());
                        taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + format + " - 同步条数 : " + (Objects.isNull(kafkaReceive.numbers) ? 0 : kafkaReceive.numbers));
                        iPipelTaskLog.savePipelTaskLog(null, null, kafkaReceive.pipelTaskTraceId, taskMap, null, split[5], Integer.parseInt(split[3]));
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


}
