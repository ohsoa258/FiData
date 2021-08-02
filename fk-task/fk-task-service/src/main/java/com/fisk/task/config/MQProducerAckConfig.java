package com.fisk.task.config;

import com.alibaba.fastjson.JSON;
import com.fisk.common.mdc.MDCHelper;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author gy
 */
@Component
@Slf4j
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        //指定 ConfirmCallback
        rabbitTemplate.setConfirmCallback(this);
        //指定 ReturnCallback
        rabbitTemplate.setReturnCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        MDCHelper.setAppLogType(TraceTypeEnum.TASK_MQ_PRODUCER_CONFIRM);
        MDCHelper.setClass(RabbitMQConfig.class.getName());
        MDCHelper.setFunction("ConfirmCallback");
        log.info("【{}】相关数据: {}. 确认情况: {}. 原因: {}.", DateTimeUtils.getNow(), correlationData, ack, cause);
    }

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        MDCHelper.setAppLogType(TraceTypeEnum.TASK_MQ_PRODUCER_CONFIRM);
        MDCHelper.setClass(RabbitMQConfig.class.getName());
        MDCHelper.setFunction("ReturnCallback");
        log.info("【{}】消息: {}. 回应码: {}. 回应信息: {}. 交换机: {}. 路由键: {}.", DateTimeUtils.getNow(), JSON.toJSONString(message.getBody()), replyCode, replyText, exchange, routingKey);
    }
}
