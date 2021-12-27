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

        if (resultEnum.getCode() == ResultEnum.TASK_TABLE_NOT_EXIST.getCode()) {
            StringBuilder sql = new StringBuilder();
            StringBuilder sqlFileds = new StringBuilder();
            sql.append("CREATE TABLE tableName ( " + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50) NOT NULL DEFAULT sys_guid() PRIMARY KEY,fi_batch_code varchar(50),");
            List<TableFieldsDTO> tableFieldsDTOS = buildPhysicalTableDTO.tableFieldsDTOS;
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
            });
            sqlFileds.append("fi_createtime varchar(50) DEFAULT to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss'),fi_updatetime varchar(50))");
            sql.append(sqlFileds);
            String stg_sql1 = sql.toString().replace("tableName", "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
            String stg_sql2 = sql.toString().replace("tableName", "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
            pg.postgreBuildTable(stg_sql1, BusinessTypeEnum.DATAINPUT);
            pg.postgreBuildTable(stg_sql2, BusinessTypeEnum.DATAINPUT);
            log.info("【PGSTG】" + stg_sql1);
            log.info("pg：建表完成");
        }
    }
    public void updataOrCreateTable(List<TableFieldDetailDTO> tableFieldDetailDTOS,List<TableFieldsDTO> tableFieldsDTOS){
        if(tableFieldDetailDTOS.size()!=0){
            List<TableFieldDetailDTO> tableFieldDetailDTOS1 = new ArrayList<>();
            List<TableFieldsDTO> tableFieldsDTOS1 = new ArrayList<>();
            tableFieldDetailDTOS1.addAll(tableFieldDetailDTOS);
            tableFieldsDTOS1.addAll(tableFieldsDTOS);
            String tableName=tableFieldDetailDTOS.get(0).tableName;
            String tableName1="stg_"+tableName.substring(4);
            String tablePk=tableFieldDetailDTOS.get(0).tableName;
            tablePk=tablePk.substring(4)+"key";
            String sql="";
            String sql1="";
            //库里已有的字段
            for(int i=0;i<tableFieldDetailDTOS.size();i++){

                //传过来的字段
                for(int j=0;j<tableFieldsDTOS.size();j++){
                    //如果库里的字段比到最后没有相同的,说明这个字段被删掉了
                    if(Objects.equals(tableFieldDetailDTOS.get(i).columnName,tableFieldsDTOS.get(j).fieldName.toLowerCase())){
                        tableFieldDetailDTOS1.remove(tableFieldDetailDTOS.get(i));
                    }else if(Objects.equals(tableFieldDetailDTOS.get(i).columnName,tableFieldsDTOS.get(j).fieldName)&&!Objects.equals(tableFieldDetailDTOS.get(i).udtName,tableFieldsDTOS.get(j).fieldType)){
                        //修改字段ALTER TABLE table_name ALTER COLUMN column_name TYPE datatype;
                        sql="ALTER TABLE "+tableName+" ALTER COLUMN "+tableFieldDetailDTOS.get(i).columnName+" type "+tableFieldsDTOS.get(j).fieldType+"("+tableFieldsDTOS.get(j).fieldLength+")";
                        sql1="ALTER TABLE "+tableName1+" ALTER COLUMN "+tableFieldDetailDTOS.get(i).columnName+" type "+tableFieldsDTOS.get(j).fieldType+"("+tableFieldsDTOS.get(j).fieldLength+")";
                        log.info("修改字段" + sql);
                        pg.postgreBuildTable(sql,BusinessTypeEnum.DATAINPUT);
                        pg.postgreBuildTable(sql1,BusinessTypeEnum.DATAINPUT);
                    }
                }
            }
            for (TableFieldDetailDTO tableFieldDetailDTO:tableFieldDetailDTOS1) {
                if(Objects.equals(tableFieldDetailDTO.columnName,tablePk)||Objects.equals(tableFieldDetailDTO.columnName,"fi_batch_code")){
                    continue;
                }else{
                    sql="ALTER TABLE "+tableName+" DROP COLUMN "+tableFieldDetailDTO.columnName;
                    sql1="ALTER TABLE "+tableName1+" DROP COLUMN "+tableFieldDetailDTO.columnName;
                    log.info("删除字段" + sql);
                    pg.postgreBuildTable(sql,BusinessTypeEnum.DATAINPUT);
                    pg.postgreBuildTable(sql1,BusinessTypeEnum.DATAINPUT);
                }
            }
            for (int j=0;j<tableFieldsDTOS.size();j++) {
                for (int i=0;i<tableFieldDetailDTOS.size();i++) {
                    if(Objects.equals(tableFieldsDTOS.get(j).fieldName.toLowerCase(),tableFieldDetailDTOS.get(i).columnName)){
                        tableFieldsDTOS1.remove(tableFieldsDTOS.get(j));
                    }
                }
            }
            for (TableFieldsDTO tableFieldsDTO:tableFieldsDTOS1) {
                //添加字段ALTER TABLE table_name ADD column_name datatype;
                sql="ALTER TABLE "+tableName+" add COLUMN "+tableFieldsDTO.fieldName+" "+tableFieldsDTO.fieldType+"("+tableFieldsDTO.fieldLength+")";
                sql1="ALTER TABLE "+tableName1+" add COLUMN "+tableFieldsDTO.fieldName+" "+tableFieldsDTO.fieldType+"("+tableFieldsDTO.fieldLength+")";
                log.info("添加字段" + sql);
                pg.postgreBuildTable(sql,BusinessTypeEnum.DATAINPUT);
                pg.postgreBuildTable(sql1,BusinessTypeEnum.DATAINPUT);
            }
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
