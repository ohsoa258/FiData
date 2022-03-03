package com.fisk.task.consumer.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.redis.RedisUtil;
import com.fisk.common.response.ResultEntity;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyNextDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.task.consumer.atlas.BuildAtlasTableAndColumnTaskListener;
import com.fisk.task.consumer.doris.BuildDataModelDorisTableListener;
import com.fisk.task.consumer.doris.BuildDorisTaskListener;
import com.fisk.task.consumer.nifi.BuildNifiCustomWorkFlow;
import com.fisk.task.consumer.nifi.BuildNifiTaskListener;
import com.fisk.task.consumer.postgre.datainput.BuildDataInputDeletePgTableListener;
import com.fisk.task.consumer.postgre.datainput.BuildDataInputPgTableListener;
import com.fisk.task.dto.task.TableTopicDTO;
import com.fisk.task.entity.OlapPO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.mapper.OlapMapper;
import com.fisk.task.service.pipeline.ITableTopicService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
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

import javax.annotation.Resource;

@Slf4j
@Component
public class ConsumerServer {
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
                                OlapPO olapPO = selectOlapPO(kafkaReceiveDTO.tableId, 1);
                                nifiGetPortHierarchyDTO.tableId = String.valueOf(olapPO.tableId);
                            } else {
                                nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.OLAP_FACT_TASK;
                                OlapPO olapPO = selectOlapPO(kafkaReceiveDTO.tableId, 2);
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

                    ResultEntity<NifiPortsHierarchyDTO> nIfiPortHierarchy = dataFactoryClient.getNIfiPortHierarchy(nifiGetPortHierarchyDTO);
                    NifiPortsHierarchyDTO data = nIfiPortHierarchy.data;
                    //本节点
                    NifiCustomWorkflowDetailDTO itselfPort = data.itselfPort;
                    TableTopicDTO topicSelf = iTableTopicService.getTableTopicDTOByComponentId(Math.toIntExact(itselfPort.id));
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
                                        redisUtil.expire(topicDTO.topicName, Long.parseLong(waitTime));
                                    } else {
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
                                redisUtil.expire(topicDTO.topicName, Long.parseLong(waitTime));
                            }
                        }
                    }
                }
            }
        }

        log.info("消费消息:end");
        }catch (Exception e){
            log.error("管道调度报错:"+e.getMessage());
        }finally {
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = {MqConstants.QueueConstants.BUILD_NIFI_FLOW}, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildNifiTaskListener(String data, Acknowledgment ack) {
        buildNifiTaskListener.msg(data,ack);
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_ATLAS_TABLECOLUMN_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildAtlasTableAndColumnTaskListener(String data, Acknowledgment ack) {
        buildAtlasTableAndColumnTaskListener.msg(data,ack);
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_DATAMODEL_DORIS_TABLE, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog(type = TraceTypeEnum.DATAMODEL_DORIS_TABLE_MQ_BUILD)
    public void buildDataModelDorisTableListener(String dataInfo, Acknowledgment acke) {
        buildDataModelDorisTableListener.msg(dataInfo,acke);
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_DORIS_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog(type = TraceTypeEnum.DORIS_MQ_BUILD)
    public void buildDorisTaskListener(String dataInfo, Acknowledgment acke) {
        buildDorisTaskListener.msg(dataInfo,acke);
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_CUSTOMWORK_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildNifiCustomWorkFlow(String dataInfo, Acknowledgment acke) {
        buildNifiCustomWorkFlow.msg(dataInfo,acke);
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_DATAINPUT_DELETE_PGSQL_TABLE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog(type = TraceTypeEnum.DATAINPUT_PG_TABLE_DELETE)
    public void buildDataInputDeletePgTableListener(String dataInfo, Acknowledgment acke) {
        buildDataInputDeletePgTableListener.msg(dataInfo,acke);
    }

    @KafkaListener(topics = MqConstants.QueueConstants.BUILD_DATAINPUT_PGSQL_TABLE_FLOW, containerFactory = "batchFactory", groupId = "test")
    @MQConsumerLog
    public void buildDataInputPgTableListener(String dataInfo, Acknowledgment acke) {
        buildDataInputPgTableListener.msg(dataInfo,acke);
    }




















    public OlapPO selectOlapPO(int id, int type) {
        HashMap<String, Object> conditionHashMap = new HashMap<>();
        OlapPO olapPO = new OlapPO();
        conditionHashMap.put("del_flag", 1);
        conditionHashMap.put("id", id);
        if (Objects.equals(type, DataClassifyEnum.CUSTOMWORKDATAMODELDIMENSIONKPL)) {
            conditionHashMap.put("type", 1);
        } else {
            conditionHashMap.put("type", 0);
        }
        List<OlapPO> olapPOS = olapMapper.selectByMap(conditionHashMap);
        if (olapPOS.size() > 0) {
            olapPO = olapPOS.get(0);
        } else {
            log.error("未找到对应指标表" + type + "表id" + id);
        }
        return olapPO;
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
}
