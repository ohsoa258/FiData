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
public class BuildDataServiceDaMengCommandImpl implements IBuildDataServiceSqlCommand {

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
        str.append("SELECT ");
        str.append(fields);
        str.append(" FROM ");
        str.append(tableName);
        if (StringUtils.isNotEmpty(where)) {
            str.append(" WHERE 1=1 " + where);
        }
        if (StringUtils.isNotEmpty(orderBy)) {
            str.append(" ORDER BY " + orderBy);
        }
//        str.append(" LIMIT " + pageSize + " OFFSET " + pageIndex);
        // OFFSET 从0开始
        str.append(String.format(" LIMIT %s,%s ", (pageIndex -1)*pageSize, pageIndex*pageSize));
        return str.toString();
    }

    @Override
    public String buildQueryCountSql(String tableName, String queryConditions) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT COUNT(*) AS totalNum FROM ");
        str.append(tableName);
        str.append(" WHERE 1=1 ");
        if (!StringUtils.isEmpty(queryConditions)) {
            str.append(queryConditions);
        }
        return str.toString();
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
        String sql = String.format("SELECT\n" +
                "\tTABLE_NAME AS tableName,\n" +
                "\tCOLUMN_NAME AS fieldName,\n" +
                "\tCOMMENTS AS fieldDesc \n" +
                "FROM\n" +
                "\tuser_col_comments\n" +
                "WHERE\n" +
                "\tTable_Name = '%s'", tableRelName);
        return sql;
    }

    @Override
    public String buildSchemaConStr(String schema, String conStr) {
        return conStr;
    }

}
