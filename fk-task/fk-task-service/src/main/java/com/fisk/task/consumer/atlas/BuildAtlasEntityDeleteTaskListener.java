package com.fisk.task.consumer.atlas;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.task.dto.atlas.AtlasEntityDeleteDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.IAtlasBuildInstance;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/12 18:03
 * Description:
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_ATLAS_ENTITYDELETE_FLOW)
@Slf4j
public class BuildAtlasEntityDeleteTaskListener {
    @Resource
    IAtlasBuildInstance atlas;

    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.ATLASENTITYDELETE_MQ_BUILD)
    public void msg(String dataInfo, Channel channel, Message message) {
        AtlasEntityDeleteDTO ad= JSON.parseObject(dataInfo, AtlasEntityDeleteDTO.class);
    }
}
