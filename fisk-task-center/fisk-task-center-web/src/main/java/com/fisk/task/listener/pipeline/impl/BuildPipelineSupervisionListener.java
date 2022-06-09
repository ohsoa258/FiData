package com.fisk.task.listener.pipeline.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.dto.api.ApiImportDataDTO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyNextDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.TableTopicDTO;
import com.fisk.task.entity.OlapPO;
import com.fisk.task.entity.PipelineTableLogPO;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.pipeline.IBuildPipelineSupervisionListener;
import com.fisk.task.mapper.NifiStageMapper;
import com.fisk.task.mapper.PipelineTableLogMapper;
import com.fisk.task.service.nifi.IOlap;
import com.fisk.task.service.pipeline.ITableTopicService;
import com.fisk.task.utils.KafkaTemplateHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: cfk
 * CreateTime: 2022/04/21 15:05
 * Description:
 */
@Slf4j
@Component
public class BuildPipelineSupervisionListener implements IBuildPipelineSupervisionListener {
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

    @Override
    public void msg(List<String> arrMessage, Acknowledgment acke) {
        log.info("消费消息:start");
        log.info("消费消息 size:" + arrMessage.size());
        //每次进来存进redis里面,key-value,都是topic-name,过期时间为5分钟
        try {
            for (String mapString : arrMessage) {
                log.info("mapString信息:" + mapString);

                KafkaReceiveDTO kafkaReceiveDTO = JSON.parseObject(mapString, KafkaReceiveDTO.class);
                if (kafkaReceiveDTO.topic != null && kafkaReceiveDTO.topic != "") {
                    String topicName = kafkaReceiveDTO.topic;
                    String[] split1 = topicName.split("\\.");
                    if (split1.length == 6) {
                        continue;
                    } else if (split1.length == 4) {
                        //  这个时候可能是api的topic,可能是管道直接调度的topic
                        if (StringUtils.isEmpty(kafkaReceiveDTO.pipelApiDispatch)) {
                            //管道开始,job开始,task开始
                            kafkaTemplateHelper.sendMessageAsync(topicName, "发布调度第一步流程");
                        } else {
                            ApiImportDataDTO apiImportData = new ApiImportDataDTO();
                            apiImportData.pipelApiDispatch = kafkaReceiveDTO.pipelApiDispatch;
                            kafkaTemplateHelper.sendMessageAsync(topicName, JSON.toJSONString(apiImportData));
                        }
                        break;
                    }
                    String pipelineId = split1[3];
                    //请求接口得到对象,条件--管道名称,表名称,表类别,表id,topic_name(加表名table_name)
                    NifiGetPortHierarchyDTO nifiGetPortHierarchyDTO = new NifiGetPortHierarchyDTO();
                    nifiGetPortHierarchyDTO.workflowId = pipelineId;
                    nifiGetPortHierarchyDTO.nifiCustomWorkflowDetailId = kafkaReceiveDTO.nifiCustomWorkflowDetailId;
                    switch (kafkaReceiveDTO.tableType) {
                        case 0:
                            OlapPO olapPO = iOlap.selectOlapPO(kafkaReceiveDTO.tableId);
                            nifiGetPortHierarchyDTO.tableId = String.valueOf(olapPO.tableId);
                            if (olapPO.tableName != null && olapPO.tableName.contains("dim_")) {
                                nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.OLAP_DIMENSION_TASK;
                            } else {
                                nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.OLAP_FACT_TASK;
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
                        case 9:
                            nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.OLAP_WIDETABLE_TASK;
                            nifiGetPortHierarchyDTO.tableId = String.valueOf(kafkaReceiveDTO.tableId);
                            break;
                        case 10:
                            nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.DATALAKE_API_TASK;
                            nifiGetPortHierarchyDTO.tableId = String.valueOf(kafkaReceiveDTO.tableId);
                            break;
                        default:
                            break;
                    }
                    ResultEntity<NifiPortsHierarchyDTO> nIfiPortHierarchy = dataFactoryClient.getNifiPortHierarchy(nifiGetPortHierarchyDTO);
                    NifiPortsHierarchyDTO data = nIfiPortHierarchy.data;
                    //本节点
                    NifiCustomWorkflowDetailDTO itselfPort = data.itselfPort;
                    TableTopicDTO topicSelf = iTableTopicService.getTableTopicDTOByComponentId(Math.toIntExact(itselfPort.id),
                            Integer.valueOf(nifiGetPortHierarchyDTO.tableId), kafkaReceiveDTO.tableType);
                    //能走到最后说明这一批次走成功了
                    pipelineTableLogMapper.updateByComponentId(Math.toIntExact(itselfPort.id), Integer.valueOf(nifiGetPortHierarchyDTO.tableId), kafkaReceiveDTO.tableType);
                    QueryWrapper<PipelineTableLogPO> Wrapper = new QueryWrapper<>();
                    Wrapper.lambda().eq(PipelineTableLogPO::getComponentId, itselfPort.id)
                            .eq(PipelineTableLogPO::getTableId, nifiGetPortHierarchyDTO.tableId)
                            .eq(PipelineTableLogPO::getTableType, kafkaReceiveDTO.tableType);
                    PipelineTableLogPO pipelineTableLogPO = pipelineTableLogMapper.selectOne(Wrapper);
                    nifiStageMapper.updateByComponentId(Math.toIntExact(itselfPort.id), pipelineTableLogPO.id);
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
                        ChannelDataEnum channel = ChannelDataEnum.getValue(itselfPort1.componentType);
                        OlapTableEnum olapTableEnum = ChannelDataEnum.getOlapTableEnum(channel.getValue());
                        log.info("表类别:", olapTableEnum);
                        //下一级所有的上一级
                        List<NifiCustomWorkflowDetailDTO> upPortList = nifiPortsHierarchyNextDTO.upPortList;
                        //判断redis里面有没有这个key    itselfPort1(key,很关键,tnnd)
                        TableTopicDTO topicDTO = iTableTopicService.getTableTopicDTOByComponentId(Math.toIntExact(itselfPort1.id),
                                Integer.valueOf(itselfPort1.tableId), olapTableEnum.getValue());
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

            log.info("消费消息:end");
        } catch (Exception e) {
            log.error("管道调度报错");
            e.printStackTrace();
        } finally {
            if (acke != null) {
                acke.acknowledge();
            }
        }
    }
}
