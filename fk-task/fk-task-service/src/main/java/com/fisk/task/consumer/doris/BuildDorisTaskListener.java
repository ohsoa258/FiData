package com.fisk.task.consumer.doris;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.IBuildTaskService;
import com.fisk.task.service.IDorisBuild;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author:yhxu CreateTime: 2021年07月05日18:33:46
 * Description:
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_DORIS_FLOW)
@Slf4j

public class BuildDorisTaskListener {

    @Resource
    IBuildTaskService service;
    @Resource
    IDorisBuild doris;
    @Resource
    DataAccessClient dc;


    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.DORIS_MQ_BUILD)
    public void msg(String dataInfo, Channel channel, Message message) {
        log.info("执行Doris");
        log.info("dataInfo:" + dataInfo);
        AtlasEntityQueryDTO inpData = JSON.parseObject(dataInfo, AtlasEntityQueryDTO.class);
        ResultEntity<AtlasEntityDbTableColumnDTO> queryRes = dc.getAtlasBuildTableAndColumn(Long.parseLong(inpData.dbId), Long.parseLong(inpData.appId));
        log.info("queryRes:" + JSON.toJSONString(queryRes.data));
        log.info("queryRes:" + JSON.toJSONString(queryRes));
        AtlasEntityDbTableColumnDTO dto = JSON.parseObject(JSON.toJSONString(queryRes.data), AtlasEntityDbTableColumnDTO.class);
        log.info("ae:" + JSON.toJSONString(dto));
        StringBuilder sql = new StringBuilder();
        String tableName = dto.tableName;
        String stg_table = "stg_" + dto.tableName;
        String ods_table = "ods_" + dto.tableName;
        sql.append("CREATE TABLE tableName");
        sql.append("(");
        StringBuilder sqlFileds = new StringBuilder();
        StringBuilder sqlAggregate = new StringBuilder("AGGREGATE KEY(");
        StringBuilder sqlSelectStrBuild = new StringBuilder();
        StringBuilder sqlDistributed = new StringBuilder("DISTRIBUTED BY HASH(");
        dto.columns.forEach((l) -> {
            //是否是主键
            if (l.isKey.equals("1")) {
                sqlDistributed.append(l.columnName);
            }
            sqlFileds.append(l.columnName + " " + l.dataType + " comment " + "'" + l.comment + "' ,");
            sqlAggregate.append(l.columnName + ",");
            sqlSelectStrBuild.append(l.columnName + ",");
        });
        sqlDistributed.append(") BUCKETS 10");
        String aggregateStr = sqlAggregate.toString();
        aggregateStr = aggregateStr.substring(0, aggregateStr.lastIndexOf(",")) + ")";
        String selectStr = sqlSelectStrBuild.toString();
        selectStr = selectStr.substring(0, selectStr.lastIndexOf(",")) + ")";
        String filedStr = sqlFileds.toString();
        sql.append(filedStr.substring(0, filedStr.lastIndexOf(",")));
        String sqlSelectStr = "select " + selectStr + " from " + dto.tableName;
        sql.append(")");
        sql.append(aggregateStr);
        sql.append(sqlDistributed.toString());
        sql.append("\n" + "PROPERTIES(\"replication_num\" = \"1\");");
        String stg_sql = sql.toString().replace("tableName", stg_table);
        String ods_sql = sql.toString().replace("tableName", ods_table);
        doris.dorisBuildTable(stg_sql);
        doris.dorisBuildTable(ods_sql);
        log.info("【STG】" + stg_sql);
        log.info("【ODS】" + ods_sql);
        log.info("Doris执行结束");
    }


}
