package com.fisk.common.service.dbBEBuild.common.impl;

import com.fisk.common.service.dbBEBuild.common.IBuildCommonSqlCommand;
import com.fisk.common.service.dbBEBuild.common.dto.DruidFieldInfoDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public class BuildCommonMySqlCommand implements IBuildCommonSqlCommand {

    @Override
    public String buildAllDbSql() {
        return "SELECT `SCHEMA_NAME` as dbname FROM `information_schema`.`SCHEMATA`;";
    }

    @Override
    public List<DruidFieldInfoDTO> druidAnalyseSql(String sql) {
        return null;
    }

    @Override
    public String buildColumnInfo(String schemaName, String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append("TABLE_NAME AS table_name,");
        str.append("COLUMN_NAME AS column_name,");
        str.append("DATA_TYPE AS data_type,");
        str.append("TABLE_SCHEMA AS schema_name,");
        str.append("CHARACTER_MAXIMUM_LENGTH AS column_length ");
        str.append("FROM ");
        str.append("information_schema.COLUMNS ");
        str.append("WHERE ");
        str.append("TABLE_SCHEMA='");
        str.append(schemaName);
        str.append("' ");
        str.append("AND ");
        str.append("TABLE_NAME in");
        str.append("(");
        str.append(tableName);
        str.append(")");
        return str.toString();
    }

}
