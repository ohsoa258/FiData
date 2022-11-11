package com.fisk.common.service.dbBEBuild.common.impl;

import com.fisk.common.service.dbBEBuild.common.IBuildCommonSqlCommand;
import com.fisk.common.service.dbBEBuild.common.dto.DruidFieldInfoDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public class BuildCommonOracleCommand implements IBuildCommonSqlCommand {

    @Override
    public String buildAllDbSql() {
        return "select OWNER as dbname from all_tables where OWNER not in ('SYS','SYSTEM')";
    }

    @Override
    public List<DruidFieldInfoDTO> druidAnalyseSql(String sql) {
        return null;
    }

    @Override
    public String buildColumnInfo(String dbName, String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append("OWNER AS schema,");
        str.append("TABLE_NAME AS table_name,");
        str.append("COLUMN_NAME AS column_name,");
        str.append("DATA_TYPE AS data_type,");
        str.append("DATA_LENGTH AS column_length,");
        str.append("DATA_PRECISION AS data_precision,");
        str.append("DATA_SCALE AS data_scale ");
        str.append("FROM ");
        str.append("ALL_TAB_COLUMNS ");
        str.append("WHERE ");
        str.append("TABLE_NAME IN");
        str.append("(");
        str.append(tableName);
        str.append(")");

        return str.toString();
    }

}
