package com.fisk.task.consumer.atlas;

import com.fisk.common.constants.MQConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/7 15:57
 * Description:
 */
@Component
@RabbitListener(queues = MQConstants.QueueConstants.BUILD_ATLAS_FLOW)
@Slf4j
public class BuildAtlasTaskListener {

}
