package com.fisk.task.consumer.atlas;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MQConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.TaskTypeEnum;
import com.fisk.task.dto.atlas.ReceiveDataConfigDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.IAtlasBuild;
import com.fisk.task.service.IBuildTaskService;
import com.fisk.task.utils.DorisHelper;
import com.fisk.task.utils.WsSessionManager;
import com.fisk.task.utils.YamlReader;
import com.rabbitmq.client.Channel;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * @DennyHui
 */
@Component
@RabbitListener(queues = MQConstants.QueueConstants.BUILD_ATLAS_FLOW)
@Slf4j

public class BuildAtlasTaskListener {

    @Resource
    IBuildTaskService service;
    IAtlasBuild atlas;

    @RabbitHandler
    @MQConsumerLog
    public void msg(String settingid, Channel channel, Message message) {
        ReceiveDataConfigDTO dto = JSON.parseObject(settingid, ReceiveDataConfigDTO.class);
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE denny_table");
        sql.append("{");
        sql.append("id INT DEFAULT '10',");
        sql.append("username VARCHAR(32) DEFAULT '',");
        sql.append("citycode SMALLINT,");
        sql.append("}");
        sql.append("AGGREGATE KEY(id, citycode, username)");
        sql.append("DISTRIBUTED BY HASH(siteid) BUCKETS 10");
        sql.append("\n" + "PROPERTIES(\"replication_num\" = \"1\");");
        int result = 0;
       BusinessResult sqlresult= atlas.dorisBuildTable(sql.toString());
        System.out.println(JSON.toJSONString(sqlresult));
        //BuildNifiFlowDTO bb = new BuildNifiFlowDTO();
 /*       bb.appId = 123L;
        service.publishTask(TaskTypeEnum.BUILD_NIFI_FLOW.getName(),
                MQConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MQConstants.QueueConstants.BUILD_NIFI_FLOW,
                bb);*/
    }


}
