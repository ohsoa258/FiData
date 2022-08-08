package com.fisk.task.listener.pipeline.impl;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.dto.api.ApiImportDataDTO;
import com.fisk.dataaccess.dto.api.PipelApiDispatchDTO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyNextDTO;
import com.fisk.datafactory.dto.tasknifi.PipeDagDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.TableTopicDTO;
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
import java.util.stream.Collectors;

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


    @Override
    public void msg(String mapString, Acknowledgment acke) {
        log.info("消费消息:start");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        KafkaReceiveDTO kafkaReceiveDTO = new KafkaReceiveDTO();
        String pipelName = "";
        String jobName = "";
        //每次进来存进redis里面,key-value,都是topic-name,过期时间为5分钟
        try {
            log.info("mapString信息:" + mapString);
            kafkaReceiveDTO = JSON.parseObject(mapString, KafkaReceiveDTO.class);
            //管道总的pipelTraceId
            if (StringUtils.isEmpty(kafkaReceiveDTO.pipelTraceId)) {
                kafkaReceiveDTO.pipelTraceId = UUID.randomUUID().toString();
            }
            if (!StringUtils.isEmpty(kafkaReceiveDTO.topic)) {
                String topicName = kafkaReceiveDTO.topic;
                String[] split1 = topicName.split("\\.");
                String pipelineId = split1[3];
                if (Objects.equals(kafkaReceiveDTO.topicType, TopicTypeEnum.DAILY_NIFI_FLOW.getValue())) {
                    //卡夫卡的内容在发布时就定义好了
                    log.info("打印topic内容:" + JSON.toJSONString(kafkaReceiveDTO));
                    if (kafkaReceiveDTO.ifTaskStart) {
                        log.info("发送的topic1:{},内容:{}", topicName, mapString);
                        TableNifiSettingPO tableNifiSetting = iTableNifiSettingService.getByTableId(Long.parseLong(split1[5]), Long.parseLong(split1[3]));
                        HashMap<Integer, Object> taskMap = new HashMap<>();
                        taskMap.put(DispatchLogEnum.taskstart.getValue(), NifiStageTypeEnum.START_RUN + " - " + simpleDateFormat.format(new Date()));
                        iPipelTaskLog.savePipelTaskLog(null, kafkaReceiveDTO.pipelTaskTraceId, taskMap, null, split1[5], Integer.parseInt(split1[3]));
                        //任务中心发布任务,通知任务开始执行
                        kafkaTemplateHelper.sendMessageAsync(topicName, mapString);
                    } else {
                        redisUtil.heartbeatDetection("nowExec" + kafkaReceiveDTO.pipelTaskTraceId + "," + topicName, topicName, Long.parseLong(waitTime));
                        //存一个真正的过期时间,不断刷新 这里key是pipelTaskTraceId.因为手动调度没有jobId和taskId;
                        Map<String, Object> map = new HashMap<>();
                        map.put(DispatchLogEnum.taskend.getName(), simpleDateFormat.format(new Date()));
                        map.put(DispatchLogEnum.taskcount.getName(), kafkaReceiveDTO.numbers);
                        redisUtil.hmset(RedisKeyEnum.PIPEL_TASK.getName() + kafkaReceiveDTO.pipelTaskTraceId, map, 3600);

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

                    if (StringUtils.isEmpty(kafkaReceiveDTO.pipelApiDispatch)) {
                        //管道开始,job开始,task开始
                        List<TableTopicDTO> topicNames = iTableTopicService.getByTopicName(topicName);
                        for (TableTopicDTO topic : topicNames) {
                            //job批次号
                            kafkaReceiveDTO.pipelJobTraceId = UUID.randomUUID().toString();
                            //task批次号
                            kafkaReceiveDTO.pipelTaskTraceId = UUID.randomUUID().toString();
                            kafkaReceiveDTO.topic = topic.topicName;
                            kafkaReceiveDTO.topicType = TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue();
                            String[] split = topic.topicName.split("\\.");
                            log.info("发送的topic2:{},内容:{}", topic.topicName, JSON.toJSONString(kafkaReceiveDTO));
                            kafkaTemplateHelper.sendMessageAsync(topic.topicName, JSON.toJSONString(kafkaReceiveDTO));
                            //-----------------------------------------------------
                            //job开始日志
                            Map<Integer, Object> jobMap = new HashMap<>();
                            NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(pipelineId, Integer.valueOf(split[4]), null, Integer.valueOf(split[6]));
                            NifiPortsHierarchyDTO nifiPortHierarchy = this.getNifiPortHierarchy(nifiGetPortHierarchy, kafkaReceiveDTO.pipelTraceId);
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
                            iPipelTaskLog.savePipelTaskLog(kafkaReceiveDTO.pipelJobTraceId, kafkaReceiveDTO.pipelTaskTraceId, taskMap, String.valueOf(nifiPortHierarchy.itselfPort.id), null, 0);

                        }

                    } else {
                        ApiImportDataDTO apiImportData = new ApiImportDataDTO();
                        //apiImportData.pipelApiDispatch = kafkaReceiveDTO.pipelApiDispatch;
                        apiImportData.pipelTraceId = kafkaReceiveDTO.pipelTraceId;
                        List<PipelApiDispatchDTO> pipelApiDispatchDTOS = JSON.parseArray(kafkaReceiveDTO.pipelApiDispatch, PipelApiDispatchDTO.class);
                        for (PipelApiDispatchDTO pipelApiDispatch : pipelApiDispatchDTOS) {
                            apiImportData.pipelApiDispatch = JSON.toJSONString(pipelApiDispatch);
                            apiImportData.pipelJobTraceId = UUID.randomUUID().toString();
                            apiImportData.pipelTaskTraceId = UUID.randomUUID().toString();
                            apiImportData.pipelStageTraceId = UUID.randomUUID().toString();
                            log.info("发送的topic3:{},内容:{}", topicName, JSON.toJSONString(apiImportData));
                            kafkaTemplateHelper.sendMessageAsync(topicName, JSON.toJSONString(apiImportData));
                            //job开始日志
                            Map<Integer, Object> jobMap = new HashMap<>();
                            NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(pipelineId, OlapTableEnum.PHYSICS_API.getValue(), null, Math.toIntExact(pipelApiDispatch.apiId));
                            NifiPortsHierarchyDTO nifiPortHierarchy = this.getNifiPortHierarchy(nifiGetPortHierarchy, kafkaReceiveDTO.pipelTraceId);
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
                            iPipelTaskLog.savePipelTaskLog(kafkaReceiveDTO.pipelJobTraceId, kafkaReceiveDTO.pipelTaskTraceId, taskMap, String.valueOf(nifiPortHierarchy.itselfPort.id), null, 0);

                        }

                    }
                    //管道开始日志
                    PipelMap.put(DispatchLogEnum.pipelstart.getValue(), NifiStageTypeEnum.START_RUN.getName() + " - " + pipelstart);
                    //PipelMap.put(DispatchLogEnum.pipelstate.getValue(), pipelName + " " + NifiStageTypeEnum.RUNNING.getName());
                    iPipelJobLog.savePipelLog(pipelTraceId, PipelMap, split1[3]);
                    iPipelLog.savePipelLog(pipelTraceId, PipelMap, split1[3]);
                } else if (Objects.equals(kafkaReceiveDTO.topicType, TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue())) {

                    //请求接口得到对象,条件--管道名称,表名称,表类别,表id,topic_name(加表名table_name)
                    NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(pipelineId, kafkaReceiveDTO.tableType, null, kafkaReceiveDTO.tableId);


                    NifiPortsHierarchyDTO data = this.getNifiPortHierarchy(nifiGetPortHierarchy, kafkaReceiveDTO.pipelTraceId);
                    //本节点
                    NifiCustomWorkflowDetailDTO itselfPort = data.itselfPort;
                    jobName = itselfPort.componentsName;
                    TableTopicDTO topicSelf = iTableTopicService.getTableTopicDTOByComponentId(Math.toIntExact(itselfPort.id),
                            Integer.valueOf(nifiGetPortHierarchy.tableId), kafkaReceiveDTO.tableType);

                    //本节点topic
                    String topicName1 = topicSelf.topicName;
                    //下一级
                    List<NifiPortsHierarchyNextDTO> nextList = data.nextList;
                    //记录节点真正的失效时间,从来都是本节点的,最多包含本节点所在job的过期时间,
                    if (nextList == null) {
                        //完成时间要去rediskey失效那里做,需要有一个标识告诉我这次失效代表什么 fiskgd--fisk管道
                        String hmgetKey = "fiskgd:" + kafkaReceiveDTO.pipelTraceId;
                        Boolean ifexist = true;
                        //通过这个管道key查是否所有支线末端都走完了,如果没有不记录结束时间
                        Map<Object, Object> hmget = redisUtil.hmget(hmgetKey);
                        //便利已存在的末端
                        if (!CollectionUtils.isEmpty(hmget)) {
                            Iterator<Map.Entry<Object, Object>> nodeMap = hmget.entrySet().iterator();
                            while (nodeMap.hasNext()) {
                                Map.Entry<Object, Object> next = nodeMap.next();
                                String key = next.getKey().toString();
                                if (Objects.equals(key, topicName1)) {
                                    ifexist = false;
                                }
                            }
                        } else {
                            hmget = new HashMap<>();
                        }
                        if (ifexist) {
                            //如果map里面不存在,装进去
                            hmget.put(topicName1, topicName1);
                            if (hmget.size() == data.pipeEndDto.size()) {
                                //如果结束支点就它一个,装进去等30秒
                                redisUtil.hmsset(hmgetKey, hmget, Long.parseLong(waitTime));
                                // 存一个真正的过期时间(最后一个task,job)
                                //如果有刷新,没有就新建  taskid  结束时间  时间值
                                Map<String, Object> map = new HashMap<>();
                                map.put(DispatchLogEnum.taskend.getName(), simpleDateFormat.format(new Date()));
                                map.put(DispatchLogEnum.taskcount.getName(), kafkaReceiveDTO.numbers);
                                redisUtil.hmset(RedisKeyEnum.PIPEL_TASK.getName() + itselfPort.id, map, 3000);
                                //redisUtil.hset(RedisKeyEnum.PIPEL_JOB.getName()+itselfPort.pid, DispatchLogEnum.jobend.getName(), simpleDateFormat.format(new Date()), 3000);
                            } else {
                                //如果结束支点不止它一个,不仅要装进去,还要等其他支点
                                redisUtil.hmsset(hmgetKey, hmget, 3000);
                            }
                        } else {
                            //如果map里面存在,判断map的记录个数,如果不是所有支点结束,刷新过期时间3000
                            if (hmget.size() == data.pipeEndDto.size()) {
                                //如果满足有所有支点的条件了,就刷新过期时间30秒
                                redisUtil.hmsset(hmgetKey, hmget, Long.parseLong(waitTime));
                                // 刷新task和job的过期时间
                                Map<String, Object> map = new HashMap<>();
                                map.put(DispatchLogEnum.taskend.getName(), simpleDateFormat.format(new Date()));
                                map.put(DispatchLogEnum.taskcount.getName(), kafkaReceiveDTO.numbers);
                                redisUtil.hmset(RedisKeyEnum.PIPEL_TASK.getName() + itselfPort.id, map, 3000);
                                //redisUtil.hset(RedisKeyEnum.PIPEL_JOB.getName()+itselfPort.pid, DispatchLogEnum.jobend.getName(), simpleDateFormat.format(new Date()), 3000);
                            } else {
                                redisUtil.hmsset(hmgetKey, hmget, 3000);
                            }
                        }
                    } else {
                        for (NifiPortsHierarchyNextDTO nifiPortsHierarchyNextDTO : nextList) {
                            //下一级本身
                            NifiCustomWorkflowDetailDTO itselfPort1 = nifiPortsHierarchyNextDTO.itselfPort;
                            ChannelDataEnum channel = ChannelDataEnum.getValue(itselfPort1.componentType);
                            OlapTableEnum olapTableEnum = ChannelDataEnum.getOlapTableEnum(channel.getValue());
                            log.info("表类别:", olapTableEnum);
                            //下一级所有的上一级
                            List<NifiCustomWorkflowDetailDTO> upPortList = nifiPortsHierarchyNextDTO.upPortList;
                            //判断redis里面有没有这个key    itselfPort1(key,很关键,tnnd)
                            TableTopicDTO topicDTO = iTableTopicService.getTableTopicDTOByComponentId(Math.toIntExact(itselfPort1.id),
                                    Integer.valueOf(itselfPort1.tableId), olapTableEnum.getValue());
                            String topicContent = "";
                            //topic需要加上一个批次号   管道的  不然redis失效那里不好判断这个任务属于哪个批次 具体命名规范为  原本topic+管道批次
                            String topic = topicDTO.topicName + "," + kafkaReceiveDTO.pipelTraceId;
                            Object key = redisUtil.get(topic);
                            if (key == null) {
                                if (upPortList.size() == 1) {
                                    log.info("存入redis即将调用的节点1:" + topic);
                                    redisUtil.heartbeatDetection(topic, topicSelf.topicName, Long.parseLong(waitTime));
                                    // 存入(此task)真正的过期时间,不断刷新  itselfPort
                                    Map<String, Object> map = new HashMap<>();
                                    map.put(DispatchLogEnum.taskend.getName(), simpleDateFormat.format(new Date()));
                                    map.put(DispatchLogEnum.taskcount.getName(), kafkaReceiveDTO.numbers);
                                    redisUtil.hmset(RedisKeyEnum.PIPEL_TASK.getName() + itselfPort.id, map, 3000);
                                } else {
                                    redisUtil.heartbeatDetection(topic, topicSelf.topicName, 3000L);
                                }
                            } else {
                                topicContent = key.toString();
                                String[] split = topicContent.split(",");
                                //意思是没全了,所有上游没有调完
                                if (split.length != upPortList.size()) {
                                    if (upPortList.size() - split.length <= 1) {
                                        if (topicContent.contains(topicSelf.topicName)) {
                                            log.info("存入redis即将调用的节点2:" + topic);
                                            redisUtil.expire(topic, Long.parseLong(waitTime));
                                            // 存入(此task)真正的过期时间,不断刷新
                                            Map<String, Object> map = new HashMap<>();
                                            map.put(DispatchLogEnum.taskend.getName(), simpleDateFormat.format(new Date()));
                                            map.put(DispatchLogEnum.taskcount.getName(), kafkaReceiveDTO.numbers);
                                            redisUtil.hmset(RedisKeyEnum.PIPEL_TASK.getName() + itselfPort.id, map, 3000);
                                        } else {
                                            log.info("存入redis即将调用的节点3:" + topic);
                                            redisUtil.heartbeatDetection(topic, topicContent + "," + topicSelf.topicName, Long.parseLong(waitTime));
                                            // 存入(此task)真正的过期时间,不断刷新
                                            Map<String, Object> map = new HashMap<>();
                                            map.put(DispatchLogEnum.taskend.getName(), simpleDateFormat.format(new Date()));
                                            map.put(DispatchLogEnum.taskcount.getName(), kafkaReceiveDTO.numbers);
                                            redisUtil.hmset(RedisKeyEnum.PIPEL_TASK.getName() + itselfPort.id, map, 3000);
                                        }
                                    } else {
                                        if (topicContent.contains(topicSelf.topicName)) {
                                            redisUtil.expire(topic, 3000L);
                                        } else {
                                            redisUtil.heartbeatDetection(topic, topicContent + "," + topicSelf.topicName, 3000L);
                                        }
                                    }
                                } else {
                                    log.info("存入redis即将调用的节点4:" + topic);
                                    redisUtil.expire(topic, Long.parseLong(waitTime));
                                    // 存入(此task)真正的过期时间,不断刷新
                                    Map<String, Object> map = new HashMap<>();
                                    map.put(DispatchLogEnum.taskend.getName(), simpleDateFormat.format(new Date()));
                                    map.put(DispatchLogEnum.taskcount.getName(), kafkaReceiveDTO.numbers);
                                    redisUtil.hmset(RedisKeyEnum.PIPEL_TASK.getName() + itselfPort.id, map, 3600);
                                }
                            }
                        }
                    }
                }
            }

            log.info("消费消息:end");
        } catch (Exception e) {
            DispatchExceptionHandlingDTO dispatchExceptionHandlingDTO = new DispatchExceptionHandlingDTO();
            dispatchExceptionHandlingDTO.comment = "发布中心报错";
            dispatchExceptionHandlingDTO.pipelTraceId = kafkaReceiveDTO.pipelTraceId;
            dispatchExceptionHandlingDTO.pipelJobTraceId = kafkaReceiveDTO.pipelJobTraceId;
            dispatchExceptionHandlingDTO.pipelStageTraceId = kafkaReceiveDTO.pipelStageTraceId;
            dispatchExceptionHandlingDTO.pipelTaskTraceId = kafkaReceiveDTO.pipelTaskTraceId;
            dispatchExceptionHandlingDTO.pipleName = pipelName;
            dispatchExceptionHandlingDTO.JobName = jobName;
            iPipelJobLog.exceptionHandlingLog(dispatchExceptionHandlingDTO);
            log.error("管道调度报错" + StackTraceHelper.getStackTraceInfo(e));
        } finally {
            if (acke != null) {
                acke.acknowledge();
            }
        }
    }

    @Override
    public NifiPortsHierarchyDTO getNifiPortHierarchy(NifiGetPortHierarchyDTO nifiGetPortHierarchy, String pipelTraceId) {
        log.info("查询部分dag图参数:{},pipelTraceId:{}", JSON.toJSONString(nifiGetPortHierarchy), pipelTraceId);
        NifiPortsHierarchyDTO nifiPortsHierarchy = new NifiPortsHierarchyDTO();
        PipeDagDTO data = this.getPipeDagDto(nifiGetPortHierarchy, pipelTraceId);
        if (data != null && data.nifiPortsHierarchyDtos != null) {
            List<NifiPortsHierarchyDTO> nifiPortsHierarchyDtos = data.nifiPortsHierarchyDtos;
            List<NifiPortsHierarchyDTO> collect = nifiPortsHierarchyDtos.stream().filter(Objects::nonNull)
                    .filter(e -> e.itselfPort.tableId.equals(nifiGetPortHierarchy.tableId) && e.itselfPort.componentType.equals(nifiGetPortHierarchy.channelDataEnum.getName())
                    ).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect)) {
                nifiPortsHierarchy = collect.get(0);
            }
        } else {
            log.error("调度模块无此调度的dag图");
        }
        if (Objects.isNull(nifiPortsHierarchy) || Objects.isNull(nifiPortsHierarchy.itselfPort)) {
            ResultEntity<NifiPortsHierarchyDTO> nifiPortHierarchy = dataFactoryClient.getNifiPortHierarchy(nifiGetPortHierarchy);
            if (Objects.equals(nifiPortHierarchy.code, ResultEnum.SUCCESS.getCode())) {
                nifiPortsHierarchy = nifiPortHierarchy.data;
            }
        }
        //去除ftp分类影响
        String dagPart = JSON.toJSONString(nifiPortsHierarchy);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(dagPart)) {
            dagPart = dagPart.replaceAll(ChannelDataEnum.DATALAKE_FTP_TASK.getName(), ChannelDataEnum.DATALAKE_TASK.getName());
            nifiPortsHierarchy = JSON.parseObject(dagPart, NifiPortsHierarchyDTO.class);
        }
        return nifiPortsHierarchy;
    }

    @Override
    public PipeDagDTO getPipeDagDto(NifiGetPortHierarchyDTO nifiGetPortHierarchy, String pipelTraceId) {
        log.info("查询dag图参数:{},pipelTraceId:{}", JSON.toJSONString(nifiGetPortHierarchy), pipelTraceId);
        PipeDagDTO data = new PipeDagDTO();
        //先查redis,如果没有查一次调度模块
        boolean flag = redisUtil.hasKey(RedisKeyEnum.PIPEL_TRACE_ID.getName() + pipelTraceId);
        if (!flag) {
            ResultEntity<PipeDagDTO> taskLinkedList = dataFactoryClient.getTaskLinkedList(Long.valueOf(nifiGetPortHierarchy.workflowId));
            if (Objects.equals(ResultEnum.SUCCESS.getCode(), taskLinkedList.code)) {
                data = taskLinkedList.data;
                redisUtil.set(RedisKeyEnum.PIPEL_TRACE_ID.getName() + pipelTraceId, JSON.toJSONString(data), 3000);
            } else {
                log.error("调度模块无此调度的dag图:" + taskLinkedList.msg);
            }
        } else {
            //如果有,从redis里面拿
            String taskJson = redisUtil.get(RedisKeyBuild.buildDispatchStructureKey(Long.valueOf(nifiGetPortHierarchy.workflowId))).toString();
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
        log.info("该管道dag图:{}", JSON.toJSONString(data));
        return data;
    }

}
