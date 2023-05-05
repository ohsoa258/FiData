package com.fisk.task.pipeline2;

import com.alibaba.fastjson.JSON;
import com.davis.client.model.ProcessGroupEntity;
import com.davis.client.model.ProcessGroupStatusDTO;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.listener.pipeline.IPipelineTaskPublishCenter;
import com.fisk.task.po.app.TableNifiSettingPO;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.service.nifi.IOlap;
import com.fisk.task.service.nifi.ITableNifiSettingService;
import com.fisk.task.utils.KafkaTemplateHelper;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.StackTraceHelper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author cfk
 */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Component
public class DelayedTask2 extends TimerTask {
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

    private String topic;

    private KafkaReceiveDTO kafkaReceive;

    private String value;


    /**
     * @param kafkaReceive               上级信息
     * @param value                      验证是否还有延时任务  值为topic+uuid
     * @param topic                      本级topic
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
    public DelayedTask2(KafkaReceiveDTO kafkaReceive, String value, String topic, KafkaTemplateHelper kafkaTemplateHelper,
                        DataFactoryClient dataFactoryClient,
                        IOlap iOlap, IPipelJobLog iPipelJobLog, IPipelLog iPipelLog,
                        IPipelTaskLog iPipelTaskLog, RedisUtil redisUtil,
                        ITableNifiSettingService iTableNifiSettingService,
                        DataAccessClient dataAccessClient,
                        IPipelineTaskPublishCenter iPipelineTaskPublishCenter,
                        PublishTaskController publishTaskController
    ) {
        this.kafkaReceive = kafkaReceive;
        this.value = value;
        this.topic = topic;
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
        // 中转站  执行延时队列
        boolean exist = redisUtil.hasKey(topic);
        String delayedTask = "";
        if (exist) {
            log.info("key还没失效" + topic);
            return;
        }
        delayedTask = redisUtil.get(RedisKeyEnum.DELAYED_TASK.getName() + ":" + topic).toString();
        log.info("打印两个值:{},{}", delayedTask, value);
        if (!Objects.equals(delayedTask, value)) {
            return;
        }
        // 看rediskey是否失效,没失效,直接return  失效了发送卡夫卡消息到任务结束中心
        log.info("比较结果:{},{}", exist, !Objects.equals(delayedTask, value));
        try {
            String groupId = "";
            List<TableNifiSettingPO> list = new ArrayList<>();
            String[] split = topic.split("\\.");
            if (split.length == 6) {
                list = iTableNifiSettingService.query().eq("type", split[3]).eq("table_access_id", split[5]).list();
            } else if (split.length == 7) {
                list = iTableNifiSettingService.query().eq("type", split[4]).eq("table_access_id", split[6]).list();
            }
            if (!CollectionUtils.isEmpty(list)) {
                groupId = list.get(0).tableComponentId;
            }
            //只有是nifi处理的任务才有这个groupId
            if (StringUtils.isNotEmpty(groupId)) {
                ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(groupId);
                ProcessGroupStatusDTO status = processGroup.getStatus();
                //flowFilesQueued 组内流文件数量,如果为0代表组内所有流文件执行完,没有正在执行的组件
                Integer flowFilesQueued = status.getAggregateSnapshot().getFlowFilesQueued();
                if (!Objects.equals(flowFilesQueued, 0)) {
                    Timer timer = new Timer();
                    String value = UUID.randomUUID().toString();
                    //刷新时间和创建key或者修改value,会产生延时任务
                    redisUtil.set(RedisKeyEnum.DELAYED_TASK.getName() + ":" + topic, value, Long.parseLong(waitTime) * 100);
                    DelayedTask2 delayedTaskAgain = new DelayedTask2(kafkaReceive, value, topic, kafkaTemplateHelper, dataFactoryClient, iOlap, iPipelJobLog, iPipelLog, iPipelTaskLog, redisUtil, iTableNifiSettingService, dataAccessClient, iPipelineTaskPublishCenter, publishTaskController);
                    timer.schedule(delayedTaskAgain, (Long.parseLong(waitTime) + 5) * 1000);
                    return;
                }
            }
        } catch (Exception e) {
            log.error("查看组状态报错:{}", StackTraceHelper.getStackTraceInfo(e));
        }
        if (StringUtils.isNotEmpty(kafkaReceive.message)) {
            DispatchExceptionHandlingDTO dto = buildDispatchExceptionHandling(kafkaReceive);
            iPipelJobLog.exceptionHandlingLog(dto);
            Map<Object, Object> hmJob = redisUtil.hmget(RedisKeyEnum.PIPEL_JOB_TRACE_ID.getName() + ":" + dto.pipelTraceId);
            Map<Object, Object> hmTask = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + dto.pipelTraceId);
            log.info("修改完的job与task结构:{},{}", JSON.toJSONString(hmJob), JSON.toJSONString(hmTask));
        }
        // 任务结束中心的topic为 : task.build.task.over
        log.info("DelayedTask2发送到任务:{}", JSON.toJSONString(kafkaReceive));
        kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_TASK_OVER_FLOW, JSON.toJSONString(kafkaReceive));


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

}
