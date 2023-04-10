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
public class BuildPgTableImpl implements IbuildTable {


    @Override
    public List<String> buildStgAndOdsTable(BuildPhysicalTableDTO buildPhysicalTableDTO) {
        log.info("保存版本号方法执行成功");
        StringBuilder sql = new StringBuilder("CREATE TABLE fi_tableName ( ");
        StringBuilder sqlFileds = new StringBuilder();
        StringBuilder pksql = new StringBuilder("PRIMARY KEY ( ");
        StringBuilder stgSql = new StringBuilder("CREATE TABLE fi_tableName ( ");
        changeCase(buildPhysicalTableDTO);
        List<TableFieldsDTO> tableFieldsDTOS = buildPhysicalTableDTO.tableFieldsDTOS;
        //ods与stg类型不变,不然有的值,类型转换不来
        tableFieldsDTOS.forEach((l) -> {
            if (l.fieldType.contains("FLOAT")) {
                sqlFileds.append("" + l.fieldName + " " + " numeric ,");
            } else if (l.fieldType.contains("INT")) {
                sqlFileds.append("" + l.fieldName + " " + l.fieldType.toLowerCase() + ",");
            } else if (l.fieldType.contains("TEXT")) {
                sqlFileds.append("" + l.fieldName + " " + l.fieldType.toLowerCase() + ",");
            } else if (l.fieldType.toUpperCase().equals("DATE")) {
                sqlFileds.append("" + l.fieldName + " " + l.fieldType.toLowerCase() + ",");
            } else if (l.fieldType.toUpperCase().equals("TIME")) {
                sqlFileds.append("" + l.fieldName + " " + l.fieldType.toLowerCase() + ",");
            } else if (l.fieldType.contains("BIT")) {
                sqlFileds.append("" + l.fieldName + " " + l.fieldType.toLowerCase() + ",");
            } else {
                sqlFileds.append("" + l.fieldName + " " + l.fieldType.toLowerCase() + "(" + l.fieldLength + "),");
            }
            stgSql.append("" + l.fieldName + " text,");
            if (l.isPrimarykey == 1) {
                pksql.append("" + l.fieldName + ",");
            }

        });
        stgSql.append("fi_createtime varchar(50) DEFAULT to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss'),fi_updatetime varchar(50),fi_enableflag varchar(50)," +
                "fi_error_message varchar(250),fidata_batch_code varchar(50),fidata_flow_batch_code varchar(50), fi_sync_type varchar(50) DEFAULT '2',fi_verify_type varchar(50) DEFAULT '3'," + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50) NOT NULL DEFAULT sys_guid()");

        sqlFileds.append("fi_createtime varchar(50) DEFAULT to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss'),fi_updatetime varchar(50),fidata_batch_code varchar(50)," + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50) NOT NULL DEFAULT sys_guid()");
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
            stg_sql1 = sql.toString().replace("fi_tableName", buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName);
            stg_sql2 = stgSql.toString().replace("fi_tableName", buildPhysicalTableDTO.appAbbreviation + ".stg_" + buildPhysicalTableDTO.tableName);
            stg_sql2 = "DROP TABLE IF EXISTS " + buildPhysicalTableDTO.appAbbreviation + ".stg_" + buildPhysicalTableDTO.tableName + ";" + stg_sql2 +
                    "create index " + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "enableflagsy on " + buildPhysicalTableDTO.appAbbreviation + ".stg_" + buildPhysicalTableDTO.tableName + " (fi_enableflag);";
        } else {
            stg_sql1 = sql.toString().replace("fi_tableName", "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
            stg_sql2 = stgSql.toString().replace("fi_tableName", "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
            stg_sql2 = "DROP TABLE IF EXISTS " + "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + ";" + stg_sql2 +
                    "create index " + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "enableflagsy on stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + " (fi_enableflag);";
        }
        List<String> sqlList = new ArrayList<>();
        sqlList.add(stg_sql1);
        sqlList.add(stg_sql2);
        return sqlList;
    }

    private void changeCase(BuildPhysicalTableDTO buildPhysicalTable) {
        buildPhysicalTable.tableName = buildPhysicalTable.tableName.toLowerCase();
        buildPhysicalTable.appAbbreviation = buildPhysicalTable.appAbbreviation.toLowerCase();
        buildPhysicalTable.apiTableNames = JSON.parseArray(JSON.toJSONString(buildPhysicalTable.apiTableNames), String.class);
        buildPhysicalTable.modelPublishTableDTO.tableName = buildPhysicalTable.modelPublishTableDTO.tableName.toLowerCase();
        buildPhysicalTable.modelPublishTableDTO.fieldList.forEach(
                e -> {
                    e.fieldEnName = e.fieldEnName.toLowerCase();
                }
        );
        buildPhysicalTable.tableFieldsDTOS.forEach(
                e -> {
                    e.fieldName = e.fieldName.toLowerCase();
                }
        );
    }

    @Override
    public String queryTableNum(BuildPhysicalTableDTO buildPhysicalTableDTO) {
        String selectTable = "select count(1) from pg_class t2,information_schema.tables t1 where t1.\"table_name\" = t2.relname and ";
        if (buildPhysicalTableDTO.whetherSchema) {
            selectTable += "table_schema = '" + buildPhysicalTableDTO.appAbbreviation + "' and ";
            for (String tableName : buildPhysicalTableDTO.apiTableNames) {
                selectTable += "( relname='" + tableName + "' or";
            }
        } else {
            selectTable += "table_schema = 'public' and ";
            for (String tableName : buildPhysicalTableDTO.apiTableNames) {
                selectTable += "( relname='ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + tableName + "' or";
            }
        }
        selectTable = selectTable.substring(0, selectTable.length() - 2) + " ) ";
        return selectTable.toLowerCase();
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
                //同步方式
                String syncMode = syncModeTypeEnum.getNameByValue(config.targetDsConfig.syncMode);
                if (Objects.nonNull(buildNifiFlow) && StringUtils.isNotEmpty(buildNifiFlow.generateVersionSql)) {
                    sql += ",'" + syncModeTypeEnum.FULL_VOLUME_VERSION.getName() + "'";
                } else {
                    sql += ",'" + syncMode + "'";
                }
            } else {
                String fieldList = config.modelPublishFieldDTOList.stream().filter(Objects::nonNull)
                        .filter(e -> e.fieldEnName != null && !Objects.equals("", e.fieldEnName))
                        .map(t -> t.fieldEnName).collect(Collectors.joining("'',''"));
                sql += fieldList + "','" + tableKey + "','" + targetTableName + "'";
                sql += ",'" + config.processorConfig.targetTableName.substring(4) + "'";
                //同步方式
                String syncMode = syncModeTypeEnum.getNameByValue(config.targetDsConfig.syncMode);
                sql += ",'" + syncMode + "'";
            }
        } else {
            List<String> stgAndTableName = getStgAndTableName(targetTableName);
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
                String fieldList = config.targetDsConfig.tableFieldsList.stream().filter(Objects::nonNull)
                        .filter(e -> e.fieldName != null && !Objects.equals("", e.fieldName))
                        .map(t -> t.fieldName).collect(Collectors.joining("'',''"));
                sql += fieldList + "','" + stgAndTableName.get(2) + "','" + stgAndTableName.get(0) + "'";
                sql += ",'" + stgAndTableName.get(1) + "'";
                //同步方式
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
        log.info("函数语句:" + sql);
        return sql.toLowerCase();
    }

    @Override
    public String prepareCallSql() {
        return "call pg_check_table_structure(?,?)";
    }

    @Override
    public String queryNumbersField(BuildNifiFlowDTO dto, DataAccessConfigDTO config, String groupId) {
        List<String> stgAndTableName = getStgAndTableName(config.processorConfig.targetTableName);
        String querySql = "";
        if (Objects.equals(dto.type, OlapTableEnum.WIDETABLE) || Objects.equals(dto.type, OlapTableEnum.KPI)) {
            querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers ,now() as end_time," +
                    "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," +
                    "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + config.processorConfig.targetTableName;
        } else {
            if (Objects.equals(dto.synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
                querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers ,to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss') as end_time," +
                        "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," +
                        "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + stgAndTableName.get(1) + " where fidata_batch_code='${fidata_batch_code}'";
            } else {
                querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers ,to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss') as end_time," +
                        "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," +
                        "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + config.processorConfig.targetTableName + " where fidata_batch_code='${fidata_batch_code}'";
            }

        }
        return querySql;
    }

    @Override
    public String queryNumbersFieldForTableServer(BuildNifiFlowDTO dto, DataAccessConfigDTO config, String groupId) {

        String querySql = "";
        if (Objects.equals(dto.type, OlapTableEnum.WIDETABLE) || Objects.equals(dto.type, OlapTableEnum.KPI)) {
            querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers ,now() as end_time," +
                    "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," +
                    "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + config.processorConfig.targetTableName;
        } else {
            if (Objects.equals(dto.synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
                querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers ,to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss') as end_time," +
                        "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," +
                        "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + config.processorConfig.targetTableName + " where fidata_batch_code='${fidata_batch_code}'";
            } else {
                querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers ,to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss') as end_time," +
                        "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," +
                        "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + config.processorConfig.targetTableName + " where fidata_batch_code='${fidata_batch_code}'";
            }

        }
        return querySql;
    }

    @Override
    public String getTotalSql(String sql, SynchronousTypeEnum synchronousTypeEnum) {
        //待定
        return sql;
    }

    @Override
    public void fieldFormatModification(DataAccessConfigDTO data) {
        data.businessKeyAppend = StringUtils.isNotEmpty(data.businessKeyAppend) ? data.businessKeyAppend.toLowerCase() : null;
        if (data.processorConfig != null && StringUtils.isNotEmpty(data.processorConfig.targetTableName)) {
            data.processorConfig.targetTableName = data.processorConfig.targetTableName.toLowerCase();
        }
        if (data.targetDsConfig != null && StringUtils.isNotEmpty(data.targetDsConfig.targetTableName)) {
            data.targetDsConfig.targetTableName = data.targetDsConfig.targetTableName.toLowerCase();
        }
        assert data.targetDsConfig != null;
        data.targetDsConfig.tableFieldsList.forEach(
                e -> {
                    e.fieldName = e.fieldName.toLowerCase();
                }
        );
    }

    @Override
    public String getEsqlAutoCommit() {
        return "false";
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
                stgSqlFileds.append("\"" + l.fieldEnName + "\" text,");
            } else if (l.fieldType.toLowerCase().contains("numeric") || l.fieldType.toLowerCase().contains("float")) {
                sqlFileds.append("\"" + l.fieldEnName + "\" float ,");
                stgSqlFileds.append("\"" + l.fieldEnName + "\" text,");
            } else {
                sqlFileds.append("\"" + l.fieldEnName + "\" " + l.fieldType.toLowerCase() + "(" + l.fieldLength + ") ,");
                stgSqlFileds.append("\"" + l.fieldEnName + "\" text,");
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
        String stgTable = "DROP TABLE IF EXISTS stg_" + tableName + "; CREATE TABLE stg_" + tableName + " (" + tablePk + " varchar(50) NOT NULL DEFAULT sys_guid()," + stgSqlFileds.toString() + associatedKey + "fi_createtime varchar(50) DEFAULT to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss'),fi_updatetime varchar(50),fi_enableflag varchar(50),fi_error_message text,fidata_batch_code varchar(50),fidata_flow_batch_code varchar(50), fi_sync_type varchar(50) DEFAULT '2',fi_verify_type varchar(50) DEFAULT '3');";
        stgTable += "create index " + tableName + "enableflagsy on stg_" + tableName + " (fi_enableflag);";
        sqlList.add(stgTable);
        sqlList.add(sql1);

        return sqlList;
    }
}
