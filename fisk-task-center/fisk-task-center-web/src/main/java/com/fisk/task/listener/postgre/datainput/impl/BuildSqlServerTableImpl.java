package com.fisk.task.listener.postgre.datainput.impl;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.enums.task.FuncNameEnum;
import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.common.core.utils.TableNameGenerateUtils;
import com.fisk.dataaccess.dto.table.TableBusinessDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.dataaccess.enums.syncModeTypeEnum;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.entity.TaskDwDimPO;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.postgre.datainput.IbuildTable;
import com.fisk.task.mapper.TaskDwDimMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author cfk
 */
@Component
@Slf4j
public class BuildSqlServerTableImpl implements IbuildTable {
    @Resource
    TaskDwDimMapper taskDwDimMapper;

    @Override
    public List<String> buildStgAndOdsTable(BuildPhysicalTableDTO buildPhysicalTableDTO) {

        log.info("保存版本号方法执行成功");
        StringBuilder sql = new StringBuilder("CREATE TABLE fi_tableName ( ");
        StringBuilder sqlFileds = new StringBuilder();
        StringBuilder pksql = new StringBuilder();
        StringBuilder stgSql = new StringBuilder("CREATE TABLE fi_tableName ( ");
        List<TableFieldsDTO> tableFieldsDTOS = buildPhysicalTableDTO.tableFieldsDTOS;
        //ods与stg类型不变,不然有的值,类型转换不来
        tableFieldsDTOS.forEach((l) -> {
            if (l.fieldType.contains("FLOAT")) {
                sqlFileds.append("" + l.fieldName + " " + " numeric(18,9) ");
            } else if (l.fieldType.contains("INT")) {
                sqlFileds.append("" + l.fieldName + " " + l.fieldType.toLowerCase() + " ");
            } else if (l.fieldType.contains("TEXT")) {
                sqlFileds.append("" + l.fieldName + " " + l.fieldType.toLowerCase() + " ");
            } else if (l.fieldType.contains("TIMESTAMP")) {
                sqlFileds.append("" + l.fieldName + " datetime ");
            } else {
                sqlFileds.append("" + l.fieldName + " " + l.fieldType.toLowerCase() + "(" + l.fieldLength + ") ");
            }
            //todo 修改stg表,字段类型
            stgSql.append("" + l.fieldName + " nvarchar(4000),");
            if (l.isPrimarykey == 1) {
                pksql.append("" + l.fieldName + ",");
                sqlFileds.append("not null ,");
            } else {
                sqlFileds.append(",");
            }

        });
        stgSql.append("fi_createtime varchar(50) DEFAULT (format(GETDATE(),'yyyy-MM-dd HH:mm:ss') ),fi_updatetime varchar(50),fi_version varchar(50),fi_enableflag varchar(50)," +
                "fi_error_message varchar(250),fidata_batch_code varchar(50),fidata_flow_batch_code varchar(50), fi_sync_type varchar(50) DEFAULT '2',fi_verify_type varchar(50) DEFAULT '3'," + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50) NOT NULL DEFAULT (newid())");

        sqlFileds.append("fi_createtime varchar(50) DEFAULT (format(GETDATE(),'yyyy-MM-dd HH:mm:ss')),fi_updatetime varchar(50),fi_version varchar(50),fidata_batch_code varchar(50)," + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50) NOT NULL DEFAULT (newid())");
        String havePk = pksql.toString();
        sqlFileds.append(")");
        stgSql.append(");");
        sql.append(sqlFileds);
        String stg_sql1 = "";
        String stg_sql2 = "";
        String odsTableName = "";
        if (buildPhysicalTableDTO.whetherSchema) {
            odsTableName = buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName;
            stg_sql1 = sql.toString().replace("fi_tableName", buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName);
            stg_sql2 = stgSql.toString().replace("fi_tableName", buildPhysicalTableDTO.appAbbreviation + ".stg_" + buildPhysicalTableDTO.tableName);
            stg_sql2 = "DROP TABLE IF EXISTS " + buildPhysicalTableDTO.appAbbreviation + ".stg_" + buildPhysicalTableDTO.tableName + ";" + stg_sql2 +
                    "create index enableflagsy on stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + " (fi_enableflag);";
        } else {
            odsTableName = "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName;
            stg_sql1 = sql.toString().replace("fi_tableName", "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
            stg_sql2 = stgSql.toString().replace("fi_tableName", "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
            stg_sql2 = "DROP TABLE IF EXISTS " + "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + ";" + stg_sql2 +
                    "create index enableflagsy on stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + " (fi_enableflag);";
        }
        List<String> sqlList = new ArrayList<>();
        //alter table Date add constraint PK_Date primary key(ID)
        if (StringUtils.isNotEmpty(havePk)) {
            // stg_sql1 += ";alter table " + odsTableName + " add constraint " + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "_pkey primary key(" + havePk.substring(0, havePk.length() - 1) + ")";
        }
        sqlList.add(stg_sql1);
        sqlList.add(stg_sql2);
        return sqlList;
    }

    @Override
    public String queryTableNum(BuildPhysicalTableDTO buildPhysicalTableDTO) {
        String selectTable = "";
        selectTable = selectTable.substring(0, selectTable.length() - 2);
        if (buildPhysicalTableDTO.whetherSchema) {
            //select * from sys.schemas ss left join sys.tables st on ss.schema_id=st.schema_id where ss.name ='dbo' and st.name='stg_dim_ghs3'
            selectTable = "select count(*) from sys.tables st left join sys.schemas ss on ss.schema_id=st.schema_id where ss.name = '"
                    + buildPhysicalTableDTO.appAbbreviation + "' and ";
            for (String tableName : buildPhysicalTableDTO.apiTableNames) {

                selectTable += " st.name='" + buildPhysicalTableDTO.appAbbreviation + "." + tableName + "' or";
            }
            selectTable = selectTable.substring(0, selectTable.length() - 2);
        } else {
            //select * from sys.schemas ss left join sys.tables st on ss.schema_id=st.schema_id where ss.name ='dbo' and st.name='stg_dim_ghs3'
            selectTable = "select count(*) from sys.tables st left join sys.schemas ss on ss.schema_id=st.schema_id where ss.name = 'dbo' ";
            for (String tableName : buildPhysicalTableDTO.apiTableNames) {
                selectTable += " st.name='dbo.ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + tableName + "' or";
            }
            selectTable = selectTable.substring(0, selectTable.length() - 2);
        }
        return selectTable;
    }

    @Override
    public String assemblySql(DataAccessConfigDTO config, SynchronousTypeEnum synchronousTypeEnum, String funcName, BuildNifiFlowDTO buildNifiFlow) {
        log.info("assemblySql方法参数,{},{},{},{}", JSON.toJSONString(config), JSON.toJSONString(synchronousTypeEnum), JSON.toJSONString(funcName), JSON.toJSONString(buildNifiFlow));
        TableBusinessDTO business = config.businessDTO;
        String tableKey = "";
        String targetTableName = config.processorConfig.targetTableName;
        List<String> stgAndTableName = getStgAndTableName(targetTableName);
        String sql = "";
        if (buildNifiFlow != null && StringUtils.isNotEmpty(buildNifiFlow.updateSql)) {
            sql += "call public." + funcName + "('" + buildNifiFlow.updateSql + "','";
        } else {
            sql += "call public." + funcName + "('','";
        }
        sql = sql.replaceFirst("call public." + funcName + "\\(", "exec [dbo]." + funcName);
        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTOPG)) {
            if (targetTableName.startsWith("dim_")) {
                tableKey = targetTableName.substring(4) + "key";
            } else if (targetTableName.startsWith("fact_")) {
                tableKey = targetTableName.substring(5) + "key";
            }
            if (Objects.equals(funcName, FuncNameEnum.PG_DATA_STG_TO_ODS_DELETE.getName())) {
                sql += "stg_" + targetTableName + "'";
                sql += ",'" + targetTableName + "'";
                //同步方式
                String syncMode = syncModeTypeEnum.getNameByValue(config.targetDsConfig.syncMode);
                if (Objects.nonNull(buildNifiFlow) && StringUtils.isNotEmpty(buildNifiFlow.generateVersionSql)) {
                    sql += ",'" + syncModeTypeEnum.FULL_VOLUME_VERSION.getName() + "'";
                } else {
                    sql += ",'" + syncMode + "'";
                }
            } else {
                sql += "${fragment.index}','''";
                String fieldList = config.modelPublishFieldDTOList.stream().filter(Objects::nonNull)
                        .filter(e -> e.fieldEnName != null && !Objects.equals("", e.fieldEnName))
                        .map(t -> t.fieldEnName).collect(Collectors.joining("'''',''''"));
                sql += fieldList + "''','" + tableKey + "','stg_" + targetTableName + "'";
                sql += ",'" + config.processorConfig.targetTableName + "'";
                String syncMode = syncModeTypeEnum.getNameByValue(config.targetDsConfig.syncMode);
                sql += ",'" + syncMode + "'";
            }
        } else {

            tableKey = stgAndTableName.get(2);
            if (Objects.equals(funcName, FuncNameEnum.PG_DATA_STG_TO_ODS_DELETE.getName())) {
                sql += stgAndTableName.get(0) + "'";
                sql += ",'" + stgAndTableName.get(1) + "'";
                //同步方式
                String syncMode = syncModeTypeEnum.getNameByValue(config.targetDsConfig.syncMode);
                if (Objects.nonNull(buildNifiFlow) && StringUtils.isNotEmpty(buildNifiFlow.generateVersionSql)) {
                    sql += ",'" + syncModeTypeEnum.FULL_VOLUME_VERSION.getName() + "'";
                } else {
                    sql += ",'" + syncMode + "'";
                }
            } else {
                //sql +="${fragment.index}','${fidata_batch_code}','";
                sql += "${fragment.index}','''";
                String fieldList = config.targetDsConfig.tableFieldsList.stream().filter(Objects::nonNull)
                        .filter(e -> e.fieldName != null && !Objects.equals("", e.fieldName))
                        .map(t -> t.fieldName).collect(Collectors.joining("'''',''''"));
                sql += fieldList + "''','" + tableKey + "','" + stgAndTableName.get(0) + "'";
                sql += ",'" + stgAndTableName.get(1) + "'";
                String syncMode = syncModeTypeEnum.getNameByValue(config.targetDsConfig.syncMode);
                sql += ",'" + syncMode + "'";
            }
        }

        //主键
        sql += config.businessKeyAppend == null ? ",''" : ",'" + config.businessKeyAppend + "'";
        if (business == null) {
            if (Objects.equals(funcName, FuncNameEnum.PG_DATA_STG_TO_ODS_DELETE.getName())) {
                sql += ",0,'',0,'','',0,'','',0,'')";
            } else if (Objects.equals(funcName, FuncNameEnum.PG_DATA_STG_TO_ODS_TOTAL.getName())) {
                sql += ",0,'',0,'','',0,'','',0,'')";
            }
        } else {
            //模式
            sql += "," + business.otherLogic;
            //年月日
            sql += (business.businessTimeFlag == null ? ",''" : ",'" + business.businessTimeFlag) + "'";
            //具体日期
            sql += "," + business.businessDate;
            //业务时间覆盖字段
            sql += (business.businessTimeField == null ? ",''" : ",'" + business.businessTimeField) + "'";
            //businessOperator
            String businessOperator = business.businessOperator;
            sql += (businessOperator == null ? ",''" : ",'" + businessOperator) + "'";
            //业务覆盖范围
            sql += "," + business.businessRange;
            //业务覆盖单位
            sql += (business.rangeDateUnit == null ? ",''" : ",'" + business.rangeDateUnit) + "'";
            //其他逻辑,逻辑符号
            String businessOperatorStandby = business.businessOperatorStandby;
            sql += (businessOperatorStandby == null ? ",''" : ",'" + businessOperatorStandby) + "'";
            //其他逻辑  业务覆盖范围
            sql += "," + business.businessRangeStandby;
            //其他逻辑  业务覆盖单位
            sql += (business.rangeDateUnitStandby == null ? ",''" : ",'" + business.rangeDateUnitStandby) + "')";
        }
        if (Objects.equals(funcName, FuncNameEnum.PG_DATA_STG_TO_ODS_TOTAL.getName())) {
            if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTOPG)) {
                //String s = associatedConditions(config);
                String s = "";
                sql = sql.substring(0, sql.length() - 1);
                //预先留住这个判断  s == null && s.length() < 2
                if (StringUtils.isEmpty(s)) {
                    sql += ",'')";
                } else {
                    sql += ",'{\"AssociatedConditionDTO\":" + s + "}')";
                }
            } else if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
                sql = sql.substring(0, sql.length() - 1);
                sql += ",'')";
            }
        }
        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.TOPGODS) || Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTOPG)) {
            sql = sql.replaceFirst("\\)", "");
        }
        log.info("函数语句:" + sql);
        return sql;
    }

    @Override
    public String prepareCallSql() {
        return "call pg_check_table_structure_sqlserver(?,?)";
    }

    @Override
    public String queryNumbersField(BuildNifiFlowDTO dto, DataAccessConfigDTO config, String groupId) {
        //convert(varchar(100),getdate(),120)
        List<String> stgAndTableName = getStgAndTableName(config.processorConfig.targetTableName);
        if (config.processorConfig.targetTableName.contains("\\.")) {
        }
        String querySql = "";
        if (Objects.equals(dto.type, OlapTableEnum.WIDETABLE) || Objects.equals(dto.type, OlapTableEnum.KPI)) {
            querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers ,now() as end_time," +
                    "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," +
                    "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + config.processorConfig.targetTableName;
        } else {
            if (Objects.equals(dto.synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
                querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers ,convert(varchar(100),getdate(),120) as end_time," +
                        "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," +
                        "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + stgAndTableName.get(1) + " where fidata_batch_code='${pipelTraceId:isEmpty():ifElse(${pipelTaskTraceId},${pipelTraceId})}'";
            } else {
                querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers ,convert(varchar(100),getdate(),120) as end_time," +
                        "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," +
                        "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + config.processorConfig.targetTableName + " where fidata_batch_code='${fidata_batch_code}'";
            }

        }
        return querySql;

    }


    @Override
    public List<String> getStgAndTableName(String tableName) {
        return TableNameGenerateUtils.getStgAndTableName(tableName);
    }

    @Override
    public List<String> buildDwStgAndOdsTable(ModelPublishTableDTO modelPublishTableDTO) {

        List<String> sqlList = new ArrayList<>();
        List<ModelPublishFieldDTO> fieldList = modelPublishTableDTO.fieldList;
        String tableName = modelPublishTableDTO.tableName;
        String tablePk = "";
        if (modelPublishTableDTO.createType == 0) {
            tablePk = "\"" + tableName.substring(4) + "key\"";
        } else {
            tablePk = "\"" + tableName.substring(5) + "key\"";
        }

        StringBuilder sql = new StringBuilder();
        StringBuilder pksql = new StringBuilder("PRIMARY KEY ( ");
        sql.append("CREATE TABLE " + modelPublishTableDTO.tableName + " ( " + tablePk + " varchar(50), ");
        StringBuilder sqlFileds = new StringBuilder();
        StringBuilder sqlFileds1 = new StringBuilder();
        StringBuilder stgSqlFileds = new StringBuilder();
        log.info("pg_dw建表字段信息:" + fieldList);
        fieldList.forEach((l) -> {
            if (l.fieldType.contains("INT") || l.fieldType.contains("TEXT")) {
                sqlFileds.append("\"" + l.fieldEnName + "\" " + l.fieldType.toLowerCase() + ",");
                stgSqlFileds.append("\"" + l.fieldEnName + "\" varchar(4000),");
            } else if (l.fieldType.toLowerCase().contains("numeric") || l.fieldType.toLowerCase().contains("float")) {
                sqlFileds.append("\"" + l.fieldEnName + "\" numeric(18,9) ,");
                stgSqlFileds.append("\"" + l.fieldEnName + "\" varchar(4000),");
            } else if (l.fieldType.contains("TIMESTAMP")) {
                sqlFileds.append("" + l.fieldEnName + " datetime ,");
                stgSqlFileds.append("\"" + l.fieldEnName + "\" varchar(4000),");
            } else {
                sqlFileds.append("\"" + l.fieldEnName + "\" " + l.fieldType.toLowerCase() + "(" + l.fieldLength + ") ,");
                stgSqlFileds.append("\"" + l.fieldEnName + "\" varchar(4000),");
            }
            if (l.isPrimaryKey == 1) {
                pksql.append("" + l.fieldEnName + " ,");
            }

        });

        String sql1 = sql.toString();
        //String associatedKey = associatedConditions(fieldList);
        String associatedKey = "";
        String sql2 = sqlFileds.toString() + associatedKey;
        sql2 += "fi_createtime varchar(50),fi_updatetime varchar(50)";
        sql2 += ",fidata_batch_code varchar(50)";
        String sql3 = sqlFileds1.toString();
        if (Objects.equals("", sql3)) {
            sql1 += sql2;
        } else {
            sql1 += sql2 + sql3;
        }
        String havePk = pksql.toString();
        if (havePk.length() != 14) {
            sql1 += "," + havePk.substring(0, havePk.length() - 1) + ")";
        }
        sql1 += ")";
        //创建表
        log.info("pg_dw建表语句" + sql1);
        //String stgTable = sql1.replaceFirst(tableName, "stg_" + tableName);
        String stgTable = "DROP TABLE IF EXISTS stg_" + tableName + "; CREATE TABLE stg_" + tableName + " (" + tablePk + " varchar(50) NOT NULL DEFAULT(newid())," + stgSqlFileds.toString() + associatedKey + "fi_createtime varchar(50) DEFAULT(format(GETDATE(),'yyyy-MM-dd HH:mm:ss')),fi_updatetime varchar(50),fi_enableflag varchar(50),fi_error_message text,fidata_batch_code varchar(50),fidata_flow_batch_code varchar(50), fi_sync_type varchar(50) DEFAULT '2',fi_verify_type varchar(50) DEFAULT '3');";
        stgTable += "create index " + tableName + "enableflagsy on stg_" + tableName + " (fi_enableflag);";
        sqlList.add(stgTable);
        sqlList.add(sql1);
        return sqlList;
    }

}
