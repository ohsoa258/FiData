package com.fisk.task.consumer.postgre.datainput;
import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.enums.task.BusinessTypeEnum;
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
        StringBuilder pksql=new StringBuilder("PRIMARY KEY ( fk_table_pk,");
        StringBuilder comsql=new StringBuilder();
        sql.append("CREATE TABLE public.tableName");
        sql.append(" ( tableName_pk varchar(32) NOT NULL DEFAULT sys_guid(),");
        dto.columns.forEach((l) -> {
            if (l.isKey.equals("1")) {
                pksql.append(l.columnName+",");
            }
            sql.append(l.columnName + " " + l.dataType+",");
            comsql.append("COMMENT ON COLUMN public.tableName."+l.columnName+" IS '"+l.comment+"';");
        });
        String pksqlstr=pksql.toString();
        pksqlstr=pksqlstr.substring(0,pksqlstr.lastIndexOf(","))+")";
        String comsqlstr=comsql.toString();
        sql.append(" fk_doris_increment_code VARCHAR(50), ").append(pksqlstr).append(");").append(comsqlstr);
        String stg_sql = sql.toString().replace("tableName", stg_table);
        String ods_sql = sql.toString().replace("tableName", ods_table);
        log.info("pg：开始建表");
        log.info(stg_sql);
        log.info(ods_sql);
        pg.postgreBuildTable(stg_sql, BusinessTypeEnum.DATAINPUT);
        pg.postgreBuildTable(ods_sql, BusinessTypeEnum.DATAINPUT);
        log.info("pg：建表完成");
    }
}
