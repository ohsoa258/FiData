package com.fisk.task.consumer.postgre.datainput;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.enums.task.BusinessTypeEnum;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.mapper.TaskPgTableStructureMapper;
import com.fisk.task.service.atlas.IAtlasBuildInstance;
import com.fisk.task.service.doris.IDorisBuild;
import com.fisk.task.utils.PostgreHelper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author: DennyHui
 * CreateTime: 2021/9/15 10:40
 * Description: 删除pgsql表，在数据接入删除应用和删除指定的物理表的时候触发。
 */
@Component
@Slf4j
public class BuildDataInputDeletePgTableListener {
    @Resource
    IAtlasBuildInstance atlas;
    @Resource
    IDorisBuild doris;
    @Resource
    TaskPgTableStructureMapper taskPgTableStructureMapper;

    //@KafkaListener(topics = MqConstants.QueueConstants.BUILD_DATAINPUT_DELETE_PGSQL_TABLE_FLOW, containerFactory = "batchFactory", groupId = "test")
    //@MQConsumerLog(type = TraceTypeEnum.DATAINPUT_PG_TABLE_DELETE)
    public void msg(String dataInfo, Acknowledgment acke) {
        log.info("执行pg delete table");
        log.info("dataInfo:" + dataInfo);
        StringBuilder buildDelSqlStr=new StringBuilder("DROP TABLE IF EXISTS ");
        PgsqlDelTableDTO inputData= JSON.parseObject(dataInfo,PgsqlDelTableDTO.class);
        HashMap<String, Object> conditionHashMap = new HashMap<>();
        if(Objects.equals(inputData.businessTypeEnum,BusinessTypeEnum.DATAINPUT)){
            List<String> atlasEntityId=new ArrayList();;
            inputData.tableList.forEach((t)->{
                buildDelSqlStr.append("stg_"+t.tableName+",ods_"+t.tableName+", ");
                atlasEntityId.add(t.tableAtlasId);
                conditionHashMap.put("table_name","stg_"+t.tableName);
                taskPgTableStructureMapper.deleteByMap(conditionHashMap);
                conditionHashMap.put("table_name","ods_"+t.tableName);
                taskPgTableStructureMapper.deleteByMap(conditionHashMap);
            });
            String delSqlStr=buildDelSqlStr.toString();
            delSqlStr=delSqlStr.substring(0,delSqlStr.lastIndexOf(","))+" ;";
            PostgreHelper.postgreExecuteSql(delSqlStr,BusinessTypeEnum.DATAINPUT);
            log.info("delsql:"+delSqlStr);
            log.info("执行pg delete table 完成");
//        log.info("开始删除atals实例");
            atlasEntityId.forEach((a)->{
                //AtlasEntityDeleteDTO ad= JSON.parseObject(a, AtlasEntityDeleteDTO.class);
                //BusinessResult resDel=atlas.atlasEntityDelete(ad);
            });
//        log.info("Atlas实例删除完成");
        }else{
            inputData.tableList.forEach((t)->{
                buildDelSqlStr.append(t.tableName+", ");
                conditionHashMap.put("table_name",t.tableName);
                taskPgTableStructureMapper.deleteByMap(conditionHashMap);
            });
            String delSqlStr=buildDelSqlStr.toString();
            delSqlStr=delSqlStr.substring(0,delSqlStr.lastIndexOf(","))+" ;";
            PostgreHelper.postgreExecuteSql(delSqlStr,BusinessTypeEnum.DATAMODEL);
            doris.dorisBuildTable(delSqlStr);
            log.info("delsql:"+delSqlStr);
            log.info("执行pg delete table 完成");
        }
        acke.acknowledge();
    }
}
