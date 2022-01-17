package com.fisk.task.consumer.postgre.datainput;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.enums.task.BusinessTypeEnum;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.dto.task.TableFieldDetailDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.IPostgreBuild;
import com.fisk.task.utils.TaskPgTableStructureHelper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.*;

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
    @Resource
    TaskPgTableStructureHelper taskPgTableStructureHelper;

    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.DATAINPUT_PG_TABLE_BUILD)
    public void msg(String dataInfo, Channel channel, Message message) {
        log.info("执行pg build table");
        log.info("dataInfo:" + dataInfo);
        BuildPhysicalTableDTO buildPhysicalTableDTO = JSON.parseObject(dataInfo, BuildPhysicalTableDTO.class);
        ModelPublishTableDTO dto = buildPhysicalTableDTO.modelPublishTableDTO;
        log.info("开始保存ods版本号,参数为{}", dto);
        // 保存ods版本号
        ResultEnum resultEnum = taskPgTableStructureHelper.saveTableStructure(dto);
/*
        dto.tableName = "stg_" + dto.tableName.substring(4);
        dto.createType = 4;
        log.info("开始保存stg版本号,参数为{}", dto);
        // 保存stg版本号
        taskPgTableStructureHelper.saveTableStructure(dto);
*/
        log.info("保存版本号方法执行成功");
        StringBuilder sql = new StringBuilder("CREATE TABLE tableName ( ");
        StringBuilder sqlFileds = new StringBuilder();
        StringBuilder pksql=new StringBuilder();
        StringBuilder stgSql = new StringBuilder("CREATE TABLE tableName ( " );
        List<TableFieldsDTO> tableFieldsDTOS = buildPhysicalTableDTO.tableFieldsDTOS;
        //ods与stg类型不变,不然有的值,类型转换不来
        tableFieldsDTOS.forEach((l) -> {
            if(l.fieldType.contains("FLOAT")){
                sqlFileds.append("" +l.fieldName + " " + " numeric ,");
            }else if(l.fieldType.contains("INT")){
                sqlFileds.append("" +l.fieldName + " " + l.fieldType.toLowerCase() + ",");
            }else if(l.fieldType.contains("TEXT")){
                sqlFileds.append(l.fieldName + " " + l.fieldType.toLowerCase() + ",");
            }else{
                sqlFileds.append("" +l.fieldName + " " + l.fieldType.toLowerCase() + "("+l.fieldLength+"),");
            }
            stgSql.append("" +l.fieldName + " text,");
            if(l.isPrimarykey==1){
                pksql.append(l.fieldName+",");
            }

        });
        stgSql.append("fi_createtime varchar(50) DEFAULT to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss'),fi_updatetime varchar(50),"+ buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50) NOT NULL DEFAULT sys_guid())");
        sqlFileds.append("fi_createtime varchar(50) DEFAULT to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss'),fi_updatetime varchar(50),enableflag varchar(50)"+ buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50) NOT NULL DEFAULT sys_guid())");
        sql.append(sqlFileds);
        String stg_sql1 = sql.toString().replace("tableName", "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
        String stg_sql2 = stgSql.toString().replace("tableName", "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
        stg_sql2="DROP TABLE IF EXISTS "+"stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName+";"+stg_sql2;
        pg.postgreBuildTable(stg_sql2, BusinessTypeEnum.DATAINPUT);
        if (resultEnum.getCode() == ResultEnum.TASK_TABLE_NOT_EXIST.getCode()) {
            pg.postgreBuildTable(stg_sql1, BusinessTypeEnum.DATAINPUT);
            log.info("【PGSTG】" + stg_sql1);
            log.info("pg：建表完成");
        }
    }


    public void saveOrUpdate(ModelPublishTableDTO dto) {
        log.info("开始保存ods版本号,参数为{}", dto);
        // 保存ods版本号
        taskPgTableStructureHelper.saveTableStructure(dto);
        dto.tableName = "stg_" + dto.tableName.substring(4);
        dto.createType = 4;
        log.info("开始保存stg版本号,参数为{}", dto);
        // 保存stg版本号
        taskPgTableStructureHelper.saveTableStructure(dto);
        log.info("保存版本号方法执行成功");

    }

    public static void main(String[] args) {
        CronExpression expr = null;
        try {
            expr = new CronExpression("0 25 20 * * ?");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(expr.getNextValidTimeAfter(new Date()));
        System.out.println(expr.getTimeZone());
        System.out.println(expr.getExpressionSummary());
        System.out.println("=======");
        TimeZone tz = TimeZone.getTimeZone("GMT - 1200");
        expr.setTimeZone(tz);
        System.out.println(expr.getNextValidTimeAfter(new Date()));
        System.out.println(expr.getTimeZone());
        System.out.println(expr.getExpressionSummary());
    }
}
