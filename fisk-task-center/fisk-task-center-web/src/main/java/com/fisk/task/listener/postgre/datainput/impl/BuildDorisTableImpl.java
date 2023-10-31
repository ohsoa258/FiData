package com.fisk.task.listener.postgre.datainput.impl;

import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.common.core.utils.TableNameGenerateUtils;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.mdmconfig.AccessMdmConfigDTO;
import com.fisk.task.dto.mdmtask.BuildMdmNifiFlowDTO;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.listener.postgre.datainput.IbuildTable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class BuildDorisTableImpl implements IbuildTable {
    @Override
    public List<String> buildStgAndOdsTable(BuildPhysicalTableDTO buildPhysicalTableDTO) {
        log.info("保存版本号方法执行成功");
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS fi_tableName ( ");
        StringBuilder sqlFileds = new StringBuilder();
        StringBuilder pksql = new StringBuilder();
        StringBuilder stgSql = new StringBuilder("CREATE TABLE fi_tableName (fi_createtime DATETIME DEFAULT CURRENT_TIMESTAMP,");
        List<TableFieldsDTO> tableFieldsDTOS = buildPhysicalTableDTO.tableFieldsDTOS;
        //ods与stg类型不变,不然有的值,类型转换不来
        tableFieldsDTOS.forEach((l) -> {
            if (l.fieldType.contains("FLOAT")) {
                sqlFileds.append(l.fieldName + " " + l.fieldType.toLowerCase() + " ");
            } else if (l.fieldType.contains("INT")) {
                sqlFileds.append(l.fieldName + " " + l.fieldType.toLowerCase() + " ");
            } else if (l.fieldType.contains("TEXT")) {
                sqlFileds.append(l.fieldName + " " + l.fieldType.toLowerCase() + " ");
                stgSql.append(l.fieldName + " " + l.fieldType.toLowerCase() + ",");
            } else if (l.fieldType.toUpperCase().equals("DATE")) {
                sqlFileds.append(l.fieldName + " " + l.fieldType.toLowerCase() + " ");
            } else if (l.fieldType.toUpperCase().equals("TIME")) {
                sqlFileds.append(l.fieldName + " " + l.fieldType.toLowerCase() + " ");
            } else if (l.fieldType.contains("TIMESTAMP") || StringUtils.equals(l.fieldType.toUpperCase(), "DATETIME")) {
                sqlFileds.append(l.fieldName + " datetime ");
            } else if (l.fieldType.contains("BIT")) {
                sqlFileds.append(l.fieldName + " " + l.fieldType.toLowerCase() + " ");
            } else {
                sqlFileds.append(l.fieldName + " " + l.fieldType.toLowerCase() + "(" + l.fieldLength + ") ");
            }
            // 修改stg表,字段类型
            if (!l.fieldType.contains("TEXT")) {
                stgSql.append(l.fieldName + " varchar(4000),");
            }
            if (l.isPrimarykey == 1) {
                pksql.append("" + l.fieldName + ",");
                sqlFileds.append("not null ,");
            } else {
                sqlFileds.append(",");
            }

        });
        stgSql.append("fi_updatetime DATETIME,fi_version varchar(50),fi_enableflag varchar(50)," +
                "fi_error_message varchar(250),fidata_batch_code varchar(50),fidata_flow_batch_code varchar(50), fi_sync_type varchar(50) DEFAULT '2',fi_verify_type varchar(50) DEFAULT '3'," + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50)");
        sqlFileds.insert(0,"fi_createtime DATETIME DEFAULT CURRENT_TIMESTAMP,");
        sqlFileds.append("fi_updatetime DATETIME,fi_version varchar(50),fidata_batch_code varchar(50)," + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "key" + " varchar(50)");
        String havePk = pksql.toString();
        sqlFileds.append(") ENGINE=OLAP DISTRIBUTED BY HASH(fi_createtime) BUCKETS 10 PROPERTIES(\"replication_num\" =\"1\");");
        stgSql.append(") ENGINE=OLAP DISTRIBUTED BY HASH(fi_createtime) BUCKETS 10 PROPERTIES(\"replication_num\" =\"1\");");
        sql.append(sqlFileds);
        String stg_sql1 = "";
        String stg_sql2 = "";
        String odsTableName = "";
        if (buildPhysicalTableDTO.whetherSchema) {
            odsTableName = buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName;
            stg_sql1 = sql.toString().replace("fi_tableName",    buildPhysicalTableDTO.tableName);
            stg_sql2 = stgSql.toString().replace("fi_tableName",  "stg_" + buildPhysicalTableDTO.tableName);
            stg_sql2 = "DROP TABLE IF EXISTS " + "stg_" + buildPhysicalTableDTO.tableName + ";" + stg_sql2;
        } else {
            odsTableName = "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName;
            stg_sql1 = sql.toString().replace("fi_tableName", "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName );
            stg_sql2 = stgSql.toString().replace("fi_tableName", "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
            stg_sql2 = "DROP TABLE IF EXISTS " + "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + ";" + stg_sql2;
        }
        List<String> sqlList = new ArrayList<>();
        //alter table Date add constraint PK_Date primary key(ID)
        sqlList.add(stg_sql1);
        sqlList.add(stg_sql2);
        return sqlList;
    }

    @Override
    public String queryTableNum(BuildPhysicalTableDTO buildPhysicalTableDTO) {
        return null;
    }

    @Override
    public String assemblySql(DataAccessConfigDTO config, SynchronousTypeEnum synchronousTypeEnum, String funcName, BuildNifiFlowDTO buildNifiFlow) {
        return null;
    }

    @Override
    public String prepareCallSql() {
        return null;
    }

    @Override
    public String queryNumbersField(BuildNifiFlowDTO dto, DataAccessConfigDTO config, String groupId) {
        return null;
    }

    @Override
    public String queryMdmNumbersField(BuildMdmNifiFlowDTO dto, AccessMdmConfigDTO config, String groupId) {
        return null;
    }

    @Override
    public String delMdmField(BuildMdmNifiFlowDTO dto, AccessMdmConfigDTO config, String groupId) {
        return null;
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
        // 前缀问题
        tablePk = tableName.substring(tableName.indexOf("_") + 1) + "key";
        StringBuilder pksql = new StringBuilder("UNIQUE KEY ( ");
        StringBuilder sqlFileds = new StringBuilder();
        StringBuilder stgSqlFileds = new StringBuilder();
        log.info("pg_dw建表字段信息:" + fieldList);
        fieldList.forEach((l) -> {
            if (l.fieldType.contains("FLOAT")) {
                sqlFileds.append(l.fieldEnName).append(" ").append(l.fieldType.toLowerCase()).append(", ");
            } else if (l.fieldType.contains("INT")) {
                sqlFileds.append(l.fieldEnName).append(" ").append(l.fieldType.toLowerCase()).append(", ");
            } else if (l.fieldType.contains("TEXT")) {
                sqlFileds.append(l.fieldEnName).append(" ").append(l.fieldType.toLowerCase()).append(", ");
                stgSqlFileds.append(l.fieldEnName).append(" ").append(l.fieldType.toLowerCase()).append(",");
            } else if (l.fieldType.toUpperCase().equals("DATE")) {
                sqlFileds.append(l.fieldEnName).append(" ").append(l.fieldType.toLowerCase()).append(", ");
            } else if (l.fieldType.toUpperCase().equals("TIME")) {
                sqlFileds.append(l.fieldEnName).append(" ").append(l.fieldType.toLowerCase()).append(", ");
            } else if (l.fieldType.contains("TIMESTAMP") || StringUtils.equals(l.fieldType.toUpperCase(), "DATETIME")) {
                sqlFileds.append(l.fieldEnName).append(" datetime, ");
            } else {
                sqlFileds.append(l.fieldEnName).append(" ").append(l.fieldType.toLowerCase()).append("(").append(l.fieldLength).append("), ");
            }
            // 修改stg表,字段类型
            if (!l.fieldType.contains("TEXT")) {
                stgSqlFileds.append("" + l.fieldEnName + " varchar(4000),");
            }
            if (l.isPrimaryKey == 1) {
                pksql.append("")
                        .append(l.fieldEnName)
                        .append(" ,");
            }

        });

        String sql1 = "CREATE TABLE " + modelPublishTableDTO.tableName + " ( ";
        //String associatedKey = associatedConditions(fieldList);
        String associatedKey = "";
        String sql2 = sqlFileds.toString() + associatedKey;
        sql2 += "fi_createtime DATETIME,fi_updatetime DATETIME";
        sql2 += ",fidata_batch_code varchar(50)";
        String sql3 = "";
        if (Objects.equals("", sql3)) {
            sql1 += sql2;
        } else {
            sql1 += sql2 + sql3;
        }
        sql1 += ") ";
        if (modelPublishTableDTO.synMode == 3) {
            String havePk = pksql.toString();
            if (havePk.length() != 14) {
                sql1 += havePk.substring(0, havePk.length() - 1) + ")";
            }
        }

        //创建表
        log.info("pg_dw建表语句" + sql1);
        //String stgTable = sql1.replaceFirst(tableName, "stg_" + tableName);
        String stgTable = "DROP TABLE IF EXISTS " + modelPublishTableDTO.prefixTempName + tableName + "; CREATE TABLE " + modelPublishTableDTO.prefixTempName + tableName + " (" + tablePk + " BIGINT IDENTITY(1,1) NOT NULL ," + stgSqlFileds.toString() + associatedKey + "fi_createtime DATETIME DEFAULT CURRENT_TIMESTAMP,fi_updatetime DATETIME,fi_enableflag varchar(50),fi_error_message text,fidata_batch_code varchar(50),fidata_flow_batch_code varchar(50), fi_sync_type varchar(50) DEFAULT '2',fi_verify_type varchar(50) DEFAULT '3');";
        //stgTable += "create index " + tableName + "enableflagsy on stg_" + tableName + " (fi_enableflag);";
        sqlList.add(stgTable);
        sqlList.add(sql1);
        return sqlList;
    }

    @Override
    public String queryNumbersFieldForTableServer(BuildNifiFlowDTO dto, DataAccessConfigDTO config, String groupId) {
        String querySql = "";
        String targetTableName = config.processorConfig.targetTableName;
        if (targetTableName.indexOf(".")>0){
            //去掉.前面的部分
            targetTableName = targetTableName.substring(targetTableName.indexOf('.') + 1);
        }
        querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers ,DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s') AS end_time," +
                "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," +
                "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + targetTableName;

        return querySql;
    }

    @Override
    public String getTotalSql(String sql, SynchronousTypeEnum synchronousTypeEnum) {
        return null;
    }

    @Override
    public void fieldFormatModification(DataAccessConfigDTO dto) {

    }

    @Override
    public String getEsqlAutoCommit() {
        return null;
    }
}
