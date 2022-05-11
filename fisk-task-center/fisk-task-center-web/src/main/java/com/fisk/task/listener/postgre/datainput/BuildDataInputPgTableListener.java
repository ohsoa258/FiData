package com.fisk.task.listener.postgre.datainput;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.baseObject.entity.BusinessResult;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.dataaccess.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.dataaccess.enums.PublishTypeEnum;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.enums.DbTypeEnum;
import com.fisk.task.service.nifi.IPostgreBuild;
import com.fisk.task.utils.TaskPgTableStructureHelper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.springframework.kafka.support.Acknowledgment;
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
@Slf4j
public class BuildDataInputPgTableListener {
    @Resource
    IPostgreBuild pg;
    @Resource
    DataAccessClient dc;
    @Resource
    TaskPgTableStructureHelper taskPgTableStructureHelper;


    public ResultEnum msg(String dataInfo, Acknowledgment acke) {
        log.info("执行pg build table");
        log.info("dataInfo:" + dataInfo);
        ModelPublishStatusDTO modelPublishStatusDTO = new ModelPublishStatusDTO();
        modelPublishStatusDTO.publish = PublishTypeEnum.SUCCESS.getValue();
        BuildPhysicalTableDTO buildPhysicalTableDTO = JSON.parseObject(dataInfo, BuildPhysicalTableDTO.class);
        modelPublishStatusDTO.tableId = Long.parseLong(buildPhysicalTableDTO.dbId);
        ModelPublishTableDTO dto = buildPhysicalTableDTO.modelPublishTableDTO;
        try {
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
            StringBuilder sql = new StringBuilder("CREATE TABLE fi_tableName ( ");
            StringBuilder sqlFileds = new StringBuilder();
            StringBuilder pksql = new StringBuilder("PRIMARY KEY ( ");
            StringBuilder stgSql = new StringBuilder("CREATE TABLE fi_tableName ( ");
            List<TableFieldsDTO> tableFieldsDTOS = buildPhysicalTableDTO.tableFieldsDTOS;
            //ods与stg类型不变,不然有的值,类型转换不来
            tableFieldsDTOS.forEach((l) -> {
                if (l.fieldType.contains("FLOAT")) {
                    sqlFileds.append("\"" + l.fieldName + "\" " + " numeric ,");
                } else if (l.fieldType.contains("INT")) {
                    sqlFileds.append("\"" + l.fieldName + "\" " + l.fieldType.toLowerCase() + ",");
                } else if (l.fieldType.contains("TEXT")) {
                    sqlFileds.append("\"" + l.fieldName + "\" " + l.fieldType.toLowerCase() + ",");
                } else {
                    sqlFileds.append("\"" + l.fieldName + "\" " + l.fieldType.toLowerCase() + "(" + l.fieldLength + "),");
                }
                stgSql.append("\"" + l.fieldName + "\" text,");
                if (l.isPrimarykey == 1) {
                    pksql.append("\"" + l.fieldName + "\",");
                }

            });
            pksql.append(buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key))");
            stgSql.append("fi_createtime varchar(50) DEFAULT to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss'),fi_updatetime varchar(50),enableflag varchar(50)," + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50) NOT NULL DEFAULT sys_guid(),");
            stgSql.append(pksql);
            sqlFileds.append("fi_createtime varchar(50) DEFAULT to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss'),fi_updatetime varchar(50)," + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50) NOT NULL DEFAULT sys_guid(),");
            sqlFileds.append(pksql);
            sql.append(sqlFileds);
            String stg_sql1 = sql.toString().replace("fi_tableName", "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
            String stg_sql2 = stgSql.toString().replace("fi_tableName", "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
            stg_sql2 = "DROP TABLE IF EXISTS " + "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + ";" + stg_sql2;
            BusinessResult Result = pg.postgreBuildTable(stg_sql2.toLowerCase(), BusinessTypeEnum.DATAINPUT);
            if (!Result.success) {
                throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
            }
            if (resultEnum.getCode() == ResultEnum.TASK_TABLE_NOT_EXIST.getCode()) {
                pg.postgreBuildTable(stg_sql1.toLowerCase(), BusinessTypeEnum.DATAINPUT);
                log.info("【PGSTG】" + stg_sql1);
                log.info("pg：建表完成");
            }
            //实时应用改状态
            if (((buildPhysicalTableDTO.apiId != null && buildPhysicalTableDTO.appType == 0) || Objects.equals(buildPhysicalTableDTO.driveType, DbTypeEnum.api))) {
                int tableCount = 0;
                modelPublishStatusDTO.apiId = buildPhysicalTableDTO.apiId;
                String selectTable = "select count(*) from pg_class where ";
                for (String tableName : buildPhysicalTableDTO.apiTableNames) {
                    selectTable += " relname='ods_" + tableName + "' or";
                }
                selectTable = selectTable.substring(0, selectTable.length() - 2);
                BusinessResult businessResult = pg.postgreQuery(selectTable, BusinessTypeEnum.DATAINPUT);
                if (businessResult.data != null) {
                    List<Object> countList = JSON.parseArray(businessResult.data.toString(), Object.class);
                    String countString = countList.get(0).toString();
                    Map countMap = JSON.parseObject(countString, Map.class);
                    Object count = countMap.get("count");
                    tableCount = Integer.parseInt(count.toString());
                }
                if (tableCount == buildPhysicalTableDTO.apiTableNames.size()) {
                    dc.updateApiPublishStatus(modelPublishStatusDTO);
                }
            }
            return ResultEnum.SUCCESS;
        } catch (Exception e) {
            if (((buildPhysicalTableDTO.apiId != null && buildPhysicalTableDTO.appType == 0) || Objects.equals(buildPhysicalTableDTO.driveType, DbTypeEnum.api))) {
                modelPublishStatusDTO.publish = PublishTypeEnum.FAIL.getValue();
                dc.updateApiPublishStatus(modelPublishStatusDTO);
            }
            log.error("创建表失败");
            e.printStackTrace();
            return ResultEnum.ERROR;
        } finally {
            //acke.acknowledge();
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
