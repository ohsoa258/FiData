package com.fisk.task.consumer.postgre.datainput;
import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.enums.task.BusinessTypeEnum;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.IPostgreBuild;
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
 * CreateTime: 2021/8/27 12:57
 * Description:在pgsql库中创建表
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
        ResultEntity<BuildPhysicalTableDTO> data = dc.getBuildPhysicalTableDTO(Long.parseLong(inpData.dbId), Long.parseLong(inpData.appId));
        BuildPhysicalTableDTO buildPhysicalTableDTO = data.data;
        StringBuilder sql = new StringBuilder();
        StringBuilder sqlFileds = new StringBuilder();
        sql.append("CREATE TABLE tableName ( " + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "_pk" + " varchar(50) NOT NULL DEFAULT sys_guid() PRIMARY KEY,fk_doris_increment_code varchar(50),");
        List<TableFieldsDTO> tableFieldsDTOS = buildPhysicalTableDTO.tableFieldsDTOS;
        tableFieldsDTOS.forEach((l) -> {
            sqlFileds.append(l.fieldName + " " + l.fieldType.toLowerCase() + "(" + l.fieldLength + ") ,");
        });
        sqlFileds.delete(sqlFileds.length() - 1, sqlFileds.length());
        sqlFileds.append(")");
        sql.append(sqlFileds);
        String stg_sql1 = sql.toString().replace("tableName", "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
        String stg_sql2 = sql.toString().replace("tableName", "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
        pg.postgreBuildTable(stg_sql1, BusinessTypeEnum.DATAINPUT);
        pg.postgreBuildTable(stg_sql2, BusinessTypeEnum.DATAINPUT);
        log.info("【PGSTG】" + stg_sql1);
        log.info("pg：建表完成");
    }
}
