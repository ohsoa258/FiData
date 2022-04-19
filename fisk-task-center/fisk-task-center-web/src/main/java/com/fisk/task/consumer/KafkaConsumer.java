package com.fisk.task.consumer;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.framework.mdc.TraceTypeEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyNextDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.BuildTableNifiSettingDTO;
import com.fisk.task.dto.task.TableNifiSettingDTO;
import com.fisk.task.dto.task.TableTopicDTO;
import com.fisk.task.entity.OlapPO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.listener.atlas.BuildAtlasTableAndColumnTaskListener;
import com.fisk.task.listener.doris.BuildDataModelDorisTableListener;
import com.fisk.task.listener.doris.BuildDorisTaskListener;
import com.fisk.task.listener.mdm.BuildModelListener;
import com.fisk.task.listener.nifi.INifiTaskListener;
import com.fisk.task.listener.nifi.ITriggerScheduling;
import com.fisk.task.listener.nifi.impl.BuildNifiCustomWorkFlow;
import com.fisk.task.listener.nifi.impl.BuildNifiTaskListener;
import com.fisk.task.listener.olap.BuildModelTaskListener;
import com.fisk.task.listener.olap.BuildWideTableTaskListener;
import com.fisk.task.listener.postgre.datainput.BuildDataInputDeletePgTableListener;
import com.fisk.task.listener.postgre.datainput.BuildDataInputPgTableListener;
import com.fisk.task.mapper.NifiStageMapper;
import com.fisk.task.mapper.OlapMapper;
import com.fisk.task.mapper.PipelineTableLogMapper;
import com.fisk.task.service.nifi.INifiStage;
import com.fisk.task.service.nifi.IOlap;
import com.fisk.task.service.pipeline.ITableTopicService;
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


    //这里只用来存放reids
    @KafkaListener(topics = "${nifi.pipeline.topicName}", containerFactory = "batchFactory", groupId = "test")
    public void consumer(List<String> arrMessage, Acknowledgment ack) {
        log.info("消费消息:start");
        log.info("消费消息 size:" + arrMessage.size());
        //每次进来存进redis里面,key-value,都是topic-name,过期时间为5分钟
        try {
            for (String mapString : arrMessage) {
                log.info("mapString信息:" + mapString);
                if (mapString.contains("topic") && mapString.contains("table_id") && mapString.contains("table_type")) {
                    KafkaReceiveDTO kafkaReceiveDTO = JSON.parseObject(mapString, KafkaReceiveDTO.class);
                    if (kafkaReceiveDTO.topic != null && kafkaReceiveDTO.topic != "") {
                        String topicName = kafkaReceiveDTO.topic;
                        String[] split1 = topicName.split("\\.");
                        if (split1.length == 5) {
                            continue;
                        }
                        String pipelineName = split1[3];
                        //请求接口得到对象,条件--管道名称,表名称,表类别,表id,topic_name(加表名table_name)
                        NifiGetPortHierarchyDTO nifiGetPortHierarchyDTO = new NifiGetPortHierarchyDTO();
                        nifiGetPortHierarchyDTO.workflowName = pipelineName;
                        switch (kafkaReceiveDTO.tableType) {
                            case 0:
                                if (split1[5].contains("dim")) {
                                    nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.OLAP_DIMENSION_TASK;
                                    OlapPO olapPO = iOlap.selectOlapPO(kafkaReceiveDTO.tableId, 1);
                                    nifiGetPortHierarchyDTO.tableId = String.valueOf(olapPO.tableId);
                                } else {
                                    nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.OLAP_FACT_TASK;
                                    OlapPO olapPO = iOlap.selectOlapPO(kafkaReceiveDTO.tableId, 2);
                                    nifiGetPortHierarchyDTO.tableId = String.valueOf(olapPO.tableId);
                                }
                                break;
                            case 1:
                                nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.DW_DIMENSION_TASK;
                                nifiGetPortHierarchyDTO.tableId = String.valueOf(kafkaReceiveDTO.tableId);
                                break;
                            case 2:
                                nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.DW_FACT_TASK;
                                nifiGetPortHierarchyDTO.tableId = String.valueOf(kafkaReceiveDTO.tableId);
                                break;
                            case 3:
                                nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.DATALAKE_TASK;
                                nifiGetPortHierarchyDTO.tableId = String.valueOf(kafkaReceiveDTO.tableId);
                                break;
                            default:
                                break;
                        }

                        ResultEntity<NifiPortsHierarchyDTO> nIfiPortHierarchy = dataFactoryClient.getNifiPortHierarchy(nifiGetPortHierarchyDTO);
                        NifiPortsHierarchyDTO data = nIfiPortHierarchy.data;
                        //本节点
                        NifiCustomWorkflowDetailDTO itselfPort = data.itselfPort;
                        TableTopicDTO topicSelf = iTableTopicService.getTableTopicDTOByComponentId(Math.toIntExact(itselfPort.id));
                        //能走到最后说明这一批次走成功了
                        nifiStageMapper.updateByComponentId(Math.toIntExact(itselfPort.id));
                        pipelineTableLogMapper.updateByComponentId(Math.toIntExact(itselfPort.id));
                        //本节点topic
                        String topicName1 = topicSelf.topicName;
                        //下一级
                        List<NifiPortsHierarchyNextDTO> nextList = data.nextList;
                        if (nextList == null) {
                            continue;
                        }
                        for (NifiPortsHierarchyNextDTO nifiPortsHierarchyNextDTO : nextList) {
                            //下一级本身
                            NifiCustomWorkflowDetailDTO itselfPort1 = nifiPortsHierarchyNextDTO.itselfPort;
                            //下一级所有的上一级
                            List<NifiCustomWorkflowDetailDTO> upPortList = nifiPortsHierarchyNextDTO.upPortList;
                            //判断redis里面有没有这个key    itselfPort1(key,很关键,tnnd)
                            TableTopicDTO topicDTO = iTableTopicService.getTableTopicDTOByComponentId(Math.toIntExact(itselfPort1.id));
                            String topicKey = "";
                            Object key = redisUtil.get(topicDTO.topicName);
                            if (key == null) {
                                if (upPortList.size() == 1) {
                                    log.info("存入redis即将调用的节点1:" + topicDTO.topicName);
                                    redisUtil.set(topicDTO.topicName, topicSelf.topicName, Long.parseLong(waitTime));
                                } else {
                                    redisUtil.set(topicDTO.topicName, topicSelf.topicName, 3000L);
                                }
                            } else {
                                topicKey = key.toString();
                                String[] split = topicKey.split(",");
                                //意思是没全了,所有上游没有调完
                                if (split.length != upPortList.size()) {
                                    if (upPortList.size() - split.length <= 1) {
                                        if (topicKey.contains(topicSelf.topicName)) {
                                            log.info("存入redis即将调用的节点2:" + topicDTO.topicName);
                                            redisUtil.expire(topicDTO.topicName, Long.parseLong(waitTime));
                                        } else {
                                            log.info("存入redis即将调用的节点3:" + topicDTO.topicName);
                                            redisUtil.set(topicDTO.topicName, topicKey + "," + topicSelf.topicName, Long.parseLong(waitTime));
                                        }
                                    } else {
                                        if (topicKey.contains(topicSelf.topicName)) {
                                            redisUtil.expire(topicDTO.topicName, 3000L);
                                        } else {
                                            redisUtil.set(topicSelf.topicName, topicKey + "," + topicSelf.topicName, 3000L);
                                        }
                                    }
                                } else {
                                    log.info("存入redis即将调用的节点4:" + topicDTO.topicName);
                                    redisUtil.expire(topicDTO.topicName, Long.parseLong(waitTime));
                                }
                            }
                        }
                    }
                }
            }

            log.info("消费消息:end");
        } catch (Exception e) {
            log.error("管道调度报错");
            e.printStackTrace();
        } finally {
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = {MqConstants.QueueConstants.BUILD_NIFI_FLOW}, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildNifiTaskListener(String data, Acknowledgment ack) {
        iNifiTaskListener.msg(data, ack);
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_ATLAS_TABLECOLUMN_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildAtlasTableAndColumnTaskListener(String data, Acknowledgment ack) {
        buildAtlasTableAndColumnTaskListener.msg(data, ack);
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_DATAMODEL_DORIS_TABLE, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog(type = TraceTypeEnum.DATAMODEL_DORIS_TABLE_MQ_BUILD)
    public void buildDataModelDorisTableListener(String dataInfo, Acknowledgment acke) {
        buildDataModelDorisTableListener.msg(dataInfo, acke);
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_DORIS_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog(type = TraceTypeEnum.DORIS_MQ_BUILD)
    public void buildDorisTaskListener(String dataInfo, Acknowledgment acke) {
        buildDorisTaskListener.msg(dataInfo, acke);
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_CUSTOMWORK_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildNifiCustomWorkFlow(String dataInfo, Acknowledgment acke) {
        buildNifiCustomWorkFlow.msg(dataInfo, acke);
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_DATAINPUT_DELETE_PGSQL_TABLE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog(type = TraceTypeEnum.DATAINPUT_PG_TABLE_DELETE)
    public void buildDataInputDeletePgTableListener(String dataInfo, Acknowledgment acke) {
        buildDataInputDeletePgTableListener.msg(dataInfo, acke);
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_DATAINPUT_PGSQL_TABLE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog(type = TraceTypeEnum.DATAINPUT_PG_TABLE_BUILD)
    public void buildDataInputPgTableListener(String dataInfo, Acknowledgment acke) {
        buildDataInputPgTableListener.msg(dataInfo, acke);
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_OLAP_CREATEMODEL_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog(type = TraceTypeEnum.OLAP_CREATEMODEL_BUILD)
    public void buildModelTaskListener(String dataInfo, Acknowledgment acke) {
        buildModelTaskListener.msg(dataInfo, acke);
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_OLAP_WIDE_TABLE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildWideTableTaskListener(String dataInfo, Acknowledgment acke) {
        buildWideTableTaskListener.msg(dataInfo, acke);
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_TASK_BUILD_NIFI_DISPATCH_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildUnifiedControlTaskListener(String dataInfo, Acknowledgment acke) {
        iTriggerScheduling.unifiedControl(dataInfo, acke);
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_IMMEDIATELYSTART_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildImmediatelyStartTaskListener(String dataInfo, Acknowledgment acke) {
        try {
            BuildTableNifiSettingDTO buildTableNifiSettingDTO = JSON.parseObject(dataInfo, BuildTableNifiSettingDTO.class);
            List<TableNifiSettingDTO> tableNifiSettings = buildTableNifiSettingDTO.tableNifiSettings;
            if (!CollectionUtils.isEmpty(tableNifiSettings)) {
                for (TableNifiSettingDTO tableNifiSettingDTO : tableNifiSettings) {
                    iNiFiHelper.immediatelyStart(tableNifiSettingDTO);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            acke.acknowledge();
        }
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_GOVERNANCE_FIELD_STRONG_RULE_TEMPLATE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildFieldStrongTaskListener(String dataInfo, Acknowledgment acke) {
        //字段强规则模板消费接口
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_GOVERNANCE_FIELD_AGGREGATE_THRESHOLD_TEMPLATE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildFieldAggregateTaskListener(String dataInfo, Acknowledgment acke) {
        //字段聚合波动阈值模板消费接口
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_GOVERNANCE_ROWCOUNT_THRESHOLD_TEMPLATE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildTableRowThresholdTaskListener(String dataInfo, Acknowledgment acke) {
        //表行数波动阈值模板消费接口
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_GOVERNANCE_EMPTY_TABLE_CHECK_TEMPLATE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildEmptyTableCheckTaskListener(String dataInfo, Acknowledgment acke) {
        //空表校验模板消费接口
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_GOVERNANCE_UPDATE_TABLE_CHECK_TEMPLATE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildUpdateTableTaskListener(String dataInfo, Acknowledgment acke) {
        //表更新校验模板消费接口
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_GOVERNANCE_TABLE_BLOOD_KINSHIP_CHECK_TEMPLATE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildTableBloodKinshipTaskListener(String dataInfo, Acknowledgment acke) {
        //表血缘断裂校验模板消费接口
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_GOVERNANCE_BUSINESS_CHECK_TEMPLATE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildBusinessCheckTaskListener(String dataInfo, Acknowledgment acke) {
        //业务验证模板消费接口
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_GOVERNANCE_SIMILARITY_TEMPLATE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildSimilarityTaskListener(String dataInfo, Acknowledgment acke) {
        //相似度模板消费接口
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_GOVERNANCE_BUSINESS_FILTER_TEMPLATE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildBusinessFilterTaskListener(String dataInfo, Acknowledgment acke) {
        //业务清洗模板消费接口
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_GOVERNANCE_SPECIFY_TIME_RECYCLING_TEMPLATE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildSpecifyTimeRecyclingTaskListener(String dataInfo, Acknowledgment acke) {
        //指定时间回收模板消费接口
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_GOVERNANCE_EMPTY_TABLE_RECOVERY_TEMPLATE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildEmptyTableRecoveryTaskListener(String dataInfo, Acknowledgment acke) {
        //空表回收模板消费接口
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_GOVERNANCE_NO_REFRESH_DATA_RECOVERY_TEMPLATE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildNoRefreshDataRecoveryTaskListener(String dataInfo, Acknowledgment acke) {
        //数据无刷新回收模板消费接口
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_GOVERNANCE_DATA_BLOOD_KINSHIP_RECOVERY_TEMPLATE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildDataBloodKinshipRecoveryTaskListener(String dataInfo, Acknowledgment acke) {
        //数据血缘断裂回收模板消费接口
    }

    @KafkaListener(topics = "pipeline.supervision", containerFactory = "batchFactory", groupId = "test")
    public void saveNifiStage(String dataInfo, Acknowledgment acke) {
        iNifiStage.saveNifiStage(dataInfo, acke);
    }

    @Bean
    public KafkaListenerContainerFactory<?> batchFactory() {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(1);
        //factory.setAutoStartup(true);
        //设置为批量消费，每个批次数量在Kafka配置参数中设置ConsumerConfig.MAX_POLL_RECORDS_CONFIG
        factory.setBatchListener(true);
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
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerBootstrapServer);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        //props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        //props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        // props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-091231");
        //每一批数量
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, sessionTimeoutMs);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    // 7个模板7个方法

    @MQConsumerLog
    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_MDM_MODEL_DATA, containerFactory = "batchFactory" ,groupId = "test")
    public void buildModelListener(String dataInfo, Acknowledgment acke) {
        buildModelListener.msg(dataInfo, acke);
    }

}
