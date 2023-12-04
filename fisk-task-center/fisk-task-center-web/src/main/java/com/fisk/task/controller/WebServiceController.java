package com.fisk.task.controller;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.consumeserveice.client.ConsumeServeiceClient;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.BuildTableApiServiceDTO;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.utils.KafkaTemplateHelper;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ws")
@Slf4j
public class WebServiceController {

    @Resource
    private ConsumeServeiceClient consumeServeiceClient;
    @Resource
    private KafkaTemplateHelper kafkaTemplateHelper;

    /**
     * 前置机-数据接入发送消息到数据分发
     *
     * @param apiConfigId
     */
    @ApiOperation("前置机-数据接入发送消息到数据分发")
    @PostMapping("/wsAccessToConsume")
    public void wsAccessToConsume(Integer apiConfigId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        // 通过管道id,查询关联api服务
        ResultEntity<List<BuildTableApiServiceDTO>> tableListByInputId = consumeServeiceClient.getTableListByInputId(11);
        if (tableListByInputId != null && tableListByInputId.code == ResultEnum.SUCCESS.getCode() && !CollectionUtils.isEmpty(tableListByInputId.data)) {
            List<BuildTableApiServiceDTO> list = tableListByInputId.data;
            for (BuildTableApiServiceDTO buildTableApiService : list) {
                if (buildTableApiService.enable == 0) {
                    continue;
                }
                KafkaReceiveDTO kafkaRkeceive = KafkaReceiveDTO.builder().build();
                kafkaRkeceive.topic = MqConstants.TopicPrefix.TOPIC_PREFIX + OlapTableEnum.DATA_SERVICE_API.getValue() + "." + buildTableApiService.getAppId() + "." + buildTableApiService.id;
                kafkaRkeceive.start_time = simpleDateFormat.format(new Date());
                kafkaRkeceive.pipelTaskTraceId = UUID.randomUUID().toString();
                kafkaRkeceive.fidata_batch_code = UUID.randomUUID().toString();
                kafkaRkeceive.pipelStageTraceId = UUID.randomUUID().toString();
                kafkaRkeceive.ifTaskStart = true;
                kafkaRkeceive.topicType = TopicTypeEnum.DAILY_NIFI_FLOW.getValue();
                //pc.universalPublish(kafkaRkeceiveDTO);
                log.info("数据分发api关联触发流程参数:{}", JSON.toJSONString(kafkaRkeceive));
                kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_TASK_PUBLISH_FLOW, JSON.toJSONString(kafkaRkeceive));
            }
        }
    }

}
