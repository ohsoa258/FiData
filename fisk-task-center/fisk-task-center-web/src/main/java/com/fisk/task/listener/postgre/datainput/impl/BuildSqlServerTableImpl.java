package com.fisk.task.listener.postgre.datainput.impl;

import com.fisk.common.core.enums.task.FuncNameEnum;
import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.dataaccess.dto.table.TableBusinessDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.dataaccess.enums.syncModeTypeEnum;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.postgre.datainput.IbuildTable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author cfk
 */
@Component
@Slf4j
public class BuildSqlServerTableImpl implements IbuildTable {
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
                sqlFileds.append("\"" + l.fieldName + "\" " + " numeric(18,9) ");
            } else if (l.fieldType.contains("INT")) {
                sqlFileds.append("\"" + l.fieldName + "\" " + l.fieldType.toLowerCase() + " ");
            } else if (l.fieldType.contains("TEXT")) {
                sqlFileds.append("\"" + l.fieldName + "\" " + l.fieldType.toLowerCase() + " ");
            } else if (l.fieldType.contains("TIMESTAMP")) {
                sqlFileds.append("\"" + l.fieldName + "\" datetime ");
            } else {
                sqlFileds.append("\"" + l.fieldName + "\" " + l.fieldType.toLowerCase() + "(" + l.fieldLength + ") ");
            }
            //todo 修改stg表,字段类型
            stgSql.append("\"" + l.fieldName + "\" varchar(4000),");
            if (l.isPrimarykey == 1) {
                pksql.append("\"" + l.fieldName + "\",");
                sqlFileds.append("not null ,");
            } else {
                sqlFileds.append(",");
            }

        });
        stgSql.append("fi_createtime varchar(50) DEFAULT (getdate()),fi_updatetime varchar(50),enableflag varchar(50)," +
                "error_message varchar(250),fidata_batch_code varchar(50),fidata_flow_batch_code varchar(50), sync_type varchar(50) DEFAULT '2',verify_type varchar(50) DEFAULT '3'," + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50) NOT NULL DEFAULT (newid())");

        sqlFileds.append("fi_createtime varchar(50) DEFAULT (getdate()),fi_updatetime varchar(50),fidata_batch_code varchar(50)," + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50) NOT NULL DEFAULT (newid())");
        String havePk = pksql.toString();
        sqlFileds.append(")");
        stgSql.append(");");
        sql.append(sqlFileds);
        String stg_sql1 = "";
        String stg_sql2 = "";
        String odsTableName = "";
        if (buildPhysicalTableDTO.whetherSchema) {
            odsTableName = "ods_" + buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName;
            stg_sql1 = sql.toString().replace("fi_tableName", "ods_" + buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName);
            stg_sql2 = stgSql.toString().replace("fi_tableName", "stg_" + buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName);
            stg_sql2 = "DROP TABLE IF EXISTS " + "stg_" + buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName + ";" + stg_sql2 +
                    "create index enableflagsy on stg_" + buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName + " (enableflag);";
        } else {
            odsTableName = "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName;
            stg_sql1 = sql.toString().replace("fi_tableName", "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
            stg_sql2 = stgSql.toString().replace("fi_tableName", "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
            stg_sql2 = "DROP TABLE IF EXISTS " + "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + ";" + stg_sql2 +
                    "create index enableflagsy on stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + " (enableflag);";
        }
        List<String> sqlList = new ArrayList<>();
        //alter table Date add constraint PK_Date primary key(ID)
        if (StringUtils.isNotEmpty(havePk)) {
            stg_sql1 += ";alter table " + odsTableName + " add constraint " + odsTableName + "_pkey primary key(" + havePk.substring(0, havePk.length() - 2) + "\")";
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
        return selectTable;
    }

    @Override
    public String assemblySql(DataAccessConfigDTO config, SynchronousTypeEnum synchronousTypeEnum, String funcName, BuildNifiFlowDTO buildNifiFlow) {
        TableBusinessDTO business = config.businessDTO;
        String tableKey = "";
        String targetTableName = config.processorConfig.targetTableName;
        String sql = "";
        if (buildNifiFlow != null && StringUtils.isNotEmpty(buildNifiFlow.updateSql)) {
            sql += "call public." + funcName + "('" + buildNifiFlow.updateSql + "','";
        } else {
            sql += "call public." + funcName + "('','";
        }

        if (Objects.equals(synchronousTypeEnum, SynchronousTypeEnum.PGTOPG)) {
            if (targetTableName.startsWith("stg_dim_")) {
                tableKey = targetTableName.substring(8) + "key";
            } else if (targetTableName.startsWith("stg_fact_")) {
                tableKey = targetTableName.substring(9) + "key";
            }
            if (Objects.equals(funcName, FuncNameEnum.PG_DATA_STG_TO_ODS_DELETE.getName())) {
                sql += "stg_" + targetTableName + "'";
                sql += ",'" + targetTableName + "'";
            } else {
                String fieldList = config.modelPublishFieldDTOList.stream().filter(Objects::nonNull)
                        .filter(e -> e.fieldEnName != null && !Objects.equals("", e.fieldEnName))
                        .map(t -> t.fieldEnName).collect(Collectors.joining("'',''")).toLowerCase();
                sql += fieldList + "','" + tableKey + "','" + targetTableName + "'";
                sql += ",'" + config.processorConfig.targetTableName.substring(4) + "'";
            }
        } else {
            sql = sql.replaceFirst("call public." + funcName + "\\(", "exec [dbo]." + funcName);
            tableKey = targetTableName.substring(4) + "key";
            if (Objects.equals(funcName, FuncNameEnum.PG_DATA_STG_TO_ODS_DELETE.getName())) {
                sql += "stg_" + targetTableName + "'";
                sql += ",'ods_" + targetTableName + "'";
            } else {
                String fieldList = config.targetDsConfig.tableFieldsList.stream().filter(Objects::nonNull)
                        .filter(e -> e.fieldName != null && !Objects.equals("", e.fieldName))
                        .map(t -> t.fieldName).collect(Collectors.joining("'',''")).toLowerCase();
                sql += fieldList + "','" + tableKey + "','" + targetTableName + "'";
                sql += ",'ods_" + targetTableName.substring(4) + "'";
            }
        }
        //同步方式
        String syncMode = syncModeTypeEnum.getNameByValue(config.targetDsConfig.syncMode);
        sql += ",'" + syncMode + "'";
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
        sql = sql.replaceFirst("\\)", "");
        log.info("函数语句:" + sql);
        return sql;
    }

    @Override
    public String prepareCallSql() {
        return "call pg_check_table_structure_sqlserver(?,?)";
    }

    @Override
    public String queryNumbersField(BuildNifiFlowDTO dto, DataAccessConfigDTO config) {
        //convert(varchar(100),getdate(),120)
        String querySql = "";
        if (Objects.equals(dto.type, OlapTableEnum.WIDETABLE) || Objects.equals(dto.type, OlapTableEnum.KPI)) {
            querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers ,now() as end_time," +
                    "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," +
                    "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + config.processorConfig.targetTableName;
        } else {
            if (Objects.equals(dto.synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
                querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers ,convert(varchar(100),getdate(),120) as end_time," +
                        "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," +
                        "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from ods_" + config.processorConfig.targetTableName.substring(4) + " where fidata_batch_code='${fidata_batch_code}'";
            } else {
                querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers ,to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss') as end_time," +
                        "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," +
                        "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + config.processorConfig.targetTableName + " where fidata_batch_code='${fidata_batch_code}'";
            }

        }
        return querySql;

    }


}
