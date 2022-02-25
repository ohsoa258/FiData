package com.fisk.task.config;

import com.alibaba.fastjson.JSON;
import com.fisk.common.mdc.MDCHelper;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author gy
 */

@Slf4j
public class MQProducerAckConfig{


}
