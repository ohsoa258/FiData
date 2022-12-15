package com.fisk.task.pipeline2;

import com.alibaba.fastjson.JSON;
import com.fisk.common.framework.mdc.MDCHelper;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.listener.pipeline.IPipelineTaskPublishCenter;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelLog;
import com.fisk.task.service.dispatchLog.IPipelStageLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.service.nifi.IOlap;
import com.fisk.task.service.nifi.ITableNifiSettingService;
import com.fisk.task.utils.KafkaTemplateHelper;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class HeartbeatService {
    @Resource
    RedisUtil redisUtil;
    @Value("${nifi.pipeline.waitTime}")
    private String waitTime;
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
    IPipelStageLog iPipelStageLog;

    public void heartbeatService(String data, Acknowledgment acke) {
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
                redisUtil.set(RedisKeyEnum.DELAYED_TASK.getName() + ":" + topic, value, 3000);
                redisUtil.set(topic, value, 30);
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
}
