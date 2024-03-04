package com.fisk.task.listener.postgre.datainput.impl;

import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.mdmconfig.AccessMdmConfigDTO;
import com.fisk.task.dto.mdmtask.BuildMdmNifiFlowDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.postgre.datainput.IbuildTable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class BuildMysqlTableImpl implements IbuildTable {
    @Override
    public List<String> buildStgAndOdsTable(BuildPhysicalTableDTO buildPhysicalTableDTO) {
        log.info("保存版本号方法执行成功");
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS fi_tableName ( ");
        StringBuilder sqlFileds = new StringBuilder();
        StringBuilder pksql = new StringBuilder();
        StringBuilder stgSql = new StringBuilder("CREATE TABLE fi_tableName (fi_createtime DATETIME DEFAULT CURRENT_TIMESTAMP,");
        List<TableFieldsDTO> tableFieldsDTOS = buildPhysicalTableDTO.tableFieldsDTOS;
        List<String> pkFields = new ArrayList<>();
        //ods与stg类型不变,不然有的值,类型转换不来
        tableFieldsDTOS.forEach((l) -> {
            if (l.fieldType.contains("FLOAT")) {
                sqlFileds.append(l.fieldName).append(" ").append(l.fieldType.toLowerCase()).append(" ");
            } else if (l.fieldType.contains("INT")) {
                sqlFileds.append(l.fieldName).append(" ").append(l.fieldType.toLowerCase()).append(" ");
            } else if (l.fieldType.contains("TEXT")) {
                sqlFileds.append(l.fieldName).append(" ").append(l.fieldType.toLowerCase()).append(" ");
                stgSql.append(l.fieldName).append(" ").append(l.fieldType.toLowerCase()).append(",");
            } else if (l.fieldType.toUpperCase().equals("DATE")) {
                sqlFileds.append(l.fieldName).append(" ").append(l.fieldType.toLowerCase()).append(" ");
            } else if (l.fieldType.toUpperCase().equals("TIME")) {
                sqlFileds.append(l.fieldName).append(" ").append(l.fieldType.toLowerCase()).append(" ");
            } else if (l.fieldType.contains("TIMESTAMP") || StringUtils.equals(l.fieldType.toUpperCase(), "DATETIME")) {
                sqlFileds.append(l.fieldName).append(" datetime ");
            } else if (l.fieldType.contains("BIT")) {
                sqlFileds.append(l.fieldName).append(" ").append(l.fieldType.toLowerCase()).append(" ");
            } else {
                sqlFileds.append(l.fieldName).append(" ").append(l.fieldType.toLowerCase()).append("(").append(l.fieldLength).append(") ");
            }
            // 修改stg表,字段类型
            if (!l.fieldType.contains("TEXT")) {
                stgSql.append(l.fieldName).append(" varchar(4000),");
            }

            //是否主键
            if (l.isPrimarykey == 1) {
                pkFields.add(l.getFieldName());
                sqlFileds.append("not null ,");
            } else {
                sqlFileds.append(",");
            }

        });

        // MYSQL单一主键或复合主键均支持
        if (!CollectionUtils.isEmpty(pkFields)) {
            pksql.append(", PRIMARY KEY (");
            for (String pkField : pkFields) {
                pksql.append(pkField)
                        .append(",");

            }
            pksql.deleteCharAt(pksql.lastIndexOf(","));
            pksql.append(")");
        }else {
            pksql.append(" ");
        }


        stgSql.append("fi_updatetime DATETIME,fi_version varchar(50),fi_enableflag varchar(50)," + "fi_error_message varchar(250),fidata_batch_code varchar(50),fidata_flow_batch_code varchar(50), fi_sync_type varchar(50) DEFAULT '2',fi_verify_type varchar(50) DEFAULT '3',").append(buildPhysicalTableDTO.appAbbreviation).append("_").append(buildPhysicalTableDTO.tableName).append("key").append(" varchar(50) ");
        sqlFileds.insert(0, "fi_createtime DATETIME DEFAULT CURRENT_TIMESTAMP,");
        sqlFileds.append("fi_updatetime DATETIME,fi_version varchar(50),fidata_batch_code varchar(50),")
                .append(buildPhysicalTableDTO.appAbbreviation)
                .append("_")
                .append(buildPhysicalTableDTO.tableName)
                .append("key")
                .append(" varchar(50)  DEFAULT (UUID())")
                .append(pksql);

        //补上字段括号
        sqlFileds.append(")");
        stgSql.append(")");
        sql.append(sqlFileds);
        //目标表建表语句
        String stg_sql1 = "";
        //临时stg表建表语句
        String stg_sql2 = "";
        stg_sql1 = sql.toString().replace("fi_tableName", "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
        stg_sql2 = stgSql.toString().replace("fi_tableName", "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
        stg_sql2 = "DROP TABLE IF EXISTS " + "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + ";" + stg_sql2;

        List<String> sqlList = new ArrayList<>();
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
    public String prepareCallSqlForDoris(String version, int type) {
        return null;
    }

    @Override
    public String queryNumbersField(BuildNifiFlowDTO dto, DataAccessConfigDTO config, String groupId) {
        List<String> stgAndTableName = getStgAndTableName(config.processorConfig.targetTableName);
        if (config.processorConfig.targetTableName.contains("\\.")) {
        }
        String querySql = "";
        if (Objects.equals(dto.type, OlapTableEnum.WIDETABLE) || Objects.equals(dto.type, OlapTableEnum.KPI)) {
//            querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers , CURRENT_TIMESTAMP() as end_time," + "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," + "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + config.processorConfig.targetTableName;
            querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, 0 as numbers , CURRENT_TIMESTAMP() as end_time," + "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," + "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType;";
        } else {
            if (Objects.equals(dto.synchronousTypeEnum, SynchronousTypeEnum.TOPGODS)) {
//                querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, count(*) as numbers , CURRENT_TIMESTAMP() as end_time," + "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," + "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType  from " + stgAndTableName.get(1) + " where  fidata_batch_code='${fidata_batch_code}'";
                querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, 0 as numbers , CURRENT_TIMESTAMP() as end_time," + "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," + "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType;";
            } else {
                querySql = "select '${kafka.topic}' as topic," + dto.id + " as table_id, " + dto.type.getValue() + " as table_type, 0 as numbers , CURRENT_TIMESTAMP() as end_time," + "'${pipelStageTraceId}' as pipelStageTraceId,'${pipelJobTraceId}' as pipelJobTraceId,'${pipelTaskTraceId}' as pipelTaskTraceId," + "'${pipelTraceId}' as pipelTraceId,'${topicType}' as topicType;";
            }

        }
        return querySql;
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
        return null;
    }

    @Override
    public List<String> buildDwStgAndOdsTable(ModelPublishTableDTO modelPublishTableDTO) {
        return null;
    }

    @Override
    public String queryNumbersFieldForTableServer(BuildNifiFlowDTO dto, DataAccessConfigDTO config, String groupId) {
        String querySql = "";
        String targetTableName = config.processorConfig.targetTableName;
        if (targetTableName.indexOf(".") > 0) {
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

    @Override
    public List<String> buildDorisDimTables(ModelPublishTableDTO modelPublishTableDTO) {
        return null;
    }

    @Override
    public List<String> buildDorisDimTablesWithoutSystemFields(ModelPublishTableDTO modelPublishTableDTO) {
        return null;
    }

    @Override
    public List<String> buildDorisFactTables(ModelPublishTableDTO modelPublishTableDTO) {
        return null;
    }

    @Override
    public List<String> buildDorisFactTablesWithoutSystemFields(ModelPublishTableDTO modelPublishTableDTO) {
        return null;
    }

    @Override
    public List<String> buildDorisaAggregateTables(ModelPublishTableDTO modelPublishTableDTO) {
        return null;
    }
}
