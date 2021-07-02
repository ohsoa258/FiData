package com.fisk.task.config;

import com.fisk.common.constants.MQConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gy
 */
@Configuration
@Slf4j
public class RabbitMQConfig {

    /**
     * 声明交换机
     */
    @Bean("itemTopicExchange")
    public Exchange topicExchange() {
        return ExchangeBuilder.topicExchange(MQConstants.ExchangeConstants.TASK_EXCHANGE_NAME).durable(true).build();
    }

    /**
     * 声明队列
     */
    @Bean("itemQueue")
    public Queue itemQueue() {
        return QueueBuilder.durable(MQConstants.QueueConstants.BUILD_NIFI_FLOW).build();
    }

    /**
     * 声明队列
     */
    @Bean("atlasQueue")
    public Queue atlasQueue() {
        return QueueBuilder.durable(MQConstants.QueueConstants.BUILD_ATLAS_FLOW).build();
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding itemQueueExchange(@Qualifier("itemQueue") Queue queue,
                                     @Qualifier("itemTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MQConstants.RouterConstants.TASK_BUILD_NIFI_ROUTER).noargs();
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding atlasQueueExchange(@Qualifier("atlasQueue") Queue queue,
                                     @Qualifier("itemTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MQConstants.RouterConstants.TASK_BUILD_ATLAS_ROUTER).noargs();
    }

    @Bean
    public RabbitTemplate createRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        //设置开启Mandatory,才能触发回调函数,无论消息推送结果怎么样都强制调用回调函数
        rabbitTemplate.setMandatory(true);

        return rabbitTemplate;
    }
}
