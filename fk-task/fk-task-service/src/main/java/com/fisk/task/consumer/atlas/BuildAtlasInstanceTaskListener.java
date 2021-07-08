package com.fisk.task.consumer.atlas;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.task.dto.atlas.AtlasEntityRdbmsDTO;
import com.fisk.task.service.IAtlasBuildInstance;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/7 15:57
 * Description:
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_ATLAS_INSTANCE_FLOW)
@Slf4j
public class BuildAtlasInstanceTaskListener {

    @Resource
    IAtlasBuildInstance atlas;
    public void msg(String dataInfo, Channel channel, Message message){
        AtlasEntityRdbmsDTO ad=JSON.parseObject(dataInfo, AtlasEntityRdbmsDTO.class);
        atlas.atlasBuildInstance(ad.entityInstance);
        atlas.atlasBuildDb(ad.entityDb);
        atlas.atlasBuildProcess(ad.entityProcess);
    }
}
