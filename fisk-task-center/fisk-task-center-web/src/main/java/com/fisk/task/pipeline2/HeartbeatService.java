package com.fisk.task.pipeline2;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.davis.client.model.ProcessGroupEntity;
import com.davis.client.model.ProcessGroupStatusDTO;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.framework.mdc.MDCHelper;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.tasknifi.TaskHierarchyDTO;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.MyTopicStateEnum;
import com.fisk.task.listener.pipeline.IPipelineTaskPublishCenter;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.po.TableTopicPO;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelLog;
import com.fisk.task.service.dispatchLog.IPipelStageLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.service.nifi.IOlap;
import com.fisk.task.service.nifi.ITableNifiSettingService;
import com.fisk.task.service.pipeline.ITableTopicService;
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

@Slf4j
@Component
public class HeartbeatService {
    @Resource
    RedisUtil redisUtil;
    @Value("${nifi.pipeline.waitTime}")
    private String waitTime;
    @Value("${nifi.pipeline.maxTime}")
    public String maxTime;
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
    IPipelineTaskPublishCenter iPipelineTaskPublishCenter;
    @Resource
    ITableNifiSettingService iTableNifiSettingService;
    @Resource
    PublishTaskController publishTaskController;
    @Resource
    ITableTopicService tableTopicService;
    @Resource
    IPipelStageLog iPipelStageLog;

    public void heartbeatService2(String data, Acknowledgment acke) {
        //List<KafkaReceiveDTO>
        log.info("心跳服务,参数:{}", data);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<String> msg = JSON.parseArray(data, String.class);
            for (String message : msg) {
                KafkaReceiveDTO kafkaReceive = JSON.parseObject(message, KafkaReceiveDTO.class);
                String topic = kafkaReceive.topic;
                String pipelTraceId = kafkaReceive.pipelTraceId;
                //管道总的pipelTraceId
                if (StringUtils.isEmpty(kafkaReceive.pipelTraceId)) {
                    kafkaReceive.pipelTraceId = UUID.randomUUID().toString();
                    MDCHelper.setPipelTraceId(kafkaReceive.pipelTraceId);
                }

                Map<String, Object> map = new HashMap<>();
                map.put(DispatchLogEnum.taskend.getName(), simpleDateFormat.format(new Date()));
                map.put(DispatchLogEnum.taskcount.getName(), kafkaReceive.numbers + "");
                log.info("打印条数81" + JSON.toJSONString(map));
                redisUtil.hmset(RedisKeyEnum.PIPEL_TASK.getName() + ":" + kafkaReceive.pipelTaskTraceId, map, 3600);
                // 第一步  创建rediskey 先判断有没有这个key,没有创建,设置过期时间30秒
                String value = UUID.randomUUID().toString();
                //刷新时间和创建key或者修改value,会产生延时任务
                redisUtil.set(RedisKeyEnum.DELAYED_TASK.getName() + ":" + topic, value, Long.parseLong(waitTime) * 100);
                redisUtil.set(topic, value, Long.parseLong(waitTime));
                // 第二步  创建延时队列,延时时间要比rediskey过期时间长5秒,且可配置,需要携带的参数有报错信息
                DelayedTask2 delayedTask2 = new DelayedTask2(kafkaReceive, value, topic, kafkaTemplateHelper,
                        dataFactoryClient,
                        iOlap, iPipelJobLog, iPipelLog,
                        iPipelTaskLog, redisUtil,
                        iTableNifiSettingService,
                        dataAccessClient,
                        iPipelineTaskPublishCenter,
                        publishTaskController);
                Timer timer = new Timer();
                timer.schedule(delayedTask2, (Long.parseLong(waitTime) + 5) * 1000);
                //记报错日志
                if (!StringUtils.isEmpty(kafkaReceive.message)) {
                    // 第三步  如果有报错,记录报错信息
                    Map<Integer, Object> errorMap = new HashMap<>();
                    errorMap.put(DispatchLogEnum.stagestate.getValue(), kafkaReceive.message);
                    iPipelStageLog.savePipelTaskStageLog(kafkaReceive.pipelStageTraceId, kafkaReceive.pipelTaskTraceId, errorMap);
                }
            }
        } catch (Exception e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
        } finally {
            acke.acknowledge();
        }
    }

    public void endService(String data, Acknowledgment acke) {
        //List<KafkaReceiveDTO>
        log.info("my-topic服务,参数:{}", data);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<String> msg = JSON.parseArray(data, String.class);
            log.info("my-topic接收条数{}", msg.size());
            for (String message : msg) {
                KafkaReceiveDTO kafkaReceive = JSON.parseObject(message, KafkaReceiveDTO.class);
                Boolean setnx;
                do {
                    Thread.sleep(200);
                    log.info("missionEndCenter获取锁PipelLock:{}",kafkaReceive.pipelTraceId);
                    setnx = redisUtil.setnx("PipelLock:"+kafkaReceive.pipelTraceId, 30, TimeUnit.SECONDS);
                } while (!setnx);
                String topic = kafkaReceive.topic;
                String pipelTraceId = kafkaReceive.pipelTraceId;
                //管道总的pipelTraceId
                if (StringUtils.isEmpty(kafkaReceive.pipelTraceId)) {
                    kafkaReceive.pipelTraceId = UUID.randomUUID().toString();
                    MDCHelper.setPipelTraceId(kafkaReceive.pipelTraceId);
                }
                String[] split = topic.split("\\.");
                LambdaQueryWrapper<TableTopicPO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(TableTopicPO::getTopicName, topic).eq(TableTopicPO::getDelFlag, 1);
                TableTopicPO topicPO = tableTopicService.getOne(queryWrapper);
                if (split.length == 7){
                    Map<String, Object> map = new HashMap<>();
                    map.put(DispatchLogEnum.taskend.getName(), simpleDateFormat.format(new Date()));
                    map.put(DispatchLogEnum.taskcount.getName(), kafkaReceive.numbers + "");
                    log.info("打印条数81" + JSON.toJSONString(map));
                    redisUtil.hmset(RedisKeyEnum.PIPEL_TASK.getName() + ":" + kafkaReceive.pipelTaskTraceId, map, 3600);
                    // 获取redis中TASK任务
                    Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + pipelTraceId);
                    //获取my-topic运行状态
                    TaskHierarchyDTO taskHierarchy = JSON.parseObject(hmget.get(topicPO.getComponentId().toString()).toString(), TaskHierarchyDTO.class);
                    if (taskHierarchy.myTopicState.equals(MyTopicStateEnum.RUNNING)) {
                        continue;
                    }
                    taskHierarchy.setMyTopicState(MyTopicStateEnum.RUNNING);
                    HashMap<Object, Object> map1 = new HashMap<>();
                    map1.put(topicPO.getComponentId().toString(), JSON.toJSONString(taskHierarchy));
                    //更新my-topic运行状态
                    redisUtil.hmsetForDispatch(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + kafkaReceive.pipelTraceId, map1, Long.parseLong(maxTime));
                    sendKafka(topicPO, kafkaReceive, msg.size());
                }else if (split.length == 6){
                    String state = (String) redisUtil.get(RedisKeyEnum.DELAYED_TASK.getName() + ":" + kafkaReceive.pipelTaskTraceId);
                    if (state.equals(MyTopicStateEnum.RUNNING.getName())) {
                        continue;
                    }
                    redisUtil.set(RedisKeyEnum.DELAYED_TASK.getName() + ":" + kafkaReceive.pipelTaskTraceId, MyTopicStateEnum.RUNNING.getName(), Long.parseLong(maxTime));
                    sendKafka(topicPO, kafkaReceive, msg.size());
                }

                //记报错日志
                if (!StringUtils.isEmpty(kafkaReceive.message)) {
                    // 第三步  如果有报错,记录报错信息
                    Map<Integer, Object> errorMap = new HashMap<>();
                    errorMap.put(DispatchLogEnum.stagestate.getValue(), kafkaReceive.message);
                    iPipelStageLog.savePipelTaskStageLog(kafkaReceive.pipelStageTraceId, kafkaReceive.pipelTaskTraceId, errorMap);
                }
                redisUtil.del("PipelLock:"+kafkaReceive.pipelTraceId);
            }
        } catch (Exception e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
        } finally {
            acke.acknowledge();
        }
    }

    /**
     * 构建 TableMetaDataObject 对象
     */
    public static DispatchExceptionHandlingDTO buildDispatchExceptionHandling(KafkaReceiveDTO kafkaReceive) {
        /*dispatchExceptionHandlingDTO.pipleName = pipelName;
        dispatchExceptionHandlingDTO.JobName = jobName;*/
        return DispatchExceptionHandlingDTO.builder()
                .comment("发布中心报错")
                .pipelTraceId(kafkaReceive.pipelTraceId)
                .pipelJobTraceId(kafkaReceive.pipelJobTraceId)
                .pipelStageTraceId(kafkaReceive.pipelStageTraceId)
                .pipelTaskTraceId(kafkaReceive.pipelTaskTraceId)
                .build();
    }


    public void sendKafka(TableTopicPO topicPO, KafkaReceiveDTO kafkaReceive, Integer msgNum) throws Exception {
        String groupId = "";
        List<TableNifiSettingPO> list = new ArrayList<>();
        String[] split = topicPO.getTopicName().split("\\.");
        if (split.length == 6) {
            list = iTableNifiSettingService.query().eq("type", split[3]).eq("table_access_id", split[5]).list();
        } else if (split.length == 7) {
            list = iTableNifiSettingService.query().eq("type", split[4]).eq("table_access_id", split[6]).list();
        }
        if (!CollectionUtils.isEmpty(list)) {
            groupId = list.get(0).tableComponentId;
        }
        //只有是nifi处理的任务才有这个groupId
        if (!StringUtils.isEmpty(groupId)) {
            Integer flowFilesQueued;
            Integer activeThreadCount;
            do {
                Thread.sleep(500);
                ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(groupId);
                ProcessGroupStatusDTO status = processGroup.getStatus();
                //flowFilesQueued 组内流文件数量,如果为0代表组内无流文件
                //activeThreadCount 组内活跃线程数量，为0代表没有正在工作的组件
                flowFilesQueued = status.getAggregateSnapshot().getFlowFilesQueued();
                activeThreadCount = status.getAggregateSnapshot().getActiveThreadCount();
                log.info("管道内剩余流文件flowFilesQueued:{}", flowFilesQueued);
                log.info("管道内正在执行线程数activeThreadCount:{}", activeThreadCount);
            } while (activeThreadCount != 0 && flowFilesQueued != 0);
            if (!StringUtils.isEmpty(kafkaReceive.message)) {
                DispatchExceptionHandlingDTO dto = buildDispatchExceptionHandling(kafkaReceive);
                iPipelJobLog.exceptionHandlingLog(dto);
                Map<Object, Object> hmJob = redisUtil.hmget(RedisKeyEnum.PIPEL_JOB_TRACE_ID.getName() + ":" + dto.pipelTraceId);
                Map<Object, Object> hmTask = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + dto.pipelTraceId);
                log.info("修改完的job与task结构:{},{}", JSON.toJSONString(hmJob), JSON.toJSONString(hmTask));
            }
            // 任务结束中心的topic为 : task.build.task.over
            log.info("my-topic服务发送到任务:{}", JSON.toJSONString(kafkaReceive));
            kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_TASK_OVER_FLOW, JSON.toJSONString(kafkaReceive));
        }
    }
}
