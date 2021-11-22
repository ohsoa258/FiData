package com.fisk.task.config;

import com.fisk.common.constants.MqConstants;
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
        return ExchangeBuilder.topicExchange(MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME).durable(true).build();
    }

    /**
     * 声明队列
     */
    @Bean("itemQueue")
    public Queue itemQueue() {
        return QueueBuilder.durable(MqConstants.QueueConstants.BUILD_NIFI_FLOW).build();
    }

    /**
     * 声明队列
     */
    @Bean("atlasInstanceQueue")
    public Queue atlasInstanceQueue() {
        return QueueBuilder.durable(MqConstants.QueueConstants.BUILD_ATLAS_INSTANCE_FLOW).build();
    }

    /**
     * 声明队列
     */
    @Bean("atlasTableColumnQueue")
    public Queue atlasTableColumnQueue() {
        return QueueBuilder.durable(MqConstants.QueueConstants.BUILD_ATLAS_TABLECOLUMN_FLOW).build();
    }

    /**
     * 声明队列
     */
    @Bean("atlasEntityDeleteQueue")
    public Queue atlasEntityDeleteQueue() {
        return QueueBuilder.durable(MqConstants.QueueConstants.BUILD_ATLAS_ENTITYDELETE_FLOW).build();
    }

    /**
     * 声明队列
     */
    @Bean("dorisBuildTableQueue")
    public Queue dorisBuildTableQueue() {
        return QueueBuilder.durable(MqConstants.QueueConstants.BUILD_DORIS_FLOW).build();
    }

    /**
     * 声明队列
     */
    @Bean("dorisBuildDataModelTableQueue")
    public Queue dorisBuildDataModelTableQueue() {
        return QueueBuilder.durable(MqConstants.QueueConstants.BUILD_DATAMODEL_DORIS_TABLE).build();
    }

    /**
     * 声明队列
     */
    @Bean("incrementResultQueue")
    public Queue incrementResultQueue() {
        return QueueBuilder.durable(MqConstants.QueueConstants.INCREMENT_RESULT).build();
    }

    /**
     * 声明队列
     */
    @Bean("incrementDorisFlowQueue")
    public Queue incrementDorisFlowQueue() {
        return QueueBuilder.durable(MqConstants.QueueConstants.BUILD_DORIS_INCREMENTAL_FLOW).build();
    }
    /**
     * 声明队列
     */
    @Bean("datainputPgBuildTableQueue")
    public Queue datainputPgBuildTableQueue() {
        return QueueBuilder.durable(MqConstants.QueueConstants.BUILD_DATAINPUT_PGSQL_TABLE_FLOW).build();
    }
    /**
     * 声明队列
     */
    @Bean("datainputPgStgToOdsQueue")
    public Queue datainputPgStgToOdsQueue() {
        return QueueBuilder.durable(MqConstants.QueueConstants.BUILD_DATAINPUT_PGSQL_STGTOODS_FLOW).build();
    }

    /**
     * 声明队列
     */
    @Bean("datainputDeletePgsqlTableQueue")
    public Queue datainputDeletePgsqlTableQueue() {
        return QueueBuilder.durable(MqConstants.QueueConstants.BUILD_DATAINPUT_DELETE_PGSQL_TABLE_FLOW).build();
    }

    /**
     * 声明队列
     */
    @Bean("buildOlapCreatemodelQueue")
    public Queue buildOlapCreatemodelQueue() {
        return QueueBuilder.durable(MqConstants.QueueConstants.BUILD_OLAP_CREATEMODEL_FLOW).build();
    }

    /*
    * 声明队列
    * */
    @Bean("buildCustomWorkFlowQueue")
    public Queue buildNifiCustomWorkFlowQueue() {
        return QueueBuilder.durable(MqConstants.QueueConstants.BUILD_CUSTOMWORK_FLOW).build();
    }

    /*
    * 绑定队列和交换机
    * */
    @Bean
    public Binding buildNifiCustomWorkFlowExchange(@Qualifier("buildCustomWorkFlowQueue") Queue queue,
                                                @Qualifier("itemTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MqConstants.RouterConstants.TASK_BUILD_CUSTOMWORK_ROUTER).noargs();
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding buildOlapCreatemodelExchange(@Qualifier("buildOlapCreatemodelQueue") Queue queue,
                                     @Qualifier("itemTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MqConstants.RouterConstants.TASK_BUILD_OLAP_CREATEMODEL_ROUTER).noargs();
    }
    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding itemQueueExchange(@Qualifier("itemQueue") Queue queue,
                                     @Qualifier("itemTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MqConstants.RouterConstants.TASK_BUILD_NIFI_ROUTER).noargs();
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding atlasInstanceQueueExchange(@Qualifier("atlasInstanceQueue") Queue queue,
                                     @Qualifier("itemTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MqConstants.RouterConstants.TASK_BUILD_ATLAS_INSTANCE_ROUTER).noargs();
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding atlasTableColumnQueueExchange(@Qualifier("atlasTableColumnQueue") Queue queue,
                                      @Qualifier("itemTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MqConstants.RouterConstants.TASK_BUILD_ATLAS_TABLECOLUMN_ROUTER).noargs();
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding atlasEntityDeleteQueueExchange(@Qualifier("atlasEntityDeleteQueue") Queue queue,
                                                  @Qualifier("itemTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MqConstants.RouterConstants.TASK_BUILD_ATLAS_ENTITYDELETE_ROUTER).noargs();
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding dorisBuildTableQueueExchange(@Qualifier("dorisBuildTableQueue") Queue queue,
                                                @Qualifier("itemTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MqConstants.RouterConstants.TASK_BUILD_DORIS_ROUTER).noargs();
    }
    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding dorisBuildDataModelTableQueueExchange(@Qualifier("dorisBuildDataModelTableQueue") Queue queue,
                                                @Qualifier("itemTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MqConstants.RouterConstants.TASK_BUILD_DATAMODEL_DORIS_TABLE_ROUTER).noargs();
    }
    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding incrementResultQueueExchange(@Qualifier("incrementResultQueue") Queue queue,
                                                @Qualifier("itemTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MqConstants.RouterConstants.INCREMENT_RESULT).noargs();
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding incrementDorisFlowQueueExchange(@Qualifier("incrementDorisFlowQueue") Queue queue,
                                                @Qualifier("itemTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MqConstants.RouterConstants.TASK_BUILD_DORIS_INCREMENTAL_ROUTER).noargs();
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding datainputPgBuildTableQueueExchange(@Qualifier("datainputPgBuildTableQueue") Queue queue,
                                                   @Qualifier("itemTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MqConstants.RouterConstants.TASK_BUILD_DATAINPUT_PGSQL_TABLE_ROUTER).noargs();
    }
    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding datainputDeletePgsqlTableQueueExchange(@Qualifier("datainputDeletePgsqlTableQueue") Queue queue,
                                                      @Qualifier("itemTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MqConstants.RouterConstants.TASK_BUILD_DATAINPUT_DELETE_PGSQL_TABLE_ROUTER).noargs();
    }
    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding datainputPgStgToOdsQueueExchange(@Qualifier("datainputPgStgToOdsQueue") Queue queue,
                                                      @Qualifier("itemTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MqConstants.RouterConstants.TASK_BUILD_DATAINPUT_PGSQL_STGTOODS_ROUTER).noargs();
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
