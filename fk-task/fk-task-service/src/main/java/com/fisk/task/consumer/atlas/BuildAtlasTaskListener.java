package com.fisk.task.consumer.atlas;
import com.alibaba.fastjson.JSON;
import com.fisk.task.dto.atlas.ReceiveDataConfigDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.utils.WsSessionManager;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;

/**
 * @author Denny 
 */
public class BuildAtlasTaskListener {
    @RabbitHandler
    @MQConsumerLog
    public void msg(String settingid, Channel channel, Message message) {
        ReceiveDataConfigDTO dto = JSON.parseObject(settingid, ReceiveDataConfigDTO.class);
        
    }
}
