package com.fisk.task.consumer.atlas;
import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MQConstants;
import com.fisk.task.dto.atlas.ReceiveDataConfigDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.utils.WsSessionManager;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @
 */
@Component
@RabbitListener(queues = MQConstants.QueueConstants.BUILD_ATLAS_FLOW)
@Slf4j

public class BuildAtlasTaskListener {
    @RabbitHandler
    @MQConsumerLog
    public void msg(String settingid, Channel channel, Message message) {
        ReceiveDataConfigDTO dto = JSON.parseObject(settingid, ReceiveDataConfigDTO.class);

    }
}
