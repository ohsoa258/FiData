package com.fisk.common.service.dbBEBuild.dataservice.impl;

import com.fisk.common.service.dbBEBuild.dataservice.IBuildDataServiceSqlCommand;
import org.apache.commons.lang.StringUtils;

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
    public String buildPagingSql(String tableName, String fields, String orderBy, Integer pageIndex, Integer pageSize,String where) {
        StringBuilder str = new StringBuilder();

        return str.toString();
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
    public String buildUseExistTableFiled(String tableFramework, String tableRelName) {
        // tableFramework 用不到，因为没有mysql没有schema的概念
        String sql = String.format("SELECT\n" +
                "\tTABLE_NAME AS tableName,\n" +
                "\tCOLUMN_NAME AS fieldName,\n" +
                "\tCOLUMN_COMMENT AS fieldDesc \n" +
                "FROM\n" +
                "\tinformation_schema.`COLUMNS` \n" +
                "WHERE\n" +
                "\tTABLE_NAME = '%s'", tableRelName);
        return sql;
    }

    @Override
    public String buildSchemaConStr(String schema, String conStr) {
        return conStr;
    }

}
