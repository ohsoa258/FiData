package com.fisk.task.consumer.nifi;

import com.alibaba.fastjson.JSON;
import com.fisk.common.aop.rabbitmq.MQConsumerLog;
import com.fisk.common.constants.MQConstants;
import com.fisk.task.dto.BuildNifiFlowDTO;
import com.fisk.task.utils.WsSessionManager;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author gy
 */
@Component
@RabbitListener(queues = MQConstants.QueueConstants.BUILD_NIFI_FLOW)
@Slf4j
public class BuildNifiTaskListener {

    @MQConsumerLog
    @RabbitHandler
    public void msg(String data, Channel channel, Message message) {
        BuildNifiFlowDTO dto = JSON.parseObject(data, BuildNifiFlowDTO.class);
        WsSessionManager.sendMsgById("---------数据流开始创建---------", dto.userId);
        try {
            WsSessionManager.sendMsgById("---------数据流创建完成---------", dto.userId);
        } catch (Exception ex) {
            WsSessionManager.sendMsgById("---------数据流创建失败---------", dto.userId);
        }
    }

}
