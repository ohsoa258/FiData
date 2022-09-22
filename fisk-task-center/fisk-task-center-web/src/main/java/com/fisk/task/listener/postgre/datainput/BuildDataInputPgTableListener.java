package com.fisk.task.listener.postgre.datainput;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.baseObject.entity.BusinessResult;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.dataaccess.enums.PublishTypeEnum;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.enums.DbTypeEnum;
import com.fisk.task.listener.atlas.BuildAtlasTableAndColumnTaskListener;
import com.fisk.task.mapper.TaskPgTableStructureMapper;
import com.fisk.task.service.nifi.IPostgreBuild;
import com.fisk.task.utils.StackTraceHelper;
import com.fisk.task.utils.TaskPgTableStructureHelper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    @Resource
    TaskPgTableStructureMapper taskPgTableStructureMapper;
    @Resource
    BuildAtlasTableAndColumnTaskListener buildAtlasTableAndColumnTaskListener;
    @Resource
    UserClient userClient;


    public ResultEnum msg(String dataInfo, Acknowledgment acke) {
        log.info("执行pg build table");
        log.info("dataInfo:" + dataInfo);
        ModelPublishStatusDTO modelPublishStatusDTO = new ModelPublishStatusDTO();
        modelPublishStatusDTO.publish = PublishTypeEnum.SUCCESS.getValue();
        BuildPhysicalTableDTO buildPhysicalTableDTO = JSON.parseObject(dataInfo, BuildPhysicalTableDTO.class);
        modelPublishStatusDTO.tableId = Long.parseLong(buildPhysicalTableDTO.dbId);
        modelPublishStatusDTO.apiId = buildPhysicalTableDTO.apiId;
        ModelPublishTableDTO dto = buildPhysicalTableDTO.modelPublishTableDTO;
        ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(5);
        DataSourceTypeEnum conType = fiDataDataSource.data.conType;
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        if (Objects.equals(DataSourceTypeEnum.SQLSERVER, conType)) {
            resultEnum = sqlServerCreateTable(dto, buildPhysicalTableDTO, modelPublishStatusDTO, acke, dataInfo);
        } else if (Objects.equals(DataSourceTypeEnum.POSTGRESQL, conType)) {
            resultEnum = pgCreateTable(dto, buildPhysicalTableDTO, modelPublishStatusDTO, acke, dataInfo);
        }
        return resultEnum;
    }


    public void saveOrUpdate(ModelPublishTableDTO dto) {
        log.info("开始保存ods版本号,参数为{}", dto);
        // 保存ods版本号
        taskPgTableStructureHelper.saveTableStructure(dto, null);
        dto.tableName = "stg_" + dto.tableName.substring(4);
        dto.createType = 4;
        log.info("开始保存stg版本号,参数为{}", dto);
        // 保存stg版本号
        taskPgTableStructureHelper.saveTableStructure(dto, null);
        log.info("保存版本号方法执行成功");

    }

    public ResultEnum sqlServerCreateTable(ModelPublishTableDTO dto, BuildPhysicalTableDTO buildPhysicalTableDTO, ModelPublishStatusDTO modelPublishStatusDTO, Acknowledgment acke, String dataInfo) {
        try {
            log.info("开始保存ods版本号,参数为{}", dto);
            // 保存ods版本号
            //获取时间戳版本号
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            Calendar calendar = Calendar.getInstance();
            String version = df.format(calendar.getTime());
            ResultEnum resultEnum = taskPgTableStructureHelper.saveTableStructure(dto, version);
            log.info("执行修改语句返回结果:" + resultEnum);
            if (resultEnum.getCode() != ResultEnum.TASK_TABLE_NOT_EXIST.getCode() && resultEnum.getCode() != ResultEnum.SUCCESS.getCode()) {
                taskPgTableStructureMapper.updatevalidVersion(version);
                throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
            }
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
                } else if (l.fieldType.contains("TIMESTAMP")) {
                    sqlFileds.append("\"" + l.fieldName + "\" datetime,");
                } else {
                    sqlFileds.append("\"" + l.fieldName + "\" " + l.fieldType.toLowerCase() + "(" + l.fieldLength + "),");
                }
                stgSql.append("\"" + l.fieldName + "\" text,");
                if (l.isPrimarykey == 1) {
                    pksql.append("\"" + l.fieldName + "\",");
                }

            });
            stgSql.append("fi_createtime varchar(50) DEFAULT (getdate()),fi_updatetime varchar(50),enableflag varchar(50)," +
                    "error_message varchar(250),fidata_batch_code varchar(50),fidata_flow_batch_code varchar(50), sync_type varchar(50) DEFAULT '2',verify_type varchar(50) DEFAULT '3'," + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50) NOT NULL DEFAULT (newid())");

            sqlFileds.append("fi_createtime varchar(50) DEFAULT (getdate()),fi_updatetime varchar(50),fidata_batch_code varchar(50)," + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50) NOT NULL DEFAULT (newid())");
            String havePk = pksql.toString();
            if (havePk.length() != 14) {
                sqlFileds.append("," + pksql.substring(0, pksql.length() - 1) + ")");
            }
            sqlFileds.append(")");
            stgSql.append(");");
            sql.append(sqlFileds);
            String stg_sql1 = "";
            String stg_sql2 = "";
            if (buildPhysicalTableDTO.whetherSchema) {
                stg_sql1 = sql.toString().replace("fi_tableName", "ods_" + buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName);
                stg_sql2 = stgSql.toString().replace("fi_tableName", "stg_" + buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName);
                stg_sql2 = "DROP TABLE IF EXISTS " + "stg_" + buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName + ";" + stg_sql2 +
                        "create index enableflagsy on stg_" + buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName + " (enableflag);";
            } else {
                stg_sql1 = sql.toString().replace("fi_tableName", "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
                stg_sql2 = stgSql.toString().replace("fi_tableName", "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
                stg_sql2 = "DROP TABLE IF EXISTS " + "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + ";" + stg_sql2 +
                        "create index enableflagsy on stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + " (enableflag);";
            }

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
                String selectTable = "";
                if (buildPhysicalTableDTO.whetherSchema) {
                    //select * from sys.schemas ss left join sys.tables st on ss.schema_id=st.schema_id where ss.name ='dbo' and st.name='stg_dim_ghs3'
                    selectTable = "select count(*) from sys.tables st left join sys.schemas ss on ss.schema_id=st.schema_id where ss.name = ods_"
                            + buildPhysicalTableDTO.appAbbreviation + " and ";
                    for (String tableName : buildPhysicalTableDTO.apiTableNames) {

                        selectTable += " st.name='ods_" + buildPhysicalTableDTO.appAbbreviation + "." + tableName.toLowerCase() + "' or";
                    }
                    selectTable = selectTable.substring(0, selectTable.length() - 2);
                } else {
                    //select * from sys.schemas ss left join sys.tables st on ss.schema_id=st.schema_id where ss.name ='dbo' and st.name='stg_dim_ghs3'
                    selectTable = "select count(*) from sys.tables st left join sys.schemas ss on ss.schema_id=st.schema_id where ss.name = 'dbo' ";
                    for (String tableName : buildPhysicalTableDTO.apiTableNames) {
                        selectTable += " st.name='dbo.ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + tableName.toLowerCase() + "' or";
                    }
                    selectTable = selectTable.substring(0, selectTable.length() - 2);
                }

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
            } else if (Objects.equals(buildPhysicalTableDTO.driveType, DbTypeEnum.oracle_cdc)) {
                log.info("oracle_cdc建表完成");
            } else {
                buildAtlasTableAndColumnTaskListener.msg(dataInfo, null);
            }

            return ResultEnum.SUCCESS;
        } catch (Exception e) {
            if (((buildPhysicalTableDTO.apiId != null && buildPhysicalTableDTO.appType == 0) || Objects.equals(buildPhysicalTableDTO.driveType, DbTypeEnum.api))) {
                modelPublishStatusDTO.publish = PublishTypeEnum.FAIL.getValue();
                dc.updateApiPublishStatus(modelPublishStatusDTO);
            } else {
                ModelPublishStatusDTO modelPublishStatus = new ModelPublishStatusDTO();
                modelPublishStatus.publishErrorMsg = StackTraceHelper.getStackTraceInfo(e);
                modelPublishStatus.publish = 2;
                modelPublishStatus.tableId = Long.parseLong(buildPhysicalTableDTO.dbId);
                dc.updateTablePublishStatus(modelPublishStatus);
            }
            log.error("创建表失败" + StackTraceHelper.getStackTraceInfo(e));
            return ResultEnum.ERROR;
        } finally {
            if (acke != null) {
                acke.acknowledge();
            }
        }
    }

    public ResultEnum pgCreateTable(ModelPublishTableDTO dto, BuildPhysicalTableDTO buildPhysicalTableDTO, ModelPublishStatusDTO modelPublishStatusDTO, Acknowledgment acke, String dataInfo) {
        try {
            log.info("开始保存ods版本号,参数为{}", dto);
            // 保存ods版本号
            //获取时间戳版本号
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            Calendar calendar = Calendar.getInstance();
            String version = df.format(calendar.getTime());
            ResultEnum resultEnum = taskPgTableStructureHelper.saveTableStructure(dto, version);
            log.info("执行修改语句返回结果:" + resultEnum);
            if (resultEnum.getCode() != ResultEnum.TASK_TABLE_NOT_EXIST.getCode() && resultEnum.getCode() != ResultEnum.SUCCESS.getCode()) {
                taskPgTableStructureMapper.updatevalidVersion(version);
                throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
            }
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
            stgSql.append("fi_createtime varchar(50) DEFAULT to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss'),fi_updatetime varchar(50),enableflag varchar(50)," +
                    "error_message varchar(250),fidata_batch_code varchar(50),fidata_flow_batch_code varchar(50), sync_type varchar(50) DEFAULT '2',verify_type varchar(50) DEFAULT '3'," +
                    buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50) NOT NULL DEFAULT sys_guid()");
            sqlFileds.append("fi_createtime varchar(50) DEFAULT to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss'),fi_updatetime varchar(50),fidata_batch_code varchar(50),"
                    + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50) NOT NULL DEFAULT sys_guid()");
            String havePk = pksql.toString();
            if (havePk.length() != 14) {
                sqlFileds.append("," + pksql.substring(0, pksql.length() - 1) + ")");
            }
            sqlFileds.append(")");
            stgSql.append(");");
            sql.append(sqlFileds);
            String stg_sql1 = "";
            String stg_sql2 = "";
            stg_sql1 = sql.toString().replace("fi_tableName", "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
            stg_sql2 = stgSql.toString().replace("fi_tableName", "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
            stg_sql2 = "DROP TABLE IF EXISTS " + "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + ";" + stg_sql2 +
                    "create index enableflagsy on stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + " (enableflag);";
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
                    selectTable += " relname='ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + tableName.toLowerCase() + "' or";
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
            } else if (Objects.equals(buildPhysicalTableDTO.driveType, DbTypeEnum.oracle_cdc)) {
                log.info("oracle_cdc建表完成");
            } else {
                buildAtlasTableAndColumnTaskListener.msg(dataInfo, null);
            }

            return ResultEnum.SUCCESS;
        } catch (Exception e) {
            if (((buildPhysicalTableDTO.apiId != null && buildPhysicalTableDTO.appType == 0) || Objects.equals(buildPhysicalTableDTO.driveType, DbTypeEnum.api))) {
                modelPublishStatusDTO.publish = PublishTypeEnum.FAIL.getValue();
                dc.updateApiPublishStatus(modelPublishStatusDTO);
            } else {
                ModelPublishStatusDTO modelPublishStatus = new ModelPublishStatusDTO();
                modelPublishStatus.publishErrorMsg = StackTraceHelper.getStackTraceInfo(e);
                modelPublishStatus.publish = 2;
                modelPublishStatus.tableId = Long.parseLong(buildPhysicalTableDTO.dbId);
                dc.updateTablePublishStatus(modelPublishStatus);
            }
            log.error("创建表失败" + StackTraceHelper.getStackTraceInfo(e));
            return ResultEnum.ERROR;
        } finally {
            if (acke != null) {
                acke.acknowledge();
            }
        }
    }
}
