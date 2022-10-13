package com.fisk.common.service.dbBEBuild.dataservice.impl;

import com.fisk.common.service.dbBEBuild.dataservice.IBuildDataServiceSqlCommand;

import java.util.List;
import java.util.Map;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/10/12 13:52
 */
public class BuildDataServiceMysqlCommandImpl implements IBuildDataServiceSqlCommand {

    @Override
    public String buildPagingSql(String tableName, String fields, String orderBy, Integer pageIndex, Integer pageSize) {
        return null;
    }

    @Override
    public String buildPagingSql(String tableName, List<String> fields, String orderBy, Integer pageIndex, Integer pageSize) {
        return null;
    }

    @Override
    public String buildQueryCountSql(String tableName, String queryConditions) {
        return null;
    }

    @Override
    public String buildQuerySql(String tableName, String fields, String queryConditions) {
        return null;
    }

    @Override
    public String buildSingleInsertSql(String tableName, Map<String, Object> member) {
        return null;
    }

    @Override
    public String buildSingleUpdateSql(String tableName, Map<String, Object> member, String editConditions) {
        return null;
    }

    @Override
    public String buildUseExistTableFiled(String dbName, String tableName) {
       String sql = String.format("SELECT\n" +
                "\tTABLE_NAME AS originalTableName,\n" +
                "\tCOLUMN_NAME AS originalFieldName,\n" +
                "\tCOLUMN_COMMENT AS originalFieldDesc,\n" +
                "\t'' AS originalFramework \n" +
                "FROM\n" +
                "\tinformation_schema.`COLUMNS` \n" +
                "WHERE\n" +
                "\tTABLE_SCHEMA = '%s' \n" +
                "\tAND TABLE_NAME = '%s'", dbName, tableName);
       return  sql;
    }
}
