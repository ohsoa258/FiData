package com.fisk.task.consumer.postgre.datainput;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.IPostgreBuild;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author: DennyHui
 * CreateTime: 2021/8/27 12:57
 * Description:
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_DATAINPUT_PGSQL_TABLE_FLOW)
@Slf4j
public class BuildDataInputPgTableListener {
    @Resource
    IPostgreBuild pg;
    @Resource
    DataAccessClient dc;

    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.DATAINPUT_PG_TABLE_BUILD)
    public void msg(String dataInfo, Channel channel, Message message) {
        log.info("执行pg build table");
        log.info("dataInfo:" + dataInfo);
        AtlasEntityQueryDTO inpData = JSON.parseObject(dataInfo, AtlasEntityQueryDTO.class);
        ResultEntity<AtlasEntityDbTableColumnDTO> queryRes = dc.getAtlasBuildTableAndColumn(Long.parseLong(inpData.dbId), Long.parseLong(inpData.appId));
        log.info("queryRes:" + JSON.toJSONString(queryRes));
        AtlasEntityDbTableColumnDTO dto = JSON.parseObject(JSON.toJSONString(queryRes.data), AtlasEntityDbTableColumnDTO.class);
        log.info("ae:" + JSON.toJSONString(dto));
        String tableName = dto.tableName;
        String stg_table = dto.appAbbreviation + "_stg_" + dto.tableName;
        String ods_table = dto.appAbbreviation + "_ods_" + dto.tableName;
        StringBuilder sql = new StringBuilder();
    }
}
