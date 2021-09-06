package com.fisk.task.consumer.olap;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.BusinessAreaGetDataDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.dto.olap.BuildCreateModelTaskDto;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.IOlap;
import com.rabbitmq.client.Channel;
import jdk.nashorn.internal.parser.JSONParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: 创建模型
 *
 * @author JinXingWang
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_OLAP_CREATEMODEL_FLOW)
@Slf4j
public class BuildModelTaskListener {

    @Resource
    DataModelClient client;
    @Resource
    IOlap olap;

    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.OLAP_CREATEMODEL_BUILD)
    public void msg(String dataInfo, Channel channel, Message message) {
        BuildCreateModelTaskDto inpData = JSON.parseObject(dataInfo, BuildCreateModelTaskDto.class);
        ResultEntity<BusinessAreaGetDataDTO> data = client.getBusinessAreaPublicData(inpData.businessAreaId);
        if (data.code == 0) {
            olap.build(inpData.businessAreaId, data.data);
        }
    }
}
