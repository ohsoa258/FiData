package com.fisk.common.service.dbBEBuild.common.impl;

import com.fisk.common.service.dbBEBuild.common.IBuildCommonSqlCommand;
import com.fisk.common.service.dbBEBuild.common.dto.DruidFieldInfoDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public class BuildCommonPgSqlCommand implements IBuildCommonSqlCommand {

    @Override
    public String buildAllDbSql() {
        return "select datname as dbname from pg_database;";
    }

    @Override
    public List<DruidFieldInfoDTO> druidAnalyseSql(String sql) {
        return null;
    }

    @Override
    public String buildColumnInfo(String schemaName, String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append("TABLE_SCHEMA AS schema_name,");
        str.append("TABLE_NAME AS table_name,");
        str.append("COLUMN_NAME AS column_name,");
        str.append("CHARACTER_MAXIMUM_LENGTH AS column_length,");
        str.append("UDT_NAME AS data_type ");
        str.append("FROM ");
        str.append("INFORMATION_SCHEMA.COLUMNS ");
        str.append("WHERE ");
        str.append("TABLE_SCHEMA NOT IN");
        str.append("(");
        str.append("'pg_catalog',");
        str.append("'information_schema'");
        str.append(") ");
        str.append("AND ");
        str.append("TABLE_NAME IN");
        str.append("(");
        str.append(tableName);
        str.append(") ");
        str.append("AND TABLE_SCHEMA='");
        str.append(schemaName);
        str.append("'");

        return str.toString();
    }

}
