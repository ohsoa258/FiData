package com.fisk.task.consumer.atlas;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.task.dto.atlas.ReceiveDataConfigDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.IAtlasBuild;
import com.fisk.task.service.IBuildTaskService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @DennyHui
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_ATLAS_FLOW)
@Slf4j

public class BuildAtlasTaskListener {

    @Resource
    IBuildTaskService service;
    IAtlasBuild atlas;

    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.ATLAS_MQ_BUILD)
    public void msg(String tableSchema, Channel channel, Message message) {
        ReceiveDataConfigDTO dto = JSON.parseObject(tableSchema, ReceiveDataConfigDTO.class);
        StringBuilder sql = new StringBuilder();
        String tableName="denny_table";
        String stg_table="stg_"+tableName;
        String ods_table="ods"+tableName;
        sql.append("CREATE TABLE tableName");
        sql.append("{");
        sql.append("id INT DEFAULT '10',");
        sql.append("username VARCHAR(32) DEFAULT '',");
        sql.append("citycode SMALLINT,");
        sql.append("}");
        sql.append("AGGREGATE KEY(id, citycode, username)");
        sql.append("DISTRIBUTED BY HASH(siteid) BUCKETS 10");
        sql.append("\n" + "PROPERTIES(\"replication_num\" = \"1\");");
        int result = 0;
        String stg_sql=sql.toString().replace("tableName",stg_table);
        String ods_sql=sql.toString().replace("tableName",ods_table);
        BusinessResult sqlresult_stg= atlas.dorisBuildTable(stg_sql);
        BusinessResult sqlresult_ods= atlas.dorisBuildTable(ods_sql);
        System.out.println(JSON.toJSONString(sqlresult_stg));
        System.out.println(JSON.toJSONString(sqlresult_ods));
        //BuildNifiFlowDTO bb = new BuildNifiFlowDTO();
 /*       bb.appId = 123L;
        service.publishTask(TaskTypeEnum.BUILD_NIFI_FLOW.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_NIFI_FLOW,
                bb);*/
    }


}
