package com.fisk.task.listener.redis;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.api.ApiImportDataDTO;
import com.fisk.dataaccess.dto.api.PipelApiDispatchDTO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.entity.PipelJobLogPO;
import com.fisk.task.entity.PipelTaskLogPO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.service.nifi.IOlap;
import com.fisk.task.utils.KafkaTemplateHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class RedisKeyExpirationListeners extends KeyExpirationEventMessageListener {
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
    IPipelTaskLog iPipelTaskLog;


    public RedisKeyExpirationListeners(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 针对redis数据失效事件，进行数据处理
     *
     * @param message
     * @param pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 用户做自己的业务处理即可,注意message.toString()可以获取失效的key
        String expiredKey = message.toString();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("即将调用的节点:" + expiredKey);
        String thisPipelTaskTraceId = UUID.randomUUID().toString();
        String thisPipelJobTraceId = UUID.randomUUID().toString();
        String thisPipelStageTraceId = UUID.randomUUID().toString();
        String upPipelJobTraceId = "";

        //用户key失效不做处理
        if (!expiredKey.toLowerCase().contains("auth") && !expiredKey.startsWith("fiskgd") && !expiredKey.startsWith("hand")) {
            //分割
            String[] split1 = expiredKey.split(",");
            String topic = split1[0];
            String[] split = topic.split("\\.");
            String pipelTraceId = split1[1];
            boolean ifEndJob = false;
            //查找所有的上一级
            NifiGetPortHierarchyDTO nifiGetPortHierarchy = iOlap.getNifiGetPortHierarchy(split[3], Integer.valueOf(split[4]), null, Integer.valueOf(split[6]));
            ResultEntity<NifiPortsHierarchyDTO> nifiPortHierarchy =
                    dataFactoryClient.getNifiPortHierarchy(nifiGetPortHierarchy);
            NifiPortsHierarchyDTO data = nifiPortHierarchy.data;
            NifiCustomWorkflowDetailDTO itselfPort = data.itselfPort;
            List<NifiCustomWorkflowDetailDTO> inportList = data.inportList;
            for (NifiCustomWorkflowDetailDTO dto : inportList) {
                //上一级的信息
                PipelJobLogPO byPipelTraceId = iPipelJobLog.getByPipelTraceId(pipelTraceId, dto.pid);
                PipelTaskLogPO byPipelJobTraceId = iPipelTaskLog.getByPipelJobTraceId(byPipelTraceId.jobTraceId, dto.id);
                String pipelTaskTraceId = byPipelJobTraceId.taskTraceId;
                // 1.要记录上一个task结束
                Map<Integer, Object> taskMap = new HashMap<>();
                taskMap.put(DispatchLogEnum.taskstate.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());
                taskMap.put(DispatchLogEnum.taskend.getValue(), simpleDateFormat.format(new Date()));
                ChannelDataEnum value = ChannelDataEnum.getValue(dto.componentType);
                OlapTableEnum olapTableEnum = ChannelDataEnum.getOlapTableEnum(value.getValue());
                iPipelTaskLog.savePipelTaskLog(byPipelJobTraceId.jobTraceId, byPipelJobTraceId.taskTraceId, taskMap, byPipelJobTraceId.taskId, dto.tableId, olapTableEnum.getValue());
                if (!Objects.equals(itselfPort.pid, dto.pid)) {
                    //说明这个组结束了
                    //2.记录上一个job结束
                    Map<Integer, Object> upJobMap = new HashMap<>();
                    upJobMap.put(DispatchLogEnum.jobstate.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());
                    upJobMap.put(DispatchLogEnum.jobend.getValue(), simpleDateFormat.format(new Date()));
                    iPipelJobLog.savePipelLogAndJobLog(pipelTraceId, upJobMap, split[3], byPipelJobTraceId.jobTraceId, byPipelTraceId.componentId);
                    ifEndJob = true;

                } else {
                    upPipelJobTraceId = byPipelJobTraceId.jobTraceId;
                }


            }
            if (ifEndJob) {
                //3.记录这个job开始
                Map<Integer, Object> thisJobMap = new HashMap<>();
                thisJobMap.put(DispatchLogEnum.jobstate.getValue(), NifiStageTypeEnum.RUNNING.getName());
                thisJobMap.put(DispatchLogEnum.jobstart.getValue(), simpleDateFormat.format(new Date()));
                iPipelJobLog.savePipelLogAndJobLog(pipelTraceId, thisJobMap, split[3], thisPipelJobTraceId, String.valueOf(itselfPort.pid));
                //4.记录这个task开始
                Map<Integer, Object> thisTaskMap = new HashMap<>();
                thisTaskMap.put(DispatchLogEnum.taskstate.getValue(), NifiStageTypeEnum.RUNNING.getName());
                thisTaskMap.put(DispatchLogEnum.taskstart.getValue(), simpleDateFormat.format(new Date()));
                iPipelTaskLog.savePipelTaskLog(thisPipelJobTraceId, thisPipelTaskTraceId, thisTaskMap, String.valueOf(itselfPort.id), split[6], Integer.parseInt(split[4]));
            } else {
                //4.记录这个task开始
                Map<Integer, Object> thisTaskMap = new HashMap<>();
                thisTaskMap.put(DispatchLogEnum.taskstate.getValue(), NifiStageTypeEnum.RUNNING.getName());
                thisTaskMap.put(DispatchLogEnum.taskstart.getValue(), simpleDateFormat.format(new Date()));
                iPipelTaskLog.savePipelTaskLog(upPipelJobTraceId, thisPipelTaskTraceId, thisTaskMap, String.valueOf(itselfPort.id), split[6], Integer.parseInt(split[4]));
                thisPipelJobTraceId = upPipelJobTraceId;
            }


            //此时,expiredKey就是即将要调用的节点,需要发消息topic_name就是expiredKey

            String tableType = split[4];
            int type = Integer.parseInt(tableType);
            if (Objects.equals(type, OlapTableEnum.PHYSICS_API.getValue())) {
                // todo 这里有问题
                ApiImportDataDTO apiImportDataDTO = new ApiImportDataDTO();
                apiImportDataDTO.workflowId = split[3];
                apiImportDataDTO.appId = Long.parseLong(split[5]);
                apiImportDataDTO.apiId = Long.parseLong(split[6]);
                apiImportDataDTO.pipelTraceId = pipelTraceId;
                apiImportDataDTO.pipelJobTraceId = thisPipelJobTraceId;
                apiImportDataDTO.pipelTaskTraceId = thisPipelTaskTraceId;
                apiImportDataDTO.pipelStageTraceId = thisPipelStageTraceId;
                PipelApiDispatchDTO pipelApiDispatchDTO = new PipelApiDispatchDTO();
                pipelApiDispatchDTO.apiId = Long.parseLong(split[6]);
                pipelApiDispatchDTO.appId = Long.parseLong(split[5]);
                pipelApiDispatchDTO.workflowId = split[3];
                apiImportDataDTO.pipelApiDispatch = JSON.toJSONString(pipelApiDispatchDTO);
                dataAccessClient.importData(apiImportDataDTO);
            } else {
                KafkaReceiveDTO kafkaReceiveDTO = new KafkaReceiveDTO();
                kafkaReceiveDTO.pipelTraceId = pipelTraceId;
                kafkaReceiveDTO.pipelJobTraceId = thisPipelJobTraceId;
                kafkaReceiveDTO.pipelTaskTraceId = thisPipelTaskTraceId;
                kafkaReceiveDTO.pipelStageTraceId = thisPipelStageTraceId;
                kafkaReceiveDTO.fidata_batch_code = UUID.randomUUID().toString();
                kafkaReceiveDTO.start_time = simpleDateFormat.format(new Date());
                log.info("发送的topic4:{},内容:{}", split1[0], JSON.toJSONString(kafkaReceiveDTO));
                kafkaTemplateHelper.sendMessageAsync(split1[0], JSON.toJSONString(kafkaReceiveDTO));
            }
        }
        if (expiredKey.startsWith("fiskgd")) {
            //整个管道记录结束
            String pipelTraceId = expiredKey.substring(7);
            String pipelId = "";
            List<PipelJobLogPO> list = iPipelJobLog.query().eq("pipel_trace_id", pipelTraceId).eq("del_flag", 1)
                    .isNotNull("pipel_id").orderByDesc("create_time").list();
            if (CollectionUtils.isNotEmpty(list)) {
                pipelId = list.get(0).pipelId;
                ResultEntity<List<NifiCustomWorkflowDetailDTO>> nifiPortTaskLastListById = dataFactoryClient.getNifiPortTaskLastListById(Long.valueOf(pipelId));
                List<NifiCustomWorkflowDetailDTO> data = nifiPortTaskLastListById.data;
                for (NifiCustomWorkflowDetailDTO dto : data) {
                    String taskId = String.valueOf(dto.id);
                    PipelJobLogPO byPipelTraceId = iPipelJobLog.getByPipelTraceId(pipelTraceId, dto.pid);
                    PipelTaskLogPO byPipelJobTraceId = iPipelTaskLog.getByPipelJobTraceId(byPipelTraceId.jobTraceId, dto.id);
                    //记录task结束
                    Map<Integer, Object> taskMap = new HashMap<>();
                    taskMap.put(DispatchLogEnum.taskstate.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());
                    taskMap.put(DispatchLogEnum.taskend.getValue(), simpleDateFormat.format(new Date()));
                    iPipelTaskLog.savePipelTaskLog(byPipelJobTraceId.jobTraceId, byPipelJobTraceId.taskTraceId, taskMap, byPipelJobTraceId.taskId, null, 0);
                    //记录job结束
                    Map<Integer, Object> upJobMap = new HashMap<>();
                    upJobMap.put(DispatchLogEnum.jobstate.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());
                    upJobMap.put(DispatchLogEnum.jobend.getValue(), simpleDateFormat.format(new Date()));
                    iPipelJobLog.savePipelLogAndJobLog(pipelTraceId, upJobMap, byPipelTraceId.pipelId, byPipelJobTraceId.jobTraceId, byPipelTraceId.componentId);
                }
            }
            //记录管道结束
            Map<Integer, Object> PipelMap = new HashMap<>();
            PipelMap.put(DispatchLogEnum.pipelend.getValue(), simpleDateFormat.format(new Date()));
            PipelMap.put(DispatchLogEnum.pipelstate.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());
            iPipelJobLog.savePipelLogAndJobLog(pipelTraceId, PipelMap, pipelId, null, null);

        } else if (expiredKey.startsWith("hand")) {
            //手动调度记录结束
            String[] split = expiredKey.split(",");
            String taskTraceId = split[0].substring(4);
            String[] split1 = split[1].split("\\.");

            HashMap<Integer, Object> taskMap = new HashMap<>();
            taskMap.put(DispatchLogEnum.taskstate.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName());
            taskMap.put(DispatchLogEnum.taskend.getValue(), simpleDateFormat.format(new Date()));
            iPipelTaskLog.savePipelTaskLog(null, taskTraceId, taskMap, null, split1[5], Integer.parseInt(split1[3]));
        }
    }
}