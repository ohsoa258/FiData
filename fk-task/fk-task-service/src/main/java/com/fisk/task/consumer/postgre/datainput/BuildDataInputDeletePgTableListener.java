package com.fisk.task.consumer.postgre.datainput;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.BusinessTypeEnum;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.task.dto.atlas.AtlasEntityDeleteDTO;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.IAtlasBuildInstance;
import com.fisk.task.utils.PostgreHelper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: DennyHui
 * CreateTime: 2021/9/15 10:40
 * Description: 删除pgsql表，在数据接入删除应用和删除指定的物理表的时候触发。
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_DATAINPUT_DELETE_PGSQL_TABLE_FLOW)
@Slf4j
public class BuildDataInputDeletePgTableListener {
    @Resource
    IAtlasBuildInstance atlas;

    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.DATAINPUT_PG_TABLE_DELETE)
    public void msg(String dataInfo, Channel channel, Message message) {
        log.info("执行pg delete table");
        log.info("dataInfo:" + dataInfo);
        PgsqlDelTableDTO inputData= JSON.parseObject(dataInfo,PgsqlDelTableDTO.class);
        StringBuilder buildDelSqlStr=new StringBuilder("DROP TABLE");
        List<String> atlasEntityId=null;
        inputData.tableList.forEach((t)->{
            buildDelSqlStr.append(t.tableName+",");
            atlasEntityId.add(t.tableAtlasId);
        });
        String delSqlStr=buildDelSqlStr.toString();
        delSqlStr=delSqlStr.substring(0,delSqlStr.lastIndexOf(","))+";";
        PostgreHelper.postgreExecuteSql(delSqlStr,BusinessTypeEnum.DATAINPUT);
        log.info("执行pg delete table 完成");
        log.info("开始删除atals实例");
        atlasEntityId.forEach((a)->{
            AtlasEntityDeleteDTO ad= JSON.parseObject(a, AtlasEntityDeleteDTO.class);
            BusinessResult resDel=atlas.atlasEntityDelete(ad);
        });
        log.info("Atlas实例删除完成");
    }
}
