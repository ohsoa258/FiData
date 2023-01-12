package com.fisk.task.listener.pipeline.impl;

import com.alibaba.fastjson.JSON;
import com.davis.client.ApiException;
import com.davis.client.model.ProcessGroupEntity;
import com.davis.client.model.ProcessGroupStatusDTO;
import com.fisk.chartvisual.enums.SsasChartFilterTypeEnum;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.mdc.MDCHelper;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.api.ApiImportDataDTO;
import com.fisk.datafactory.dto.customworkflowdetail.DispatchJobHierarchyDTO;
import com.fisk.datafactory.dto.customworkflowdetail.QueryJobHierarchyDTO;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.task.ExecScriptDTO;
import com.fisk.dataaccess.dto.api.PipelApiDispatchDTO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.TaskHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyNextDTO;
import com.fisk.datafactory.dto.tasknifi.PipeDagDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.TableTopicDTO;
import com.fisk.task.entity.OlapPO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.pipeline.IPipelineTaskPublishCenter;
import com.fisk.task.mapper.NifiStageMapper;
import com.fisk.task.mapper.PipelineTableLogMapper;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelLog;
import com.fisk.task.service.dispatchLog.IPipelStageLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.service.nifi.IOlap;
import com.fisk.task.service.nifi.ITableNifiSettingService;
import com.fisk.task.service.pipeline.ITableTopicService;
import com.fisk.task.utils.DelayedTask;
import com.fisk.task.utils.KafkaTemplateHelper;
import com.fisk.task.utils.NifiHelper;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.fisk.common.framework.redis.RedisKeyEnum.DELAYED_TASK;

/**
 * @author: cfk
 * CreateTime: 2022/04/21 15:05
 * Description:
 */
@Slf4j
@Component
public class PipelineTaskPublishCenter implements IPipelineTaskPublishCenter {
    @Resource
    IOlap iOlap;
    @Value("${nifi.pipeline.waitTime}")
    private String waitTime;
    @Resource
    RedisUtil redisUtil;
    @Resource
    ITableTopicService iTableTopicService;
    @Resource
    private DataFactoryClient dataFactoryClient;
    @Resource
    NifiStageMapper nifiStageMapper;
    @Resource
    PipelineTableLogMapper pipelineTableLogMapper;
    @Resource
    KafkaTemplateHelper kafkaTemplateHelper;
    @Resource
    IPipelJobLog iPipelJobLog;
    @Resource
    IPipelLog iPipelLog;
    @Resource
    IPipelTaskLog iPipelTaskLog;
    @Resource
    IPipelStageLog iPipelStageLog;
    @Resource
    ITableNifiSettingService iTableNifiSettingService;
    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    IPipelineTaskPublishCenter iPipelineTaskPublishCenter;
    @Resource
    PublishTaskController publishTaskController;
    @Value("${nifi.pipeline.maxTime}")
    public String maxTime;


    @Override
    public void msg(String mapString, Acknowledgment acke) {
        log.info("消费消息:start");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        KafkaReceiveDTO kafkaReceiveDTO = KafkaReceiveDTO.builder().build();
        //流程所在组id,只限有nifi的流程
        String groupId = "";
        Timer timer = new Timer();
        String pipelName = "";
        String jobName = "";
        //每次进来存进redis里面,key-value,都是topic-name,过期时间为5分钟
        try {
            mapString = "[" + mapString + "]";
            log.info("mapString信息:" + mapString);
            List<KafkaReceiveDTO> kafkaReceives = JSON.parseArray(mapString, KafkaReceiveDTO.class);
            for (KafkaReceiveDTO kafkaReceive : kafkaReceives) {
                kafkaReceiveDTO = kafkaReceive;
                //管道总的pipelTraceId
                if (StringUtils.isEmpty(kafkaReceiveDTO.pipelTraceId)) {
                    kafkaReceiveDTO.pipelTraceId = UUID.randomUUID().toString();
                    MDCHelper.setPipelTraceId(kafkaReceiveDTO.pipelTraceId);
                }
                if (!StringUtils.isEmpty(kafkaReceiveDTO.topic)) {
                    String topicName = kafkaReceiveDTO.topic;
                    String[] split1 = topicName.split("\\.");
                    String pipelineId = split1[3];
                    if (Objects.equals(kafkaReceiveDTO.topicType, TopicTypeEnum.DAILY_NIFI_FLOW.getValue())) {
                        //卡夫卡的内容在发布时就定义好了
                        String msg = JSON.toJSONString(kafkaReceiveDTO);
                        log.info("打印topic内容:" + msg);
                        if (kafkaReceiveDTO.ifTaskStart) {
                            HashMap<Integer, Object> taskMap = new HashMap<>();
                            taskMap.put(DispatchLogEnum.taskstart.getValue(), NifiStageTypeEnum.START_RUN + " - " + simpleDateFormat.format(new Date()));
                            log.info("第二处调用保存task日志");
                            iPipelTaskLog.savePipelTaskLog(null, null, kafkaReceiveDTO.pipelTaskTraceId, taskMap, null, split1[5], Integer.parseInt(split1[3]));
                            //任务中心发布任务,通知任务开始执行
                            kafkaTemplateHelper.sendMessageAsync(topicName, msg);
                        } else {
                            //这里可以不做处理,因为这里不是管道的结束
                            redisUtil.heartbeatDetection("nowExec" + kafkaReceiveDTO.pipelTaskTraceId + "," + topicName, topicName, Long.parseLong(waitTime));
                            //存一个真正的过期时间,不断刷新 这里key是pipelTaskTraceId.因为手动调度没有jobId和taskId;
                            Map<String, Object> map = new HashMap<>();
                            map.put(DispatchLogEnum.taskend.getName(), simpleDateFormat.format(new Date()));
                            map.put(DispatchLogEnum.taskcount.getName(), kafkaReceiveDTO.numbers + "");
                            log.info("打印条数151" + JSON.toJSONString(map));
                            redisUtil.hmset(RedisKeyEnum.PIPEL_TASK.getName() + ":" + kafkaReceiveDTO.pipelTaskTraceId, map, 3600);

                        }
                    }
                    //调度管道中与调度job相连接的job(可能是多个job与开始调度job连接)中首个task任务
                    else if (Objects.equals(kafkaReceiveDTO.topicType, TopicTypeEnum.PIPELINE_NIFI_FLOW.getValue())) {
                        //  这个时候可能是api的topic,可能是管道直接调度的topic,保存管道开始,job开始 定义管道traceid  定义job的traceid
                        //流程开始时间
                        kafkaReceiveDTO.start_time = simpleDateFormat.format(new Date());
                        kafkaReceiveDTO.pipelStageTraceId = UUID.randomUUID().toString();
                        //nifi流程要的批次号
                        kafkaReceiveDTO.fidata_batch_code = kafkaReceiveDTO.pipelTraceId;

                        log.info("打印topic内容:" + JSON.toJSONString(kafkaReceiveDTO));
                        String pipelTraceId = kafkaReceiveDTO.pipelTraceId;
                        Map<Integer, Object> PipelMap = new HashMap<>();
                        String pipelstart = simpleDateFormat.format(new Date());
                        //创建redis里本次调度版本
                        NifiGetPortHierarchyDTO Hierarchy = new NifiGetPortHierarchyDTO();
                        Hierarchy.workflowId = pipelineId;
                        iPipelineTaskPublishCenter.getPipeDagDto(Hierarchy, pipelTraceId);

                        //管道开始,job开始,task开始
                        List<TableTopicDTO> topicNames = iTableTopicService.getByTopicName(topicName);
                        for (TableTopicDTO topic : topicNames) {
                            String[] split = topic.topicName.split("\\.");
                            NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(pipelineId, Integer.parseInt(split[4]), null, Integer.valueOf(split[6]));
                            TaskHierarchyDTO nifiPortHierarchy = this.getNifiPortHierarchy(nifiGetPortHierarchy, kafkaReceiveDTO.pipelTraceId);
                            //job批次号
                            kafkaReceiveDTO.pipelJobTraceId = iPipelineTaskPublishCenter.getDispatchJobHierarchyByTaskId(kafkaReceiveDTO.pipelTraceId, String.valueOf(topic.componentId)).jobTraceId;
                            //task批次号
                            kafkaReceiveDTO.pipelTaskTraceId = UUID.randomUUID().toString();
                            kafkaReceiveDTO.topic = topic.topicName;
                            kafkaReceiveDTO.topicType = TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue();

                            log.info("发送的topic2:{},内容:{}", topic.topicName, JSON.toJSONString(kafkaReceiveDTO));
                            kafkaTemplateHelper.sendMessageAsync(topic.topicName, JSON.toJSONString(kafkaReceiveDTO));
                            //-----------------------------------------------------
                            //job开始日志
                            Map<Integer, Object> jobMap = new HashMap<>();

                            pipelName = nifiPortHierarchy.itselfPort.workflowName;
                            jobName = nifiPortHierarchy.itselfPort.componentsName;
                            //任务依赖的组件
                            jobMap.put(DispatchLogEnum.jobstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                            //jobMap.put(DispatchLogEnum.jobstate.getValue(), jobName + " " + NifiStageTypeEnum.RUNNING.getName());
                            iPipelJobLog.savePipelJobLog(kafkaReceiveDTO.pipelTraceId, jobMap, split1[3], kafkaReceiveDTO.pipelJobTraceId, String.valueOf(nifiPortHierarchy.itselfPort.pid));
                            //task日志
                            HashMap<Integer, Object> taskMap = new HashMap<>();
                            taskMap.put(DispatchLogEnum.taskstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                            //taskMap.put(DispatchLogEnum.taskstate.getValue(), jobName + "-" + nifiPortHierarchy.itselfPort.tableOrder + " " + NifiStageTypeEnum.RUNNING.getName());
                            log.info("第三处调用保存task日志");
                            iPipelTaskLog.savePipelTaskLog(kafkaReceiveDTO.pipelTraceId, kafkaReceiveDTO.pipelJobTraceId, kafkaReceiveDTO.pipelTaskTraceId, taskMap, String.valueOf(nifiPortHierarchy.itselfPort.id), null, 0);
                        }
                        //调度脚本任务
                        if (!StringUtils.isEmpty(kafkaReceiveDTO.scriptTaskIds)) {
                            ExecScriptDTO execScript = new ExecScriptDTO();
                            String[] scriptTaskId = kafkaReceiveDTO.scriptTaskIds.split(",");
                            execScript.pipelTraceId = kafkaReceiveDTO.pipelTraceId;
                            for (String taskId : scriptTaskId) {
                                execScript.pipelJobTraceId = iPipelineTaskPublishCenter.getDispatchJobHierarchyByTaskId(kafkaReceiveDTO.pipelTraceId, String.valueOf(taskId)).jobTraceId;
                                execScript.pipelTaskTraceId = UUID.randomUUID().toString();
                                execScript.taskId = taskId;
                                log.info("发送的执行脚本topic:{},内容:{}", MqConstants.QueueConstants.BUILD_EXEC_SCRIPT_FLOW, JSON.toJSONString(execScript));
                                kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_EXEC_SCRIPT_FLOW, JSON.toJSONString(execScript));
                                //job开始日志
                                Map<Integer, Object> jobMap = new HashMap<>();
                                NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(pipelineId, OlapTableEnum.CUSTOMIZESCRIPT.getValue(), null, 0);
                                if (Objects.equals(Integer.parseInt(split1[4]), OlapTableEnum.CUSTOMIZESCRIPT.getValue())) {
                                    //没有表id就把任务id扔进去
                                    nifiGetPortHierarchy.nifiCustomWorkflowDetailId = Long.valueOf(taskId);
                                }

                                TaskHierarchyDTO nifiPortHierarchy = this.getNifiPortHierarchy(nifiGetPortHierarchy, kafkaReceiveDTO.pipelTraceId);
                                pipelName = nifiPortHierarchy.itselfPort.workflowName;
                                jobName = nifiPortHierarchy.itselfPort.componentsName;
                                //任务依赖的组件
                                jobMap.put(DispatchLogEnum.jobstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                                //jobMap.put(DispatchLogEnum.jobstate.getValue(), jobName + " " + NifiStageTypeEnum.RUNNING.getName());
                                iPipelJobLog.savePipelJobLog(kafkaReceiveDTO.pipelTraceId, jobMap, pipelineId, execScript.pipelJobTraceId, String.valueOf(nifiPortHierarchy.itselfPort.pid));
                                //task日志
                                HashMap<Integer, Object> taskMap = new HashMap<>();
                                taskMap.put(DispatchLogEnum.taskstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                                //taskMap.put(DispatchLogEnum.taskstate.getValue(), jobName + "-" + nifiPortHierarchy.itselfPort.tableOrder + " " + NifiStageTypeEnum.RUNNING.getName());
                                log.info("第四处调用保存task日志");
                                iPipelTaskLog.savePipelTaskLog(kafkaReceiveDTO.pipelTraceId, execScript.pipelJobTraceId, kafkaReceiveDTO.pipelTaskTraceId, taskMap, String.valueOf(nifiPortHierarchy.itselfPort.id), null, OlapTableEnum.CUSTOMIZESCRIPT.getValue());

                            }

                        }
                        //如果有非实时api,单独发消息
                        if (!StringUtils.isEmpty(kafkaReceiveDTO.pipelApiDispatch)) {
                            ApiImportDataDTO apiImportData = new ApiImportDataDTO();
                            //apiImportData.pipelApiDispatch = kafkaReceiveDTO.pipelApiDispatch;
                            apiImportData.pipelTraceId = kafkaReceiveDTO.pipelTraceId;
                            List<PipelApiDispatchDTO> pipelApiDispatchs = JSON.parseArray(kafkaReceiveDTO.pipelApiDispatch, PipelApiDispatchDTO.class);
                            for (PipelApiDispatchDTO pipelApiDispatch : pipelApiDispatchs) {
                                apiImportData.pipelApiDispatch = JSON.toJSONString(pipelApiDispatch);
                                apiImportData.pipelJobTraceId = iPipelineTaskPublishCenter.getDispatchJobHierarchyByTaskId(kafkaReceiveDTO.pipelTraceId, String.valueOf(pipelApiDispatch.workflowId)).jobTraceId;
                                kafkaReceiveDTO.pipelJobTraceId = apiImportData.pipelJobTraceId;
                                apiImportData.pipelTaskTraceId = UUID.randomUUID().toString();
                                kafkaReceiveDTO.pipelTaskTraceId = apiImportData.pipelTaskTraceId;
                                apiImportData.pipelStageTraceId = UUID.randomUUID().toString();
                                pipelineId = String.valueOf(pipelApiDispatch.pipelineId);
                                log.info("发送的topic3:{},内容:{}", MqConstants.QueueConstants.BUILD_ACCESS_API_FLOW, JSON.toJSONString(apiImportData));
                                kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_ACCESS_API_FLOW, JSON.toJSONString(apiImportData));
                                //job开始日志
                                Map<Integer, Object> jobMap = new HashMap<>();
                                NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(pipelineId, OlapTableEnum.PHYSICS_API.getValue(), null, Math.toIntExact(pipelApiDispatch.apiId));
                                TaskHierarchyDTO nifiPortHierarchy = this.getNifiPortHierarchy(nifiGetPortHierarchy, kafkaReceiveDTO.pipelTraceId);
                                pipelName = nifiPortHierarchy.itselfPort.workflowName;
                                jobName = nifiPortHierarchy.itselfPort.componentsName;
                                //任务依赖的组件
                                jobMap.put(DispatchLogEnum.jobstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                                //jobMap.put(DispatchLogEnum.jobstate.getValue(), jobName + " " + NifiStageTypeEnum.RUNNING.getName());
                                iPipelJobLog.savePipelJobLog(kafkaReceiveDTO.pipelTraceId, jobMap, pipelineId, kafkaReceiveDTO.pipelJobTraceId, String.valueOf(nifiPortHierarchy.itselfPort.pid));
                                //task日志
                                HashMap<Integer, Object> taskMap = new HashMap<>();
                                taskMap.put(DispatchLogEnum.taskstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + simpleDateFormat.format(new Date()));
                                //taskMap.put(DispatchLogEnum.taskstate.getValue(), jobName + "-" + nifiPortHierarchy.itselfPort.tableOrder + " " + NifiStageTypeEnum.RUNNING.getName());
                                log.info("第四处调用保存task日志");
                                iPipelTaskLog.savePipelTaskLog(kafkaReceiveDTO.pipelTraceId, kafkaReceiveDTO.pipelJobTraceId, kafkaReceiveDTO.pipelTaskTraceId, taskMap, String.valueOf(nifiPortHierarchy.itselfPort.id), null, 0);

                            }

                        }
                        //管道开始日志
                        PipelMap.put(DispatchLogEnum.pipelstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + pipelstart);
                        //PipelMap.put(DispatchLogEnum.pipelstate.getValue(), pipelName + " " + NifiStageTypeEnum.RUNNING.getName());
                        log.info("第一处调用保存job日志");
                        iPipelJobLog.savePipelLog(pipelTraceId, PipelMap, pipelineId);
                        iPipelLog.savePipelLog(pipelTraceId, PipelMap, pipelineId);
                    } else if (Objects.equals(kafkaReceiveDTO.topicType, TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue())) {

                        //请求接口得到对象,条件--管道名称,表名称,表类别,表id,topic_name(加表名table_name)
                        NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(pipelineId, kafkaReceiveDTO.tableType, null, kafkaReceiveDTO.tableId);
                        if (Objects.equals(Integer.parseInt(split1[4]), OlapTableEnum.CUSTOMIZESCRIPT.getValue())) {
                            //没有表id就把任务id扔进去
                            nifiGetPortHierarchy.nifiCustomWorkflowDetailId = Long.valueOf(split1[6]);
                        }
                        if (kafkaReceiveDTO.tableId != null && kafkaReceiveDTO.tableId != 0) {
                            List<TableNifiSettingPO> list = iTableNifiSettingService.query()
                                    .eq("type", kafkaReceiveDTO.tableType).eq("table_access_id", kafkaReceiveDTO.tableId).eq("del_flag", 1).list();
                            if (!CollectionUtils.isEmpty(list)) {
                                groupId = list.get(list.size() - 1).tableComponentId;
                            }
                        }

                        TaskHierarchyDTO data = this.getNifiPortHierarchy(nifiGetPortHierarchy, kafkaReceiveDTO.pipelTraceId);
                        //本节点
                        NifiCustomWorkflowDetailDTO itselfPort = data.itselfPort;
                        String id = String.valueOf(itselfPort.id);
                        jobName = itselfPort.componentsName;
                        TableTopicDTO topicSelf = iTableTopicService.getTableTopicDTOByComponentId(Math.toIntExact(itselfPort.id),
                                nifiGetPortHierarchy.tableId, kafkaReceiveDTO.tableType);

                        //本节点topic
                        String topicName1 = topicSelf.topicName;
                        //下一级
                        List<NifiPortsHierarchyNextDTO> nextList = data.nextList;
                        //记录节点真正的失效时间,从来都是本节点的,最多包含本节点所在job的过期时间,
                        //------------------------------------------------------------------------------------------------
                        if (nextList == null) {
                            //完成时间要去rediskey失效那里做,需要有一个标识告诉我这次失效代表什么 fiskgd--fisk管道
                            String hmgetKey = "fiskgd:" + kafkaReceiveDTO.pipelTraceId;
                            Boolean ifexist = true;
                            boolean ifend = true;
                            //通过这个管道key查是否所有支线末端都走完了,如果没有不记录结束时间
                            Map<Object, Object> hmget = redisUtil.hmget(hmgetKey);
                            //便利已存在的末端
                            if (!CollectionUtils.isEmpty(hmget)) {
                                Map<Object, Object> mapObj = redisUtil.hmget(RedisKeyEnum.PIPEL_JOB_TRACE_ID.getName() + ":" + kafkaReceiveDTO.pipelTraceId);
                                Iterator<Map.Entry<Object, Object>> nodeMap = mapObj.entrySet().iterator();
                                while (nodeMap.hasNext()) {
                                    Map.Entry<Object, Object> next = nodeMap.next();
                                    DispatchJobHierarchyDTO dto = JSON.parseObject(next.getValue().toString(), DispatchJobHierarchyDTO.class);
                                    if (dto.last && dto.jobProcessed) {
                                        ifexist = false;
                                    }
                                    if (dto.last && !dto.jobProcessed) {
                                        ifend = false;
                                    }
                                }
                            } else {
                                hmget = new HashMap<>();
                            }
                            if (ifexist) {
                                //如果map里面不存在,装进去
                                hmget.put(topicName1, topicName1);
                                if (ifend) {
                                    //如果结束支点就它一个,装进去等30秒
                                    redisUtil.hmsset(hmgetKey, hmget, Long.parseLong(waitTime));
                                    delayedTask(id, kafkaReceiveDTO.message, timer, kafkaReceiveDTO.pipelTraceId, data.pipeEndDto, hmgetKey, groupId);
                                    // 存一个真正的过期时间(最后一个task,job)
                                    //如果有刷新,没有就新建  taskid  结束时间  时间值
                                    Map<String, Object> map = new HashMap<>();
                                    map.put(DispatchLogEnum.taskend.getName(), simpleDateFormat.format(new Date()));
                                    map.put(DispatchLogEnum.taskcount.getName(), kafkaReceiveDTO.numbers + "");
                                    log.info(itselfPort.id + "打印条数344" + JSON.toJSONString(map));
                                    redisUtil.hmset(RedisKeyEnum.PIPEL_TASK.getName() + ":" + itselfPort.id, map, Long.parseLong(maxTime));
                                    //redisUtil.hset(RedisKeyEnum.PIPEL_JOB.getName()+itselfPort.pid, DispatchLogEnum.jobend.getName(), simpleDateFormat.format(new Date()), 3000);
                                } else {
                                    //如果结束支点不止它一个,不仅要装进去,还要等其他支点
                                    Map<String, Object> map = new HashMap<>();
                                    map.put(DispatchLogEnum.taskend.getName(), simpleDateFormat.format(new Date()));
                                    map.put(DispatchLogEnum.taskcount.getName(), kafkaReceiveDTO.numbers + "");
                                    log.info(itselfPort.id + "打印条数352" + JSON.toJSONString(map));
                                    redisUtil.hmset(RedisKeyEnum.PIPEL_TASK.getName() + ":" + itselfPort.id, map, Long.parseLong(maxTime));
                                    redisUtil.hmsset(hmgetKey, hmget, Long.parseLong(maxTime));
                                }
                            } else {
                                //如果map里面存在,判断map的记录个数,如果不是所有支点结束,刷新过期时间3000
                                if (ifend) {
                                    //如果满足有所有支点的条件了,就刷新过期时间30秒
                                    redisUtil.hmsset(hmgetKey, hmget, Long.parseLong(waitTime));
                                    delayedTask(id, kafkaReceiveDTO.message, timer, kafkaReceiveDTO.pipelTraceId, data.pipeEndDto, hmgetKey, groupId);
                                    // 刷新task和job的过期时间
                                    Map<String, Object> map = new HashMap<>();
                                    map.put(DispatchLogEnum.taskend.getName(), simpleDateFormat.format(new Date()));
                                    map.put(DispatchLogEnum.taskcount.getName(), kafkaReceiveDTO.numbers + "");
                                    log.info(itselfPort.id + "打印条数366" + JSON.toJSONString(map));
                                    redisUtil.hmset(RedisKeyEnum.PIPEL_TASK.getName() + ":" + itselfPort.id, map, Long.parseLong(maxTime));
                                    //redisUtil.hset(RedisKeyEnum.PIPEL_JOB.getName()+itselfPort.pid, DispatchLogEnum.jobend.getName(), simpleDateFormat.format(new Date()), 3000);
                                } else {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put(DispatchLogEnum.taskend.getName(), simpleDateFormat.format(new Date()));
                                    map.put(DispatchLogEnum.taskcount.getName(), kafkaReceiveDTO.numbers + "");
                                    log.info(itselfPort.id + "打印条数373" + JSON.toJSONString(map));
                                    redisUtil.hmset(RedisKeyEnum.PIPEL_TASK.getName() + ":" + itselfPort.id, map, Long.parseLong(maxTime));
                                    redisUtil.hmsset(hmgetKey, hmget, Long.parseLong(maxTime));
                                }
                            }
                        }
                        //------------------------------------------------------------------------------------------------
                        else {
                            for (NifiPortsHierarchyNextDTO nifiPortsHierarchyNextDTO : nextList) {
                                //下一级本身
                                //NifiCustomWorkflowDetailDTO itselfPort1 = nifiPortsHierarchyNextDTO.itselfPort;
                                NifiGetPortHierarchyDTO nextHierarchy = new NifiGetPortHierarchyDTO();
                                nextHierarchy.nifiCustomWorkflowDetailId = nifiPortsHierarchyNextDTO.itselfPort;
                                nextHierarchy.workflowId = pipelineId;
                                TaskHierarchyDTO taskNext = this.getNifiPortHierarchy(nextHierarchy, kafkaReceiveDTO.pipelTraceId);
                                NifiCustomWorkflowDetailDTO itselfPort1 = taskNext.itselfPort;
                                Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + kafkaReceiveDTO.pipelTraceId);
                                TaskHierarchyDTO taskHierarchy = JSON.parseObject(hmget.get(String.valueOf(nextHierarchy.nifiCustomWorkflowDetailId)).toString(), TaskHierarchyDTO.class);
                                ChannelDataEnum channel = ChannelDataEnum.getValue(itselfPort1.componentType);
                                OlapTableEnum olapTableEnum = ChannelDataEnum.getOlapTableEnum(channel.getValue());
                                log.info("表类别:{}", olapTableEnum);
                                //下一级所有的上一级
                                List<Long> upPortList = nifiPortsHierarchyNextDTO.upPortList;
                                //判断redis里面有没有这个key    itselfPort1(key,很关键,tnnd)
                                TableTopicDTO topicDTO = iTableTopicService.getTableTopicDTOByComponentId(Math.toIntExact(itselfPort1.id),
                                        itselfPort1.tableId, olapTableEnum.getValue());
                                String topicContent = "";
                                //topic需要加上一个批次号   管道的  不然redis失效那里不好判断这个任务属于哪个批次 具体命名规范为  原本topic+管道批次
                                String topic = topicDTO.topicName + "," + kafkaReceiveDTO.pipelTraceId;
                                Object key = redisUtil.get(topic);
                                //--------------------------------------------------------
                                //upPortList  
                                boolean hasKey = redisUtil.hasKey(topic);
                                if (Objects.equals(taskHierarchy.taskStatus, DispatchLogEnum.taskpass)) {
                                    if (hasKey) {
                                        redisUtil.heartbeatDetection(topic, topicSelf.topicName, Long.parseLong(waitTime));
                                        delayedTask(id, kafkaReceiveDTO.message, timer, kafkaReceiveDTO.pipelTraceId, nifiPortsHierarchyNextDTO.upPortList, topic, groupId);
                                    }
                                }
                                boolean goNext2 = true;
                                if (!CollectionUtils.isEmpty(upPortList)) {
                                    boolean goNext = true;

                                    for (Long upId : upPortList) {
                                        TaskHierarchyDTO taskHierarchy1 = iPipelineTaskPublishCenter.getTaskHierarchy(kafkaReceiveDTO.pipelTraceId, String.valueOf(upId));
                                        if (!taskHierarchy1.taskProcessed) {
                                            goNext2 = false;
                                        }
                                        if (Objects.equals(upId, itselfPort.id)) {
                                            //true代表正常,false代表不正常
                                            boolean passage = getGroupId(upId, kafkaReceiveDTO.pipelTraceId);
                                            //判断流文件数量
                                            if (!passage) {
                                                //先判断key是否存在,因为一开始这个key不存在
                                                goNext = false;
                                            }
                                        }
                                    }
                                    if (hasKey && !goNext) {
                                        redisUtil.expire(topic, Long.parseLong(maxTime));
                                        continue;
                                    }
                                }
                                //--------------------------------------------------------
                                // 存入(此task)真正的过期时间,不断刷新
                                Map<String, Object> map = new HashMap<>();
                                map.put(DispatchLogEnum.taskend.getName(), simpleDateFormat.format(new Date()));
                                map.put(DispatchLogEnum.taskcount.getName(), kafkaReceiveDTO.numbers + "");
                                log.info(itselfPort.id + "打印条数435" + JSON.toJSONString(map));
                                redisUtil.hmset(RedisKeyEnum.PIPEL_TASK.getName() + ":" + itselfPort.id, map, 3600);
                                if (key == null) {
                                    if (upPortList.size() == 1) {
                                        log.info("存入redis即将调用的节点1:" + topic);
                                        redisUtil.heartbeatDetection(topic, topicSelf.topicName, Long.parseLong(waitTime));
                                        delayedTask(id, kafkaReceiveDTO.message, timer, kafkaReceiveDTO.pipelTraceId, nifiPortsHierarchyNextDTO.upPortList, topic, groupId);

                                    } else {
                                        redisUtil.heartbeatDetection(topic, topicSelf.topicName, Long.parseLong(maxTime));
                                    }
                                } else {
                                    topicContent = key.toString();
                                    String[] split = topicContent.split(",");
                                    //实例化一个set集合
                                    HashSet<String> set = new HashSet<>();
                                    //遍历数组并存入集合,如果元素已存在则不会重复存入
                                    for (int i = 0; i < split.length; i++) {
                                        set.add(split[i]);
                                    }
                                    //返回Set集合的数组形式
                                    split = (String[])(set.toArray(new String[ set.size()]));
                                    //意思是没全了,所有上游没有调完
                                    if (split.length < upPortList.size()) {
                                        log.info("比较的两个值{},{},{}", JSON.toJSONString(upPortList), split, goNext2);
                                        if (upPortList.size() - split.length <= 1) {
                                            if (goNext2) {
                                                if (topicContent.contains(topicSelf.topicName)) {
                                                    log.info("存入redis即将调用的节点2:" + topic);
                                                    redisUtil.expire(topic, Long.parseLong(waitTime));
                                                    delayedTask(id, kafkaReceiveDTO.message, timer, kafkaReceiveDTO.pipelTraceId, nifiPortsHierarchyNextDTO.upPortList, topic, groupId);
                                                } else {
                                                    log.info("存入redis即将调用的节点3:" + topic);
                                                    redisUtil.heartbeatDetection(topic, topicContent + "," + topicSelf.topicName, Long.parseLong(waitTime));
                                                    delayedTask(id, kafkaReceiveDTO.message, timer, kafkaReceiveDTO.pipelTraceId, nifiPortsHierarchyNextDTO.upPortList, topic, groupId);
                                                }
                                            } else {
                                                if (topicContent.contains(topicSelf.topicName)) {
                                                    redisUtil.expire(topic, Long.parseLong(maxTime));
                                                } else {
                                                    redisUtil.heartbeatDetection(topic, topicContent + "," + topicSelf.topicName, Long.parseLong(maxTime));
                                                }
                                            }

                                        } else {

                                            if (topicContent.contains(topicSelf.topicName)) {
                                                redisUtil.expire(topic, Long.parseLong(maxTime));
                                            } else {
                                                redisUtil.heartbeatDetection(topic, topicContent + "," + topicSelf.topicName, Long.parseLong(maxTime));
                                            }
                                        }
                                    } else {
                                        log.info("存入redis即将调用的节点4:" + topic);
                                        redisUtil.expire(topic, Long.parseLong(waitTime));
                                        delayedTask(id, kafkaReceiveDTO.message, timer, kafkaReceiveDTO.pipelTraceId, nifiPortsHierarchyNextDTO.upPortList, topic, groupId);
                                    }
                                }
                            }
                        }
                    }
                }

                log.info("消费消息:end");
            }
        } catch (Exception e) {
            DispatchExceptionHandlingDTO dispatchExceptionHandling = getDispatchExceptionHandling(kafkaReceiveDTO, pipelName, jobName);
            log.error("管道调度报错" + StackTraceHelper.getStackTraceInfo(e));
            iPipelJobLog.exceptionHandlingLog(dispatchExceptionHandling);

        } finally {
            if (acke != null) {
                acke.acknowledge();
            }
        }
    }

    public static DispatchExceptionHandlingDTO getDispatchExceptionHandling(KafkaReceiveDTO kafkaReceive,String pipelName,String jobName){
        return DispatchExceptionHandlingDTO.builder()
                .comment("发布中心报错")
                .jobName(jobName)
                .pipleName(pipelName)
                .pipelTraceId(kafkaReceive.pipelTraceId)
                .pipelJobTraceId(kafkaReceive.pipelJobTraceId)
                .pipelStageTraceId(kafkaReceive.pipelStageTraceId)
                .pipelTaskTraceId(kafkaReceive.pipelTaskTraceId)
                .build();
    }

    @Override
    public TaskHierarchyDTO getNifiPortHierarchy(NifiGetPortHierarchyDTO nifiGetPortHierarchy, String pipelTraceId) {
        log.info("查询部分dag图参数:{},pipelTraceId:{}", JSON.toJSONString(nifiGetPortHierarchy), pipelTraceId);
        TaskHierarchyDTO nifiPortsHierarchy = new TaskHierarchyDTO();
        PipeDagDTO data = this.getPipeDagDto(nifiGetPortHierarchy, pipelTraceId);
        if (data != null && data.taskHierarchyDtos != null) {
            List<TaskHierarchyDTO> nifiPortsHierarchyDtos = data.taskHierarchyDtos;
            List<TaskHierarchyDTO> collect = new ArrayList<>();
            if (!StringUtils.isEmpty(nifiGetPortHierarchy.tableId)) {
                collect = nifiPortsHierarchyDtos.stream().filter(Objects::nonNull)
                        .filter(e -> Objects.equals(nifiGetPortHierarchy.tableId, e.itselfPort.tableId) && e.itselfPort.componentType.equals(nifiGetPortHierarchy.channelDataEnum.getName())
                        ).collect(Collectors.toList());
            } else {

                collect = nifiPortsHierarchyDtos.stream().filter(Objects::nonNull)
                        .filter(e -> Objects.equals(e.itselfPort.id, nifiGetPortHierarchy.nifiCustomWorkflowDetailId)
                        ).collect(Collectors.toList());
            }

            if (!CollectionUtils.isEmpty(collect)) {
                nifiPortsHierarchy = collect.get(0);
            }
        } else {
            log.error("调度模块无此调度的dag图");
        }
        if (Objects.isNull(nifiPortsHierarchy) || Objects.isNull(nifiPortsHierarchy.itselfPort)) {
            ResultEntity<TaskHierarchyDTO> nifiPortHierarchy = dataFactoryClient.getNifiPortHierarchy(nifiGetPortHierarchy);
            if (Objects.equals(nifiPortHierarchy.code, ResultEnum.SUCCESS.getCode())) {
                nifiPortsHierarchy = nifiPortHierarchy.data;
            }
        }
        //去除ftp分类影响
        String dagPart = JSON.toJSONString(nifiPortsHierarchy);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(dagPart)) {
            dagPart = dagPart.replaceAll(ChannelDataEnum.DATALAKE_FTP_TASK.getName(), ChannelDataEnum.DATALAKE_TASK.getName());
            nifiPortsHierarchy = JSON.parseObject(dagPart, TaskHierarchyDTO.class);
        }
        return nifiPortsHierarchy;
    }


    @Override
    public PipeDagDTO getPipeDagDto(NifiGetPortHierarchyDTO nifiGetPortHierarchy, String pipelTraceId) {
        log.info("查询dag图参数:{},pipelTraceId:{}", JSON.toJSONString(nifiGetPortHierarchy), pipelTraceId);
        PipeDagDTO data = new PipeDagDTO();
        //先查redis,如果没有查一次调度模块
        boolean flag = redisUtil.hasKey(RedisKeyEnum.PIPEL_TRACE_ID.getName() + ":" + pipelTraceId);
        if (!flag) {
            ResultEntity<PipeDagDTO> taskLinkedList = dataFactoryClient.getTaskLinkedList(Long.valueOf(nifiGetPortHierarchy.workflowId));
            QueryJobHierarchyDTO queryJobHierarchy = new QueryJobHierarchyDTO();
            queryJobHierarchy.nifiCustomWorkflowId = Long.valueOf(nifiGetPortHierarchy.workflowId);
            queryJobHierarchy.pipelTraceId = pipelTraceId;
            ResultEntity<List<DispatchJobHierarchyDTO>> jobList = dataFactoryClient.getJobList(queryJobHierarchy);
            if (Objects.equals(jobList.code, ResultEnum.SUCCESS.getCode())) {
                Map<Object, Object> jobMap = new HashMap<>();
                List<DispatchJobHierarchyDTO> dtos = jobList.data;
                //PIPEL_JOB_TRACE_ID
                for (DispatchJobHierarchyDTO jobHierarchy : dtos) {
                    jobHierarchy.jobTraceId = UUID.randomUUID().toString();
                    jobMap.put(String.valueOf(jobHierarchy.id), JSON.toJSONString(jobHierarchy));
                }
                //把job的运行情况加上
                redisUtil.hmsset(RedisKeyEnum.PIPEL_JOB_TRACE_ID.getName() + ":" + pipelTraceId, jobMap, Long.parseLong(maxTime));
            }
            if (Objects.equals(ResultEnum.SUCCESS.getCode(), taskLinkedList.code)) {
                data = taskLinkedList.data;
                List<TaskHierarchyDTO> taskHierarchyDtos = data.taskHierarchyDtos;
                taskHierarchyDtos.forEach(e -> {
                    //初始状态为未运行
                    e.taskStatus = DispatchLogEnum.tasknorun;
                });
                Map<Object, Object> tasks = new HashMap<>();
                for (TaskHierarchyDTO dto : taskHierarchyDtos) {
                    dto.taskTraceId = UUID.randomUUID().toString();
                    tasks.put(String.valueOf(dto.id), JSON.toJSONString(dto));
                }
                //只是列表
                redisUtil.hmsset(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + pipelTraceId, tasks, Long.parseLong(maxTime));
                //原始数据
                redisUtil.set(RedisKeyEnum.PIPEL_TRACE_ID.getName() + ":" + pipelTraceId, JSON.toJSONString(data), Long.parseLong(maxTime));

            } else {
                log.error("调度模块无此调度的dag图:" + taskLinkedList.msg);
            }
        } else {
            //如果有,从redis里面拿
            String taskJson = redisUtil.get(RedisKeyEnum.PIPEL_TRACE_ID.getName() + ":" + pipelTraceId).toString();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(taskJson)) {
                data = JSON.parseObject(taskJson, PipeDagDTO.class);
            }
        }
        data.pipelTraceId = pipelTraceId;
        //去除ftp分类影响
        String dag = JSON.toJSONString(data);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(dag)) {
            dag = dag.replaceAll(ChannelDataEnum.DATALAKE_FTP_TASK.getName(), ChannelDataEnum.DATALAKE_TASK.getName());
            data = JSON.parseObject(dag, PipeDagDTO.class);
        }
        //log.info("该管道dag图:{}", JSON.toJSONString(data));
        return data;
    }

    @Override
    public DispatchJobHierarchyDTO getDispatchJobHierarchy(String pipelTraceId, String jobId) {
        log.info("获取DispatchJobHierarchy参数:{},{}", pipelTraceId, jobId);
        Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_JOB_TRACE_ID.getName() + ":" + pipelTraceId);
        return JSON.parseObject(hmget.get(jobId).toString(), DispatchJobHierarchyDTO.class);
    }

    @Override
    public TaskHierarchyDTO getTaskHierarchy(String pipelTraceId, String taskId) {
        log.info("获取TaskHierarchy参数:{},{}", pipelTraceId, taskId);
        Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + pipelTraceId);
        return JSON.parseObject(hmget.get(taskId).toString(), TaskHierarchyDTO.class);
    }

    @Override
    public DispatchJobHierarchyDTO getDispatchJobHierarchyByTaskId(String pipelTraceId, String taskId) {
        TaskHierarchyDTO taskHierarchy = getTaskHierarchy(pipelTraceId, taskId);
        Long pid = taskHierarchy.itselfPort.pid;
        return getDispatchJobHierarchy(pipelTraceId, String.valueOf(pid));
    }

    /**
     * 创建延时队列
     *
     * @param pipelTraceId
     * @param taskId       本身任务id
     * @param thisTopic    下一级本身topic
     * @param groupId      本级本身nifi所在组的id
     */
    private void delayedTask(String id, String message, Timer timer, String pipelTraceId, List<Long> taskId, String thisTopic, String groupId) {
        //真正调用之前要看本节点是否被调用过,解决提前调用
        log.info("delayedTask方法参数:{},{},{},{},{}", message, pipelTraceId, JSON.toJSONString(taskId), thisTopic, groupId);
        Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + pipelTraceId);
        boolean success = true;
/*        for (Long id : taskId) {
            Object thisTask = hmget.get(String.valueOf(id));
            if (Objects.nonNull(thisTask)) {
                TaskHierarchyDTO dto = JSON.parseObject(thisTask.toString(), TaskHierarchyDTO.class);
                log.info("本节点详情" + JSON.toJSONString(dto));
                if (Objects.nonNull(dto) && Objects.equals(dto.taskStatus, DispatchLogEnum.tasknorun)) {
                    success = false;
                }
            } else {
                success = false;
                log.info("redis中未找到上游");
            }

        }*/

        if (success) {
            String value = UUID.randomUUID().toString();
            //刷新时间和创建key或者修改value,会产生延时任务
            redisUtil.set(RedisKeyEnum.DELAYED_TASK.getName() + ":" + thisTopic, value, Long.parseLong(maxTime));
            DelayedTask delayedTask = new DelayedTask(id, message, value, groupId, thisTopic, kafkaTemplateHelper, dataFactoryClient, iOlap, iPipelJobLog, iPipelLog, iPipelTaskLog, redisUtil, iTableNifiSettingService, dataAccessClient, iPipelineTaskPublishCenter, publishTaskController);
            timer.schedule(delayedTask, Long.parseLong(waitTime) * 1000);
        } else {
            log.info("上游任务未调用完成");
        }
    }

    public boolean getGroupId(long taskId, String pipelTraceId) {
        Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + pipelTraceId);
        TaskHierarchyDTO taskHierarchy = JSON.parseObject(hmget.get(String.valueOf(taskId)).toString(), TaskHierarchyDTO.class);
        NifiCustomWorkflowDetailDTO itselfPort = taskHierarchy.itselfPort;
        ChannelDataEnum channel = ChannelDataEnum.getValue(itselfPort.componentType);
        OlapTableEnum olapTableEnum = ChannelDataEnum.getOlapTableEnum(channel.getValue());
        String groupId = "";
        List<TableNifiSettingPO> list = iTableNifiSettingService.query()
                .eq("type", olapTableEnum.getValue()).eq("table_access_id", itselfPort.tableId).eq("del_flag", 1).list();
        if (!CollectionUtils.isEmpty(list)) {
            groupId = list.get(list.size() - 1).tableComponentId;
        }
        try {
            //只有是nifi处理的任务才有这个groupId
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(groupId)) {
                ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(groupId);
                ProcessGroupStatusDTO status = processGroup.getStatus();
                //flowFilesQueued 组内流文件数量,如果为0代表组内所有流文件执行完,没有正在执行的组件
                Integer flowFilesQueued = status.getAggregateSnapshot().getFlowFilesQueued();
                if (!Objects.equals(flowFilesQueued, 0)) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("查看组状态报错");
        }
        return true;
    }


}
