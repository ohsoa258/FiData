package com.fisk.task.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.constants.NifiConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.mdc.TraceTypeEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.task.dto.task.BuildTableNifiSettingDTO;
import com.fisk.task.dto.task.TableNifiSettingDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.listener.atlas.BuildAtlasTableAndColumnTaskListener;
import com.fisk.task.listener.doris.BuildDataModelDorisTableListener;
import com.fisk.task.listener.doris.BuildDorisTaskListener;
import com.fisk.task.listener.governance.BuildQualityReportListener;
import com.fisk.task.listener.mdm.BuildModelListener;
import com.fisk.task.listener.metadata.IMetaDataListener;
import com.fisk.task.listener.nifi.IExecScriptListener;
import com.fisk.task.listener.nifi.INifiTaskListener;
import com.fisk.task.listener.nifi.INonRealTimeListener;
import com.fisk.task.listener.nifi.ITriggerScheduling;
import com.fisk.task.listener.nifi.impl.BuildNifiCustomWorkFlow;
import com.fisk.task.listener.nifi.impl.BuildNifiTaskListener;
import com.fisk.task.listener.nifi.impl.BuildSftpCopyListener;
import com.fisk.task.listener.olap.BuildModelTaskListener;
import com.fisk.task.listener.olap.BuildWideTableTaskListener;
import com.fisk.task.listener.pipeline.IPipelineTaskPublishCenter;
import com.fisk.task.listener.postgre.datainput.BuildDataInputDeletePgTableListener;
import com.fisk.task.listener.postgre.datainput.BuildDataInputPgTableListener;
import com.fisk.task.mapper.NifiStageMapper;
import com.fisk.task.mapper.OlapMapper;
import com.fisk.task.mapper.PipelineTableLogMapper;
import com.fisk.task.pipeline2.HeartbeatService;
import com.fisk.task.pipeline2.MissionEndCenter;
import com.fisk.task.pipeline2.TaskPublish;
import com.fisk.task.service.nifi.INifiStage;
import com.fisk.task.service.nifi.IOlap;
import com.fisk.task.service.pipeline.ITableTopicService;
import com.fisk.task.utils.StackTraceHelper;
import com.fisk.task.utils.nifi.INiFiHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author cfk
 */
@Slf4j
@Component
public class KafkaConsumer {
    @Resource
    RedisUtil redisUtil;
    @Resource
    ITableTopicService iTableTopicService;
    @Resource
    private DataFactoryClient dataFactoryClient;
    @Resource
    OlapMapper olapMapper;
    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String consumerBootstrapServer;
    //
    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private String enableAutoCommit;
    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keyDeserializer;
    @Value("${spring.kafka.consumer.value-deserializer}")
    private String valueDeserializer;
    @Value("${spring.kafka.consumer.session.timeout.ms}")
    private String sessionTimeoutMs;
    @Value("${nifi.pipeline.waitTime}")
    private String waitTime;
    @Value("${nifi.Enable-Authentication}")
    public String enableAuthentication;
    @Value("${nifi.kerberos.login.config}")
    public String loginConfigPath;
    @Value("${nifi.kerberos.krb5.conf}")
    public String krb5ConfigPath;
    @Resource
    BuildNifiTaskListener buildNifiTaskListener;
    @Resource
    BuildAtlasTableAndColumnTaskListener buildAtlasTableAndColumnTaskListener;
    @Resource
    BuildDataModelDorisTableListener buildDataModelDorisTableListener;
    @Resource
    BuildDorisTaskListener buildDorisTaskListener;
    @Resource
    BuildNifiCustomWorkFlow buildNifiCustomWorkFlow;
    @Resource
    BuildDataInputDeletePgTableListener buildDataInputDeletePgTableListener;
    @Resource
    BuildDataInputPgTableListener buildDataInputPgTableListener;
    @Resource
    IOlap iOlap;
    @Resource
    INifiStage iNifiStage;
    @Resource
    NifiStageMapper nifiStageMapper;
    @Resource
    PipelineTableLogMapper pipelineTableLogMapper;
    @Resource
    BuildModelTaskListener buildModelTaskListener;
    @Resource
    BuildWideTableTaskListener buildWideTableTaskListener;
    @Resource
    INifiTaskListener iNifiTaskListener;
    @Resource
    INiFiHelper iNiFiHelper;
    @Resource
    ITriggerScheduling iTriggerScheduling;
    @Resource
    BuildModelListener buildModelListener;
    @Resource
    IPipelineTaskPublishCenter iPipelineTaskPublishCenter;
    @Resource
    INonRealTimeListener iNonRealTimeListener;
    @Resource
    IMetaDataListener metaDataListener;
    @Resource
    BuildQualityReportListener qualityReportListener;
    @Resource
    IExecScriptListener iExecScriptListener;
    @Resource
    HeartbeatService heartbeatService;
    @Resource
    MissionEndCenter missionEndCenter;
    @Resource
    TaskPublish taskPublish;
    @Resource
    BuildSftpCopyListener buildSftpCopyListener;


    @Bean
    public KafkaListenerContainerFactory<?> batchFactory() {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(1);
        //factory.setAutoStartup(true);
        //设置为批量消费，每个批次数量在Kafka配置参数中设置ConsumerConfig.MAX_POLL_RECORDS_CONFIG
        factory.setBatchListener(false);
        //设置提交偏移量的方式
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public ConsumerFactory consumerFactory() {
        return new DefaultKafkaConsumerFactory(consumerConfigs());

    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        if (Objects.equals(enableAuthentication, NifiConstants.enableAuthentication.ENABLE)) {
            System.setProperty("java.security.auth.login.config", loginConfigPath);
            System.setProperty("java.security.krb5.conf", krb5ConfigPath);
        }


        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerBootstrapServer);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        //props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        //props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        props.put("fetch.message.max.bytes", "6291456");
        // props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-091231");
        //每一批数量
        if (Objects.equals(enableAuthentication, NifiConstants.enableAuthentication.ENABLE)) {
            props.put("sasl.kerberos.service.name", "kafka");     //认证代码
            props.put("sasl.mechanism", "GSSAPI");                //认证代码
            props.put("security.protocol", "SASL_PLAINTEXT");
        }
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, sessionTimeoutMs);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    //任务发布中心,这里只用来存放reids
    @KafkaListener(topics = "my-topic", containerFactory = "batchFactory", groupId = "test")
    public void consumer(String message, Acknowledgment ack) {
        //iPipelineTaskPublishCenter.msg(message, ack);
        message = "[" + message + "]";
        heartbeatService.heartbeatService(message, ack);
    }

    /**
     * task.build.task.over
     *
     * @param message
     * @param ack
     */
    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_TASK_OVER_FLOW, containerFactory = "batchFactory", groupId = "test")
    public void missionEndCenter(String message, Acknowledgment ack) {
        missionEndCenter.missionEndCenter(message, ack);
    }

    /**
     * task.build.task.publish
     *
     * @param message
     * @param ack
     * @return
     */
    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_TASK_PUBLISH_FLOW, containerFactory = "batchFactory", groupId = "test")
    public void TaskPublish(String message, Acknowledgment ack) {
        taskPublish.taskPublish(message, ack);
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_NIFI_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public ResultEntity<Object> buildNifiTaskListener(String data, Acknowledgment ack) {
        return ResultEntityBuild.build(iNifiTaskListener.msg(data, ack));
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_TABLE_SERVER_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public ResultEntity<Object> buildDataServices(String data, Acknowledgment ack) {
        return ResultEntityBuild.build(iNifiTaskListener.buildDataServices(data, ack));
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_ATLAS_TABLECOLUMN_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public ResultEntity<Object> buildAtlasTableAndColumnTaskListener(String data, Acknowledgment ack) {
        return ResultEntityBuild.build(buildAtlasTableAndColumnTaskListener.msg(data, ack));
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_DATAMODEL_DORIS_TABLE, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog(type = TraceTypeEnum.DATAMODEL_DORIS_TABLE_MQ_BUILD)
    public ResultEntity<Object> buildDataModelDorisTableListener(String dataInfo, Acknowledgment acke) {
        return ResultEntityBuild.build(buildDataModelDorisTableListener.msg(dataInfo, acke));
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_DORIS_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog(type = TraceTypeEnum.DORIS_MQ_BUILD)
    public ResultEntity<Object> buildDorisTaskListener(String dataInfo, Acknowledgment acke) {
        return ResultEntityBuild.build(buildDorisTaskListener.msg(dataInfo, acke));
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_CUSTOMWORK_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public ResultEntity<Object> buildNifiCustomWorkFlow(String dataInfo, Acknowledgment acke) {
        return ResultEntityBuild.build(buildNifiCustomWorkFlow.msg(dataInfo, acke));
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_DATAINPUT_DELETE_PGSQL_TABLE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog(type = TraceTypeEnum.DATAINPUT_PG_TABLE_DELETE)
    public ResultEntity<Object> buildDataInputDeletePgTableListener(String dataInfo, Acknowledgment acke) {
        return ResultEntityBuild.build(buildDataInputDeletePgTableListener.msg(dataInfo, acke));
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_DATAINPUT_PGSQL_TABLE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog(type = TraceTypeEnum.DATAINPUT_PG_TABLE_BUILD)
    public ResultEntity<Object> buildDataInputPgTableListener(String dataInfo, Acknowledgment acke) {
        log.info("进入建表");
        return ResultEntityBuild.build(buildDataInputPgTableListener.msg(dataInfo, acke));
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_OLAP_CREATEMODEL_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog(type = TraceTypeEnum.OLAP_CREATEMODEL_BUILD)
    public ResultEntity<Object> buildModelTaskListener(String dataInfo, Acknowledgment acke) {
        return ResultEntityBuild.build(buildModelTaskListener.msg(dataInfo, acke));
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_OLAP_WIDE_TABLE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public ResultEntity<Object> buildWideTableTaskListener(String dataInfo, Acknowledgment acke) {
        return ResultEntityBuild.build(buildWideTableTaskListener.msg(dataInfo, acke));
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_TASK_BUILD_NIFI_DISPATCH_FLOW, containerFactory = "batchFactory", groupId = "test")
    //@MQConsumerLog
    public ResultEntity<Object> buildUnifiedControlTaskListener(String dataInfo, Acknowledgment acke) {
        return ResultEntityBuild.build(iTriggerScheduling.unifiedControl(dataInfo, acke));
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_IMMEDIATELYSTART_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public ResultEntity<Object> buildImmediatelyStartTaskListener(String dataInfo, Acknowledgment acke) {
        try {
            BuildTableNifiSettingDTO buildTableNifiSettingDTO = JSON.parseObject(dataInfo, BuildTableNifiSettingDTO.class);
            List<TableNifiSettingDTO> tableNifiSettings = buildTableNifiSettingDTO.tableNifiSettings;
            if (!CollectionUtils.isEmpty(tableNifiSettings)) {
                for (TableNifiSettingDTO tableNifiSettingDTO : tableNifiSettings) {
                    iNiFiHelper.immediatelyStart(tableNifiSettingDTO);
                }
            }
            return ResultEntityBuild.build(ResultEnum.SUCCESS);
        } catch (Exception e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
            return ResultEntityBuild.build(ResultEnum.ERROR);
        } finally {
            acke.acknowledge();
        }
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_GOVERNANCE_TEMPLATE_FLOW, containerFactory = "batchFactory", groupId = "test")
    //@MQConsumerLog
    public void buildQualityReportTaskListener(String dataInfo, Acknowledgment acke) {
        // 数据质量--质量报告 消费类
        qualityReportListener.msg(dataInfo, acke);
    }

    @KafkaListener(topics = "pipeline.supervision", containerFactory = "batchFactory", groupId = "test")
    public void saveNifiStage(String dataInfo, Acknowledgment acke) {
        iNifiStage.saveNifiStage(dataInfo, acke);
    }

    @MQConsumerLog
    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_MDM_MODEL_DATA, containerFactory = "batchFactory", groupId = "test")
    public ResultEntity<Object> buildModelListener(String dataInfo, Acknowledgment acke) {
        return ResultEntityBuild.build(buildModelListener.msg(dataInfo, acke));
    }

    @MQConsumerLog
    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_MDM_ENTITY_DATA, containerFactory = "batchFactory", groupId = "test")
    public ResultEntity<Object> buildEntityListener(String dataInfo, Acknowledgment acke) {
        return ResultEntityBuild.build(buildModelListener.backgroundCreateTasks(dataInfo, acke));
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_ACCESS_API_FLOW, containerFactory = "batchFactory", groupId = "test")
    public ResultEntity<Object> importData(String dataInfo, Acknowledgment acke) {
        return ResultEntityBuild.build(iNonRealTimeListener.importData(dataInfo, acke));
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_METADATA_FLOW, containerFactory = "batchFactory", groupId = "test")
    public ResultEntity<Object> buildMetaData(String dataInfo, Acknowledgment ack) {
        return ResultEntityBuild.build(metaDataListener.metaData(dataInfo, ack));
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_EXEC_SCRIPT_FLOW, containerFactory = "batchFactory", groupId = "test")
    public ResultEntity<Object> BuildExecScript(String dataInfo, Acknowledgment ack) {
        return ResultEntityBuild.build(iExecScriptListener.execScript(dataInfo, ack));
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_SFTP_FILE_COPY_FLOW, containerFactory = "batchFactory", groupId = "test")
    public ResultEntity<Object> BuildSftpCopyTask(String dataInfo, Acknowledgment ack) {
        log.info("进入sftp复制任务");
        return ResultEntityBuild.build(buildSftpCopyListener.sftpCopyTask(dataInfo, ack));
    }
}
