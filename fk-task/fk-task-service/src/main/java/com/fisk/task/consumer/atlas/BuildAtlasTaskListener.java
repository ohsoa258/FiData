package com.fisk.task.consumer.atlas;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MQConstants;
import com.fisk.task.dto.BuildNifiFlowDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.utils.WsSessionManager;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

public class BuildAtlasTaskListener {
    @RabbitHandler
    @MQConsumerLog
    public void msg(String settingid, Channel channel, Message message) {
        BuildNifiFlowDTO dto = JSON.parseObject(settingid, BuildNifiFlowDTO.class);
        WsSessionManager.sendMsgById("---------数据流开始创建---------", dto.userId);
        try {
            WsSessionManager.sendMsgById("---------数据流创建完成---------", dto.userId);
        } catch (Exception ex) {
            WsSessionManager.sendMsgById("---------数据流创建失败---------", dto.userId);
        }
    }
}
