package com.fisk.task.listener.postgre.datainput.impl;

import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.mdmconfig.AccessMdmConfigDTO;
import com.fisk.task.dto.mdmtask.BuildMdmNifiFlowDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.listener.postgre.datainput.IbuildTable;

import java.util.List;

public class BuildMysqlTableImpl implements IbuildTable {
    @Override
    public List<String> buildStgAndOdsTable(BuildPhysicalTableDTO buildPhysicalTableDTO) {
        return null;
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
