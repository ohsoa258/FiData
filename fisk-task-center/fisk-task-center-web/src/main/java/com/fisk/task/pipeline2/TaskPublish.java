package com.fisk.task.pipeline2;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.framework.mdc.MDCHelper;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.api.ApiImportDataDTO;
import com.fisk.dataaccess.dto.api.PipelApiDispatchDTO;
import com.fisk.datafactory.dto.customworkflowdetail.DispatchJobHierarchyDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyNextDTO;
import com.fisk.datafactory.dto.tasknifi.TaskHierarchyDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.ExecScriptDTO;
import com.fisk.task.dto.task.PowerBiDataSetRefreshDTO;
import com.fisk.task.dto.task.SftpCopyDTO;
import com.fisk.task.dto.task.TableTopicDTO;
import com.fisk.task.entity.PipelTaskLogPO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.pipeline.IPipelineTaskPublishCenter;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.service.nifi.IOlap;
import com.fisk.task.service.nifi.ITableNifiSettingService;
import com.fisk.task.service.pipeline.ITableTopicService;
import com.fisk.task.utils.KafkaTemplateHelper;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 任务发布中心,管道调用的是这里 task.build.task.publish
 *
 * @author cfk
 */
@Slf4j
@Component
public class TaskPublish {

    @Resource
    IPipelTaskLog iPipelTaskLog;
    @Resource
    KafkaTemplateHelper kafkaTemplateHelper;
    @Resource
    RedisUtil redisUtil;
    @Resource
    IPipelineTaskPublishCenter iPipelineTaskPublishCenter;
    @Resource
    ITableTopicService iTableTopicService;
    @Resource
    IPipelJobLog iPipelJobLog;
    @Resource
    IOlap iOlap;
    @Value("${nifi.pipeline.waitTime}")
    private String waitTime;
    @Resource
    IPipelLog iPipelLog;
    @Resource
    ITableNifiSettingService iTableNifiSettingService;
    @Resource
    DataAccessClient dataAccessClient;


    /**
     * 接收到的本节点,需要找所有下游,根据下游状态调用
     */
    public void taskPublish(String message, Acknowledgment acke) {
////        //李世纪 2023-05-10测试修改
//        acke.acknowledge();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String pipelTraceId = "";
        try {
            log.info("taskPublish参数:{}", message);
            message = "[" + message + "]";
            List<String> msg = JSON.parseArray(message, String.class);
            for (String node : msg) {
                KafkaReceiveDTO kafkaReceiveDTO = JSON.parseObject(node, KafkaReceiveDTO.class);
                //流程所在组id,只限有nifi的流程
                String groupId = "";
                //管道总的pipelTraceId
                if (StringUtils.isEmpty(kafkaReceiveDTO.pipelTraceId)) {
                    kafkaReceiveDTO.pipelTraceId = UUID.randomUUID().toString();
                    MDCHelper.setPipelTraceId(kafkaReceiveDTO.pipelTraceId);
                }
                pipelTraceId = kafkaReceiveDTO.pipelTraceId;
                if (!StringUtils.isEmpty(kafkaReceiveDTO.topic)) {
                    String topicName = kafkaReceiveDTO.topic;
                    String[] split1 = topicName.split("\\.");
                    String pipelineId = split1[3];
                    //调度管道中与调度job相连接的job(可能是多个job与开始调度job连接)中首个task任务
                    if (Objects.equals(kafkaReceiveDTO.topicType, TopicTypeEnum.PIPELINE_NIFI_FLOW.getValue())) {
                        //  这个时候可能是api的topic,可能是管道直接调度的topic,保存管道开始,job开始 定义管道traceid  定义job的traceid
                        //流程开始时间
                        kafkaReceiveDTO.start_time = simpleDateFormat.format(new Date());
                        kafkaReceiveDTO.pipelStageTraceId = UUID.randomUUID().toString();
                        //nifi流程要的批次号
                        kafkaReceiveDTO.fidata_batch_code = kafkaReceiveDTO.pipelTraceId;

                        log.info("打印topic内容:" + JSON.toJSONString(kafkaReceiveDTO));
                        Map<Integer, Object> pipelMap = new HashMap<>();
                        String pipelstart = simpleDateFormat.format(new Date());
                        //创建redis里本次调度版本
                        NifiGetPortHierarchyDTO hierarchy = new NifiGetPortHierarchyDTO();
                        hierarchy.workflowId = pipelineId;
                        iPipelineTaskPublishCenter.getPipeDagDto(hierarchy, pipelTraceId);

                        //管道开始,job开始,task开始
                        List<TableTopicDTO> topicNames = iTableTopicService.getByTopicName(topicName);
                        for (TableTopicDTO topic : topicNames) {
                            String[] split = topic.topicName.split("\\.");
                            NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(pipelineId, Integer.parseInt(split[4]), null, Integer.valueOf(split[6]));
                            if (Objects.equals(Integer.parseInt(split[4]), OlapTableEnum.CUSTOMIZESCRIPT.getValue()) ||
                                    Objects.equals(Integer.parseInt(split[4]), OlapTableEnum.SFTPFILECOPYTASK.getValue()) ||
                                    Objects.equals(Integer.parseInt(split[4]), OlapTableEnum.POWERBIDATASETREFRESHTASK.getValue())) {
                                //没有表id就把任务id扔进去
                                String ids = kafkaReceiveDTO.sftpFileCopyTaskIds + "," + kafkaReceiveDTO.scriptTaskIds + "," + kafkaReceiveDTO.powerBiDataSetRefreshTaskIds;
                                String[] id = ids.split(",");
                                boolean next = false;
                                for (String taskId : id) {
                                    if (Objects.equals(split[6], taskId)) {
                                        next = true;
                                    }
                                }
                                if (!next) {
                                    continue;
                                }
                                nifiGetPortHierarchy.nifiCustomWorkflowDetailId = Long.valueOf(split[6]);
                            }
                            TaskHierarchyDTO nifiPortHierarchy = iPipelineTaskPublishCenter.getNifiPortHierarchy(nifiGetPortHierarchy, kafkaReceiveDTO.pipelTraceId);
                            //job批次号
                            kafkaReceiveDTO.pipelJobTraceId = iPipelineTaskPublishCenter.getDispatchJobHierarchyByTaskId(kafkaReceiveDTO.pipelTraceId, String.valueOf(topic.componentId)).jobTraceId;
                            //task批次号
                            kafkaReceiveDTO.pipelTaskTraceId = iPipelineTaskPublishCenter.getTaskHierarchy(pipelTraceId, String.valueOf(topic.componentId)).taskTraceId;
                            kafkaReceiveDTO.topic = topic.topicName;
                            kafkaReceiveDTO.topicType = TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue();


                            if (Objects.equals(Integer.parseInt(split[4]), OlapTableEnum.CUSTOMIZESCRIPT.getValue())) {
                                //调度脚本任务
                                sendScriptTask(kafkaReceiveDTO, pipelineId, split[4], split[6]);
                            } else if (Objects.equals(Integer.parseInt(split[4]), OlapTableEnum.SFTPFILECOPYTASK.getValue())) {
                                //sftp复制任务
                                sendSftpFileCopyTask(kafkaReceiveDTO, pipelineId, split[4], split[6]);
                            } else if (Objects.equals(Integer.parseInt(split[4]), OlapTableEnum.POWERBIDATASETREFRESHTASK.getValue())) {
                                // power任务
                                sendPowerBiDataSetRefreshTask(kafkaReceiveDTO, pipelineId, split[4], split[6]);
                            } else {
                                log.info("发送的topic2:{},内容:{}", topic.topicName, JSON.toJSONString(kafkaReceiveDTO));
                                kafkaTemplateHelper.sendMessageAsync(topic.topicName, JSON.toJSONString(kafkaReceiveDTO));
                            }
                            //-----------------------------------------------------
                            //job开始日志
                            Map<Integer, Object> jobMap = new HashMap<>();
                            //任务依赖的组件
                            jobMap.put(DispatchLogEnum.jobstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                            iPipelJobLog.savePipelJobLog(kafkaReceiveDTO.pipelTraceId, jobMap, split1[3], kafkaReceiveDTO.pipelJobTraceId, String.valueOf(nifiPortHierarchy.itselfPort.pid));
                            //task日志
                            HashMap<Integer, Object> taskMap = new HashMap<>();
                            taskMap.put(DispatchLogEnum.taskstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                            log.info("第三处调用保存task日志");
                            iPipelTaskLog.savePipelTaskLog(kafkaReceiveDTO.pipelTraceId, kafkaReceiveDTO.pipelJobTraceId, kafkaReceiveDTO.pipelTaskTraceId, taskMap, String.valueOf(nifiPortHierarchy.itselfPort.id), null, 0);


                        }

                        //如果有非实时api,单独发消息
                        if (!StringUtils.isEmpty(kafkaReceiveDTO.pipelApiDispatch)) {
                            ApiImportDataDTO apiImportData = new ApiImportDataDTO();
                            apiImportData.pipelTraceId = kafkaReceiveDTO.pipelTraceId;
                            List<PipelApiDispatchDTO> pipelApiDispatchs = JSON.parseArray(kafkaReceiveDTO.pipelApiDispatch, PipelApiDispatchDTO.class);
                            for (PipelApiDispatchDTO pipelApiDispatch : pipelApiDispatchs) {
                                apiImportData.pipelApiDispatch = JSON.toJSONString(pipelApiDispatch);
                                apiImportData.pipelJobTraceId = iPipelineTaskPublishCenter.getDispatchJobHierarchyByTaskId(kafkaReceiveDTO.pipelTraceId, String.valueOf(pipelApiDispatch.workflowId)).jobTraceId;
                                kafkaReceiveDTO.pipelJobTraceId = apiImportData.pipelJobTraceId;
                                apiImportData.pipelTaskTraceId = iPipelineTaskPublishCenter.getTaskHierarchy(pipelTraceId, String.valueOf(pipelApiDispatch.workflowId)).taskTraceId;
                                kafkaReceiveDTO.pipelTaskTraceId = apiImportData.pipelTaskTraceId;
                                apiImportData.pipelStageTraceId = UUID.randomUUID().toString();
                                pipelineId = String.valueOf(pipelApiDispatch.pipelineId);
                                log.info("发送的topic3:{},内容:{}", MqConstants.QueueConstants.BUILD_ACCESS_API_FLOW, JSON.toJSONString(apiImportData));
                                kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_ACCESS_API_FLOW, JSON.toJSONString(apiImportData));
                                //job开始日志
                                Map<Integer, Object> jobMap = new HashMap<>();
                                NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(pipelineId, OlapTableEnum.PHYSICS_API.getValue(), null, Math.toIntExact(pipelApiDispatch.apiId));
                                TaskHierarchyDTO nifiPortHierarchy = iPipelineTaskPublishCenter.getNifiPortHierarchy(nifiGetPortHierarchy, kafkaReceiveDTO.pipelTraceId);
                                //任务依赖的组件
                                jobMap.put(DispatchLogEnum.jobstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                                iPipelJobLog.savePipelJobLog(kafkaReceiveDTO.pipelTraceId, jobMap, pipelineId, kafkaReceiveDTO.pipelJobTraceId, String.valueOf(nifiPortHierarchy.itselfPort.pid));
                                //task日志
                                HashMap<Integer, Object> taskMap = new HashMap<>();
                                taskMap.put(DispatchLogEnum.taskstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                                log.info("第四处调用保存task日志");
                                iPipelTaskLog.savePipelTaskLog(kafkaReceiveDTO.pipelTraceId, kafkaReceiveDTO.pipelJobTraceId, kafkaReceiveDTO.pipelTaskTraceId, taskMap, String.valueOf(nifiPortHierarchy.itselfPort.id), String.valueOf(pipelApiDispatch.apiId), OlapTableEnum.PHYSICS_API.getValue());

                            }

                        }
                        //管道开始日志
                        pipelMap.put(DispatchLogEnum.pipelstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + pipelstart);
                        log.info("第一处调用保存job日志");
                        iPipelJobLog.savePipelLog(pipelTraceId, pipelMap, pipelineId);
                        iPipelLog.savePipelLog(pipelTraceId, pipelMap, pipelineId);
                    } else if (Objects.equals(kafkaReceiveDTO.topicType, TopicTypeEnum.DAILY_NIFI_FLOW.getValue()) || Objects.equals(kafkaReceiveDTO.topicType, TopicTypeEnum.MDM_NIFI_FLOW.getValue())) {
                        //卡夫卡的内容在发布时就定义好了
                        String dailyNifiMsg = JSON.toJSONString(kafkaReceiveDTO);
                        log.info("打印topic内容:" + dailyNifiMsg);
                        HashMap<Integer, Object> taskMap = new HashMap<>();
                        taskMap.put(DispatchLogEnum.taskstart.getValue(), NifiStageTypeEnum.START_RUN + " - " + simpleDateFormat.format(new Date()));
                        log.info("第二处调用保存task日志");
                        iPipelTaskLog.savePipelTaskLog(null, null, kafkaReceiveDTO.pipelTaskTraceId, taskMap, null, split1[5], Integer.parseInt(split1[3]));
                        //任务中心发布任务,通知任务开始执行
                        kafkaTemplateHelper.sendMessageAsync(topicName, dailyNifiMsg);
                    } else if (Objects.equals(kafkaReceiveDTO.topicType, TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue())) {
                        String tableId = "";
                        if (!Objects.equals(Integer.parseInt(split1[4]), OlapTableEnum.CUSTOMIZESCRIPT.getValue()) &&
                                !Objects.equals(Integer.parseInt(split1[4]), OlapTableEnum.SFTPFILECOPYTASK.getValue()) &&
                                !Objects.equals(Integer.parseInt(split1[4]), OlapTableEnum.POWERBIDATASETREFRESHTASK.getValue())) {
                            //没有表id就把任务id扔进去
                            tableId = split1[6];
                        }
                        //请求接口得到对象,条件--管道名称,表名称,表类别,表id,topic_name(加表名table_name)
                        NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(pipelineId, Integer.parseInt(split1[4]), null, Integer.parseInt(StringUtils.isEmpty(tableId) ? "0" : tableId));
                        if (Objects.equals(Integer.parseInt(split1[4]), OlapTableEnum.CUSTOMIZESCRIPT.getValue()) ||
                                Objects.equals(Integer.parseInt(split1[4]), OlapTableEnum.SFTPFILECOPYTASK.getValue()) ||
                                Objects.equals(Integer.parseInt(split1[4]), OlapTableEnum.POWERBIDATASETREFRESHTASK.getValue())) {
                            //没有表id就把任务id扔进去
                            nifiGetPortHierarchy.nifiCustomWorkflowDetailId = Long.valueOf(split1[6]);
                        }
                        TaskHierarchyDTO data = iPipelineTaskPublishCenter.getNifiPortHierarchy(nifiGetPortHierarchy, kafkaReceiveDTO.pipelTraceId);
                        //本节点
                        NifiCustomWorkflowDetailDTO itselfPort = data.itselfPort;
                        String id = String.valueOf(itselfPort.id);
                        TableTopicDTO topicSelf = iTableTopicService.getTableTopicDTOByComponentId(Math.toIntExact(itselfPort.id),
                                nifiGetPortHierarchy.tableId, kafkaReceiveDTO.tableType);

                        //下一级
                        List<NifiPortsHierarchyNextDTO> nextList = data.nextList;
                        if (!CollectionUtils.isEmpty(nextList)) {
                            for (NifiPortsHierarchyNextDTO nifiPortsHierarchyNext : nextList) {
                                //下一级本身
                                NifiGetPortHierarchyDTO nextHierarchy = new NifiGetPortHierarchyDTO();
                                nextHierarchy.nifiCustomWorkflowDetailId = nifiPortsHierarchyNext.itselfPort;
                                nextHierarchy.workflowId = pipelineId;
                                TaskHierarchyDTO taskNext = iPipelineTaskPublishCenter.getNifiPortHierarchy(nextHierarchy, kafkaReceiveDTO.pipelTraceId);
                                NifiCustomWorkflowDetailDTO itselfPort1 = taskNext.itselfPort;
                                DispatchJobHierarchyDTO nextDispatchJobHierarchy = iPipelineTaskPublishCenter.getDispatchJobHierarchyByTaskId(pipelTraceId, String.valueOf(itselfPort1.id));
                                String nextJobTraceId = nextDispatchJobHierarchy.jobTraceId;
                                log.info("下一级jobtraceid:{},{}", itselfPort1.id, nextJobTraceId);
                                Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + kafkaReceiveDTO.pipelTraceId);
                                TaskHierarchyDTO taskHierarchy = JSON.parseObject(hmget.get(String.valueOf(nextHierarchy.nifiCustomWorkflowDetailId)).toString(), TaskHierarchyDTO.class);
                                ChannelDataEnum channel = ChannelDataEnum.getValue(itselfPort1.componentType);
                                OlapTableEnum olapTableEnum = ChannelDataEnum.getOlapTableEnum(channel.getValue());
                                log.info("表类别:{}", olapTableEnum);
                                //下一级所有的上一级
                                List<Long> upPortList = nifiPortsHierarchyNext.upPortList;
                                //判断redis里面有没有这个key    itselfPort1(key,很关键,tnnd)
                                TableTopicDTO topicDTO = iTableTopicService.getTableTopicDTOByComponentId(Math.toIntExact(itselfPort1.id),
                                        itselfPort1.tableId, olapTableEnum.getValue());
                                String topic = topicDTO.topicName;
                                String upPid = "";
                                if (!CollectionUtils.isEmpty(upPortList)) {
                                    boolean goNext = true;
                                    for (Long upId : upPortList) {
                                        DispatchJobHierarchyDTO dispatchJobHierarchy = iPipelineTaskPublishCenter.getDispatchJobHierarchyByTaskId(pipelTraceId, String.valueOf(upId));
                                        if (!Objects.equals(dispatchJobHierarchy.id, itselfPort.pid)) {
                                            if (!dispatchJobHierarchy.jobProcessed) {
                                                goNext = false;
                                            }
                                        }
                                        upPid = String.valueOf(dispatchJobHierarchy.id);
                                    }
                                    List<PipelTaskLogPO> list = iPipelTaskLog.query().eq("job_trace_id", nextJobTraceId).eq("task_id", taskNext.itselfPort.id).list();
                                    if (goNext && !taskNext.taskProcessed && CollectionUtils.isEmpty(list)) {
                                        sendNextTask(pipelTraceId, topic, olapTableEnum, taskHierarchy);
                                        if (Objects.equals(String.valueOf(itselfPort1.pid), upPid)) {
                                            log.info("job没有变");
                                        } else {
                                            //job开始日志
                                            Map<Integer, Object> jobMap = new HashMap<>();
                                            //任务依赖的组件
                                            jobMap.put(DispatchLogEnum.jobstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                                            iPipelJobLog.savePipelJobLog(kafkaReceiveDTO.pipelTraceId, jobMap, pipelineId, nextJobTraceId, String.valueOf(itselfPort1.pid));
                                        }
                                        HashMap<Integer, Object> taskMap = new HashMap<>();
                                        taskMap.put(DispatchLogEnum.taskstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                                        log.info("第三处调用保存task日志");
                                        iPipelTaskLog.savePipelTaskLog(kafkaReceiveDTO.pipelTraceId, nextJobTraceId, taskNext.taskTraceId, taskMap, String.valueOf(taskNext.itselfPort.id), taskNext.itselfPort.tableId, olapTableEnum.getValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            log.info("消费消息:end");
        } catch (Exception e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
        } finally {
            log.info("TaskPublish.taskPublish方法结束...");
            if (acke != null) {
                acke.acknowledge();
            }
        }
    }

    private void sendNextTask(String pipelTraceId, String topic, OlapTableEnum type, TaskHierarchyDTO taskHierarchy) {
        String[] split = topic.split("\\.");
        DispatchJobHierarchyDTO dispatchJobHierarchy = iPipelineTaskPublishCenter.getDispatchJobHierarchyByTaskId(pipelTraceId, String.valueOf(taskHierarchy.id));
        String jobTraceId = dispatchJobHierarchy.jobTraceId;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 如果正常,找下游,记下游的开始,并调用真正的下游任务
        if (!Objects.equals(taskHierarchy.taskStatus, DispatchLogEnum.taskfailure) &&
                !Objects.equals(taskHierarchy.taskStatus, DispatchLogEnum.taskpass) &&
                //不禁用
                Objects.nonNull(taskHierarchy.itselfPort) && taskHierarchy.itselfPort.forbidden) {
            //正常发布,调用相应流程
            if (Objects.equals(type, OlapTableEnum.PHYSICS_API)) {
                //非实时api发布
                ApiImportDataDTO apiImportDataDTO = new ApiImportDataDTO();
                apiImportDataDTO.workflowId = split[3];
                apiImportDataDTO.appId = Long.parseLong(split[5]);
                apiImportDataDTO.apiId = Long.parseLong(split[6]);
                apiImportDataDTO.pipelTraceId = pipelTraceId;
                apiImportDataDTO.pipelJobTraceId = jobTraceId;
                apiImportDataDTO.pipelTaskTraceId = taskHierarchy.taskTraceId;
                apiImportDataDTO.pipelStageTraceId = UUID.randomUUID().toString();
                PipelApiDispatchDTO pipelApiDispatchDTO = new PipelApiDispatchDTO();
                pipelApiDispatchDTO.apiId = Long.parseLong(split[6]);
                pipelApiDispatchDTO.appId = Long.parseLong(split[5]);
                pipelApiDispatchDTO.workflowId = String.valueOf(taskHierarchy.id);
                pipelApiDispatchDTO.pipelineId = Long.parseLong(split[3]);
                apiImportDataDTO.pipelApiDispatch = JSON.toJSONString(pipelApiDispatchDTO);
                log.info("api发送卡夫卡请求参数:{}", JSON.toJSONString(apiImportDataDTO));
                dataAccessClient.importData(apiImportDataDTO);
            } else if (Objects.equals(type, OlapTableEnum.CUSTOMIZESCRIPT)) {
                ExecScriptDTO execScript = new ExecScriptDTO();
                execScript.pipelJobTraceId = jobTraceId;
                execScript.pipelTaskTraceId = taskHierarchy.taskTraceId;
                execScript.pipelTraceId = pipelTraceId;
                execScript.taskId = split[6];
                log.info("执行脚本任务发送卡夫卡请求参数:{}", JSON.toJSONString(execScript));
                kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_EXEC_SCRIPT_FLOW, JSON.toJSONString(execScript));
            } else if (Objects.equals(type, OlapTableEnum.SFTPFILECOPYTASK)) {
                SftpCopyDTO sftpCopy = getSftpCopy(pipelTraceId, jobTraceId, taskHierarchy.taskTraceId, split[6], null);
                log.info("执行脚本任务发送卡夫卡请求参数:{}", JSON.toJSONString(sftpCopy));
                kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_SFTP_FILE_COPY_FLOW, JSON.toJSONString(sftpCopy));
            } else if (Objects.equals(type, OlapTableEnum.POWERBIDATASETREFRESHTASK)) {
                // 发送power刷新数据集任务
                PowerBiDataSetRefreshDTO powerBiDataSetRefresh = getPowerBiDataSetRefresh(pipelTraceId, jobTraceId, taskHierarchy.taskTraceId, split[6], null);
                log.info("执行POWERBI数据集刷新任务发送卡夫卡请求参数:{}", JSON.toJSONString(powerBiDataSetRefresh));
                kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_POWERBI_DATA_SET_REFRESH_FLOW, JSON.toJSONString(powerBiDataSetRefresh));

            } else {
                KafkaReceiveDTO kafkaReceive = getKafkaReceive(pipelTraceId, jobTraceId, taskHierarchy.taskTraceId, simpleDateFormat.format(new Date()), TopicTypeEnum.COMPONENT_NIFI_FLOW, topic);
                log.info("发送卡夫卡请求参数:{},内容:{}", topic, JSON.toJSONString(kafkaReceive));
                kafkaTemplateHelper.sendMessageAsync(topic, JSON.toJSONString(kafkaReceive));
            }
        } else {
            //失效调用失效中心
            KafkaReceiveDTO kafkaReceive = getKafkaReceive(pipelTraceId, jobTraceId, taskHierarchy.taskTraceId, simpleDateFormat.format(new Date()), TopicTypeEnum.COMPONENT_NIFI_FLOW, topic);
            kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_TASK_OVER_FLOW, JSON.toJSONString(kafkaReceive));

        }

    }

    /**
     * 发送脚本任务
     *
     * @param kafkaReceiveDTO
     * @param pipelineId
     * @param taskType
     */
    public void sendScriptTask(KafkaReceiveDTO kafkaReceiveDTO, String pipelineId, String taskType, String task) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!StringUtils.isEmpty(kafkaReceiveDTO.scriptTaskIds)) {
            ExecScriptDTO execScript = new ExecScriptDTO();
            String[] scriptTaskId = kafkaReceiveDTO.scriptTaskIds.split(",");
            execScript.pipelTraceId = kafkaReceiveDTO.pipelTraceId;
            for (String taskId : scriptTaskId) {
                if (!Objects.equals(task, taskId)) {
                    continue;
                }
                execScript.pipelJobTraceId = iPipelineTaskPublishCenter.getDispatchJobHierarchyByTaskId(kafkaReceiveDTO.pipelTraceId, String.valueOf(taskId)).jobTraceId;
                execScript.pipelTaskTraceId = iPipelineTaskPublishCenter.getTaskHierarchy(kafkaReceiveDTO.pipelTraceId, String.valueOf(taskId)).taskTraceId;
                execScript.taskId = taskId;
                log.info("发送的执行脚本topic:{},内容:{}", MqConstants.QueueConstants.BUILD_EXEC_SCRIPT_FLOW, JSON.toJSONString(execScript));
                kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_EXEC_SCRIPT_FLOW, JSON.toJSONString(execScript));
                //job开始日志
                Map<Integer, Object> jobMap = new HashMap<>();
                NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(pipelineId, OlapTableEnum.CUSTOMIZESCRIPT.getValue(), null, 0);
                if (Objects.equals(Integer.parseInt(taskType), OlapTableEnum.CUSTOMIZESCRIPT.getValue())) {
                    //没有表id就把任务id扔进去
                    nifiGetPortHierarchy.nifiCustomWorkflowDetailId = Long.valueOf(taskId);
                }

                TaskHierarchyDTO nifiPortHierarchy = iPipelineTaskPublishCenter.getNifiPortHierarchy(nifiGetPortHierarchy, kafkaReceiveDTO.pipelTraceId);
                //任务依赖的组件
                jobMap.put(DispatchLogEnum.jobstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                iPipelJobLog.savePipelJobLog(kafkaReceiveDTO.pipelTraceId, jobMap, pipelineId, execScript.pipelJobTraceId, String.valueOf(nifiPortHierarchy.itselfPort.pid));
                //task日志
                HashMap<Integer, Object> taskMap = new HashMap<>();
                taskMap.put(DispatchLogEnum.taskstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                //taskMap.put(DispatchLogEnum.taskstate.getValue(), jobName + "-" + nifiPortHierarchy.itselfPort.tableOrder + " " + NifiStageTypeEnum.RUNNING.getName());
                log.info("第四处调用保存task日志");
                iPipelTaskLog.savePipelTaskLog(kafkaReceiveDTO.pipelTraceId, execScript.pipelJobTraceId, kafkaReceiveDTO.pipelTaskTraceId, taskMap, String.valueOf(nifiPortHierarchy.itselfPort.id), null, OlapTableEnum.CUSTOMIZESCRIPT.getValue());

            }
        }
    }

    /**
     * 发送脚本任务
     *
     * @param kafkaReceiveDTO
     * @param pipelineId
     * @param taskType
     */
    public void sendSftpFileCopyTask(KafkaReceiveDTO kafkaReceiveDTO, String pipelineId, String taskType, String task) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!StringUtils.isEmpty(kafkaReceiveDTO.sftpFileCopyTaskIds)) {
            SftpCopyDTO execScript = new SftpCopyDTO();
            String[] scriptTaskId = kafkaReceiveDTO.sftpFileCopyTaskIds.split(",");
            //实例化一个set集合
            HashSet<String> set = new HashSet<>();
            //遍历数组并存入集合,如果元素已存在则不会重复存入
            for (int i = 0; i < scriptTaskId.length; i++) {
                set.add(scriptTaskId[i]);
            }
            //返回Set集合的数组形式
            scriptTaskId = (String[]) (set.toArray(new String[set.size()]));
            execScript.pipelTraceId = kafkaReceiveDTO.pipelTraceId;
            for (String taskId : scriptTaskId) {
                if (!Objects.equals(task, taskId)) {
                    continue;
                }
                execScript.pipelJobTraceId = iPipelineTaskPublishCenter.getDispatchJobHierarchyByTaskId(kafkaReceiveDTO.pipelTraceId, String.valueOf(taskId)).jobTraceId;
                execScript.pipelTaskTraceId = iPipelineTaskPublishCenter.getTaskHierarchy(kafkaReceiveDTO.pipelTraceId, String.valueOf(taskId)).taskTraceId;
                execScript.taskId = taskId;
                log.info("发送的执行脚本topic:{},内容:{}", MqConstants.QueueConstants.BUILD_SFTP_FILE_COPY_FLOW, JSON.toJSONString(execScript));
                kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_SFTP_FILE_COPY_FLOW, JSON.toJSONString(execScript));
                //job开始日志
                Map<Integer, Object> jobMap = new HashMap<>();
                NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(pipelineId, OlapTableEnum.SFTPFILECOPYTASK.getValue(), null, 0);
                if (Objects.equals(Integer.parseInt(taskType), OlapTableEnum.SFTPFILECOPYTASK.getValue())) {
                    //没有表id就把任务id扔进去
                    nifiGetPortHierarchy.nifiCustomWorkflowDetailId = Long.valueOf(taskId);
                }

                TaskHierarchyDTO nifiPortHierarchy = iPipelineTaskPublishCenter.getNifiPortHierarchy(nifiGetPortHierarchy, kafkaReceiveDTO.pipelTraceId);
                //任务依赖的组件
                jobMap.put(DispatchLogEnum.jobstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                iPipelJobLog.savePipelJobLog(kafkaReceiveDTO.pipelTraceId, jobMap, pipelineId, execScript.pipelJobTraceId, String.valueOf(nifiPortHierarchy.itselfPort.pid));
                //task日志
                HashMap<Integer, Object> taskMap = new HashMap<>();
                taskMap.put(DispatchLogEnum.taskstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                //taskMap.put(DispatchLogEnum.taskstate.getValue(), jobName + "-" + nifiPortHierarchy.itselfPort.tableOrder + " " + NifiStageTypeEnum.RUNNING.getName());
                log.info("第四处调用保存task日志");
                iPipelTaskLog.savePipelTaskLog(kafkaReceiveDTO.pipelTraceId, execScript.pipelJobTraceId, kafkaReceiveDTO.pipelTaskTraceId, taskMap, String.valueOf(nifiPortHierarchy.itselfPort.id), null, OlapTableEnum.SFTPFILECOPYTASK.getValue());

            }
        }
    }

    /**
     * 发送脚本任务
     *
     * @param kafkaReceiveDTO
     * @param pipelineId
     * @param taskType
     */
    public void sendPowerBiDataSetRefreshTask(KafkaReceiveDTO kafkaReceiveDTO, String pipelineId, String taskType, String task) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!StringUtils.isEmpty(kafkaReceiveDTO.powerBiDataSetRefreshTaskIds)) {
            PowerBiDataSetRefreshDTO execScript = new PowerBiDataSetRefreshDTO();
            String[] scriptTaskId = kafkaReceiveDTO.powerBiDataSetRefreshTaskIds.split(",");
            //实例化一个set集合
            HashSet<String> set = new HashSet<>();
            //遍历数组并存入集合,如果元素已存在则不会重复存入
            for (int i = 0; i < scriptTaskId.length; i++) {
                set.add(scriptTaskId[i]);
            }
            //返回Set集合的数组形式
            scriptTaskId = (String[]) (set.toArray(new String[set.size()]));
            execScript.pipelTraceId = kafkaReceiveDTO.pipelTraceId;
            for (String taskId : scriptTaskId) {
                if (!Objects.equals(task, taskId)) {
                    continue;
                }
                execScript.pipelJobTraceId = iPipelineTaskPublishCenter.getDispatchJobHierarchyByTaskId(kafkaReceiveDTO.pipelTraceId, String.valueOf(taskId)).jobTraceId;
                execScript.pipelTaskTraceId = iPipelineTaskPublishCenter.getTaskHierarchy(kafkaReceiveDTO.pipelTraceId, String.valueOf(taskId)).taskTraceId;
                execScript.taskId = taskId;
                log.info("发送的POWERBI数据集刷新topic:{},内容:{}", MqConstants.QueueConstants.BUILD_POWERBI_DATA_SET_REFRESH_FLOW, JSON.toJSONString(execScript));
                kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_POWERBI_DATA_SET_REFRESH_FLOW, JSON.toJSONString(execScript));
                //job开始日志
                Map<Integer, Object> jobMap = new HashMap<>();
                NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(pipelineId, OlapTableEnum.POWERBIDATASETREFRESHTASK.getValue(), null, 0);
                if (Objects.equals(Integer.parseInt(taskType), OlapTableEnum.POWERBIDATASETREFRESHTASK.getValue())) {
                    //没有表id就把任务id扔进去
                    nifiGetPortHierarchy.nifiCustomWorkflowDetailId = Long.valueOf(taskId);
                }

                TaskHierarchyDTO nifiPortHierarchy = iPipelineTaskPublishCenter.getNifiPortHierarchy(nifiGetPortHierarchy, kafkaReceiveDTO.pipelTraceId);
                //任务依赖的组件
                jobMap.put(DispatchLogEnum.jobstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                iPipelJobLog.savePipelJobLog(kafkaReceiveDTO.pipelTraceId, jobMap, pipelineId, execScript.pipelJobTraceId, String.valueOf(nifiPortHierarchy.itselfPort.pid));
                //task日志
                HashMap<Integer, Object> taskMap = new HashMap<>();
                taskMap.put(DispatchLogEnum.taskstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                //taskMap.put(DispatchLogEnum.taskstate.getValue(), jobName + "-" + nifiPortHierarchy.itselfPort.tableOrder + " " + NifiStageTypeEnum.RUNNING.getName());
                log.info("sendPowerBiDataSetRefreshTask调用保存task日志");
                iPipelTaskLog.savePipelTaskLog(kafkaReceiveDTO.pipelTraceId, execScript.pipelJobTraceId, kafkaReceiveDTO.pipelTaskTraceId, taskMap, String.valueOf(nifiPortHierarchy.itselfPort.id), null, OlapTableEnum.SFTPFILECOPYTASK.getValue());

            }
        }
    }

    /**
     * @param pipelTraceId
     * @param jobTraceId
     * @param pipelTaskTraceId
     * @param startTime
     * @param topicType
     * @param topic
     * @return
     */
    public static KafkaReceiveDTO getKafkaReceive(String pipelTraceId, String jobTraceId, String pipelTaskTraceId, String startTime, TopicTypeEnum topicType, String topic) {
        return KafkaReceiveDTO.builder()
                .pipelTraceId(pipelTraceId)
                .pipelJobTraceId(jobTraceId)
                .pipelTaskTraceId(pipelTaskTraceId)
                .pipelStageTraceId(UUID.randomUUID().toString())
                .fidata_batch_code(pipelTraceId)
                .start_time(startTime)
                .topicType(topicType.getValue())
                .topic(topic)
                .build();
    }

    public static SftpCopyDTO getSftpCopy(String pipelTraceId,
                                          String pipelJobTraceId,
                                          String pipelTaskTraceId,
                                          String taskId,
                                          String pipelStageTraceId) {
        return SftpCopyDTO.builder()
                .pipelJobTraceId(pipelJobTraceId)
                .pipelStageTraceId(pipelStageTraceId)
                .pipelTaskTraceId(pipelTaskTraceId)
                .pipelTraceId(pipelTraceId)
                .taskId(taskId)
                .build();
    }

    public static PowerBiDataSetRefreshDTO getPowerBiDataSetRefresh(String pipelTraceId,
                                                                    String pipelJobTraceId,
                                                                    String pipelTaskTraceId,
                                                                    String taskId,
                                                                    String pipelStageTraceId) {
        return PowerBiDataSetRefreshDTO.builder()
                .pipelJobTraceId(pipelJobTraceId)
                .pipelStageTraceId(pipelStageTraceId)
                .pipelTaskTraceId(pipelTaskTraceId)
                .pipelTraceId(pipelTraceId)
                .taskId(taskId)
                .build();
    }


}


