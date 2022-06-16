package com.fisk.task.listener.pipeline.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.dto.api.ApiImportDataDTO;
import com.fisk.dataaccess.dto.api.PipelApiDispatchDTO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyNextDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.TableTopicDTO;
import com.fisk.task.entity.*;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.pipeline.IBuildPipelineSupervisionListener;
import com.fisk.task.mapper.NifiStageMapper;
import com.fisk.task.mapper.PipelineTableLogMapper;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelStageLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.service.nifi.IOlap;
import com.fisk.task.service.pipeline.ITableTopicService;
import com.fisk.task.utils.KafkaTemplateHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

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
    @Resource
    IPipelJobLog iPipelJobLog;
    @Resource
    IPipelTaskLog iPipelTaskLog;
    @Resource
    IPipelStageLog iPipelStageLog;


    @Override
    public void msg(List<String> arrMessage, Acknowledgment acke) {
        log.info("消费消息:start");
        log.info("消费消息 size:" + arrMessage.size());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //每次进来存进redis里面,key-value,都是topic-name,过期时间为5分钟
        try {
            for (String mapString : arrMessage) {
                log.info("mapString信息:" + mapString);

                KafkaReceiveDTO kafkaReceiveDTO = JSON.parseObject(mapString, KafkaReceiveDTO.class);
                //管道总的pipelTraceId
                kafkaReceiveDTO.pipelTraceId = UUID.randomUUID().toString();
                if (kafkaReceiveDTO.topic != null && kafkaReceiveDTO.topic != "") {
                    String topicName = kafkaReceiveDTO.topic;
                    String[] split1 = topicName.split("\\.");
                    String pipelineId = split1[3];
                    if (split1.length == 6) {
                        //卡夫卡的内容在发布时就定义好了
                        log.info("打印topic内容:" + JSON.toJSONString(kafkaReceiveDTO));
                        if (kafkaReceiveDTO.ifDown) {
                            log.info("发送的topic1:{},内容:{}",topicName,mapString);
                            kafkaTemplateHelper.sendMessageAsync(topicName, mapString);
                        }
                        redisUtil.set("hand" + kafkaReceiveDTO.pipelTaskTraceId + "," + topicName, topicName, 30);
                        continue;
                    } else if (split1.length == 4) {
                        //  这个时候可能是api的topic,可能是管道直接调度的topic,保存管道开始,job开始 定义管道traceid  定义job的traceid

                        //流程开始时间
                        kafkaReceiveDTO.start_time = simpleDateFormat.format(new Date());
                        //nifi流程要的批次号
                        kafkaReceiveDTO.fidata_batch_code = UUID.randomUUID().toString();

                        log.info("打印topic内容:" + JSON.toJSONString(kafkaReceiveDTO));
                        if (StringUtils.isEmpty(kafkaReceiveDTO.pipelApiDispatch)) {
                            //管道开始,job开始,task开始
                            List<TableTopicDTO> topicNames = iTableTopicService.getByTopicName(topicName);
                            for (TableTopicDTO topic : topicNames) {
                                //job批次号
                                kafkaReceiveDTO.pipelJobTraceId = UUID.randomUUID().toString();
                                //task批次号
                                kafkaReceiveDTO.pipelTaskTraceId = UUID.randomUUID().toString();
                                kafkaReceiveDTO.topic = topic.topicName;
                                String[] split = topic.topicName.split("\\.");
                                log.info("发送的topic2:{},内容:{}",topic.topicName,JSON.toJSONString(kafkaReceiveDTO));
                                kafkaTemplateHelper.sendMessageAsync(topic.topicName, JSON.toJSONString(kafkaReceiveDTO));
                                //-----------------------------------------------------
                                String pipelTraceId = kafkaReceiveDTO.pipelTraceId;
                                Map<Integer, Object> PipelMap = new HashMap<>();
                                PipelMap.put(DispatchLogEnum.pipelstart.getValue(), simpleDateFormat.format(new Date()));
                                PipelMap.put(DispatchLogEnum.pipelstate.getValue(), NifiStageTypeEnum.RUNNING.getName());
                                //管道开始日志
                                iPipelJobLog.savePipelLogAndJobLog(pipelTraceId, PipelMap, split1[3], null, null);
                                //job开始日志
                                Map<Integer, Object> jobMap = new HashMap<>();
                                NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(pipelineId, Integer.valueOf(split[4]), null, Integer.valueOf(split[6]));
                                ResultEntity<NifiPortsHierarchyDTO> nifiPortHierarchy = dataFactoryClient.getNifiPortHierarchy(nifiGetPortHierarchy);
                                //任务依赖的组件
                                jobMap.put(DispatchLogEnum.jobstart.getValue(), simpleDateFormat.format(new Date()));
                                jobMap.put(DispatchLogEnum.jobstate.getValue(), NifiStageTypeEnum.RUNNING.getName());
                                iPipelJobLog.savePipelLogAndJobLog(kafkaReceiveDTO.pipelTraceId, jobMap, split1[3], kafkaReceiveDTO.pipelJobTraceId, String.valueOf(nifiPortHierarchy.data.itselfPort.pid));
                                //task日志
                                HashMap<Integer, Object> taskMap = new HashMap<>();
                                taskMap.put(DispatchLogEnum.taskstart.getValue(), simpleDateFormat.format(new Date()));
                                taskMap.put(DispatchLogEnum.taskstate.getValue(), NifiStageTypeEnum.RUNNING.getName());
                                iPipelTaskLog.savePipelTaskLog(kafkaReceiveDTO.pipelJobTraceId, kafkaReceiveDTO.pipelTaskTraceId, taskMap, String.valueOf(nifiPortHierarchy.data.itselfPort.id), null, 0);

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
                                log.info("发送的topic3:{},内容:{}",topicName,JSON.toJSONString(apiImportData));
                                kafkaTemplateHelper.sendMessageAsync(topicName, JSON.toJSONString(apiImportData));
                            }

                        }

                        break;
                    }

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
                   /* pipelineTableLogMapper.updateByComponentId(Math.toIntExact(itselfPort.id), Integer.valueOf(nifiGetPortHierarchyDTO.tableId), kafkaReceiveDTO.tableType);
                    QueryWrapper<PipelineTableLogPO> Wrapper = new QueryWrapper<>();
                    Wrapper.lambda().eq(PipelineTableLogPO::getComponentId, itselfPort.id)
                            .eq(PipelineTableLogPO::getTableId, nifiGetPortHierarchyDTO.tableId)
                            .eq(PipelineTableLogPO::getTableType, kafkaReceiveDTO.tableType).orderByDesc();
                    List<PipelineTableLogPO> pipelineTableLogs = pipelineTableLogMapper.selectList(Wrapper);
                    for (PipelineTableLogPO pipelineTableLog:pipelineTableLogs) {
                        nifiStageMapper.updateByComponentId(Math.toIntExact(itselfPort.id), pipelineTableLog.id);
                    }*/

                    //本节点topic
                    String topicName1 = topicSelf.topicName;
                    //下一级
                    List<NifiPortsHierarchyNextDTO> nextList = data.nextList;
                    if (nextList == null) {
                        //完成时间要去rediskey失效那里做,需要有一个标识告诉我这次失效代表什么 fiskgd--fisk管道
                        String hmgetKey = "fiskgd:" + kafkaReceiveDTO.pipelTraceId;
                        Boolean ifexist = true;
                        //通过这个管道key查是否所有支线末端都走完了,如果没有不记录结束时间
                        Map<Object, Object> hmget = redisUtil.hmget(hmgetKey);
                        //便利已存在的末端
                        Iterator<Map.Entry<Object, Object>> nodeMap = hmget.entrySet().iterator();
                        while (nodeMap.hasNext()) {
                            Map.Entry<Object, Object> next = nodeMap.next();
                            String key = next.getKey().toString();
                            if (Objects.equals(key, topicName1)) {
                                ifexist = false;
                            }
                        }
                        if (ifexist) {
                            //如果map里面不存在,装进去
                            hmget.put(topicName1, topicName1);
                            if (hmget.size() == data.pipeEndDto.size()) {
                                //如果结束支点就它一个,装进去等30秒
                                redisUtil.hmsset(hmgetKey, hmget, 30);
                            } else {
                                //如果结束支点不止它一个,不仅要装进去,还要等其他支点
                                redisUtil.hmsset(hmgetKey, hmget, 3000);
                            }
                        } else {
                            //如果map里面存在,判断map的记录个数,如果不是所有支点结束,刷新过期时间3000
                            if (hmget.size() == data.pipeEndDto.size()) {
                                //如果满足有所有支点的条件了,就刷新过期时间30秒
                                redisUtil.hmsset(hmgetKey, hmget, 30);

                            } else {
                                redisUtil.hmsset(hmgetKey, hmget, 3000);
                            }
                        }

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
                        String topicContent = "";
                        //topic需要加上一个批次号   管道的  不然redis失效那里不好判断这个任务属于哪个批次 具体命名规范为  原本topic+管道批次
                        String topic = topicDTO.topicName + "," + kafkaReceiveDTO.pipelTraceId;
                        Object key = redisUtil.get(topic);
                        if (key == null) {
                            if (upPortList.size() == 1) {
                                log.info("存入redis即将调用的节点1:" + topicDTO.topicName);
                                redisUtil.set(topic, topicSelf.topicName, Long.parseLong(waitTime));
                            } else {
                                redisUtil.set(topic, topicSelf.topicName, 3000L);
                            }
                        } else {
                            topicContent = key.toString();
                            String[] split = topicContent.split(",");
                            //意思是没全了,所有上游没有调完
                            if (split.length != upPortList.size()) {
                                if (upPortList.size() - split.length <= 1) {
                                    if (topicContent.contains(topicSelf.topicName)) {
                                        log.info("存入redis即将调用的节点2:" + topicDTO.topicName);
                                        redisUtil.expire(topic, Long.parseLong(waitTime));
                                    } else {
                                        log.info("存入redis即将调用的节点3:" + topicDTO.topicName);
                                        redisUtil.set(topic, topicContent + "," + topicSelf.topicName, Long.parseLong(waitTime));
                                    }
                                } else {
                                    if (topicContent.contains(topicSelf.topicName)) {
                                        redisUtil.expire(topic, 3000L);
                                    } else {
                                        redisUtil.set(topic, topicContent + "," + topicSelf.topicName, 3000L);
                                    }
                                }
                            } else {
                                log.info("存入redis即将调用的节点4:" + topicDTO.topicName);
                                redisUtil.expire(topic, Long.parseLong(waitTime));
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
