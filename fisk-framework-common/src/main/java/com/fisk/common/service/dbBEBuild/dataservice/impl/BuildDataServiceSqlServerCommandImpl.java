package com.fisk.common.service.dbBEBuild.dataservice.impl;

import com.fisk.common.service.dbBEBuild.dataservice.IBuildDataServiceSqlCommand;
import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author dick
 */
public class BuildDataServiceSqlServerCommandImpl implements IBuildDataServiceSqlCommand {

    @Override
    public String buildPagingSql(String tableName, String fields, String orderBy, Integer pageIndex, Integer pageSize) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append(fields);
        str.append(" FROM ");
        str.append(tableName);
        if (StringUtils.isNotEmpty(orderBy)) {
            str.append(" ORDER BY " + orderBy);
        } else {
            str.append(" ORDER BY 1");
        }
        str.append(" OFFSET " + pageIndex + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY;");
        return str.toString();
    }

    @Override
    public String buildPagingSql(String tableName, List<String> fields, String orderBy, Integer pageIndex, Integer pageSize) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append(Joiner.on(",").join(fields));
        str.append(" FROM ");
        str.append(tableName);
        if (StringUtils.isNotEmpty(orderBy)) {
            str.append(" ORDER BY " + orderBy);
        } else {
            str.append(" ORDER BY 1");
        }
        str.append(" OFFSET " + pageIndex + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY;");
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
        StringBuilder str = new StringBuilder();
        str.append(" SELECT ");
        if (StringUtils.isNotEmpty(fields)) {
            str.append(fields);
        } else {
            str.append(" * ");
        }
        str.append(" FROM " + tableName);
        str.append(" WHERE 1=1 " + queryConditions);
        return str.toString();
    }

    @Override
    public String buildSingleInsertSql(String tableName, Map<String, Object> member) {
        StringBuilder str = new StringBuilder();
        str.append("INSERT INTO ");
        str.append(tableName);
        str.append("(");
        String column = singleInsertSql(member, 0);
        str.append(column);
        str.append(")");
        str.append("VALUES(");
        String data = singleInsertSql(member, 1);
        str.append(data);
        str.append(")");
        return str.toString();
    }

    @Override
    public String buildSingleUpdateSql(String tableName, Map<String, Object> member, String editConditions) {
        StringBuilder str = new StringBuilder();
        str.append("UPDATE ");
        str.append(tableName);
        str.append(" SET ");
        String column = singleUpdateSql(member);
        str.append(column);
        str.append(" WHERE ");
        str.append(editConditions);
        return str.toString();
    }

    @Override
    public String buildUseExistTableFiled(String tableFramework, String tableRelName) {
        String sql = String.format("SELECT\n" +
                "\td.name AS tableName,\n" +
                "\ta.name AS fieldName,\n" +
                "\tisnull( g.[value], '' ) AS fieldDesc \n" +
                "FROM\n" +
                "\tsyscolumns a\n" +
                "\tLEFT JOIN systypes b ON a.xusertype= b.xusertype\n" +
                "\tINNER JOIN sysobjects d ON a.id= d.id \n" +
                "\tAND d.xtype= 'U' \n" +
                "\tAND d.name<> 'dtproperties'\n" +
                "\tLEFT JOIN sys.tables tb ON tb.name= d.name\n" +
                "\tLEFT JOIN syscomments e ON a.cdefault= e.id\n" +
                "\tLEFT JOIN sys.extended_properties g ON a.id= g.major_id \n" +
                "\tAND a.colid= g.minor_id\n" +
                "\tLEFT JOIN sys.extended_properties f ON d.id= f.major_id \n" +
                "\tAND f.minor_id= 0 \n" +
                "WHERE\n" +
                "\td.name = '%s'", tableRelName);
        if (StringUtils.isNotEmpty(tableFramework)) {
            sql += String.format(" AND schema_name( tb.schema_id ) = '%s' ", tableFramework);
        }
        return sql;
    }

    /**
     * 单条新增SQL语句
     *
     * @param member
     * @param type
     * @return
     */
    private static String singleInsertSql(Map<String, Object> member, int type) {
        List<String> columnList = new ArrayList<>();
        Iterator iter = member.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = entry.getKey().toString();
            //获取列名
            if (type == 0) {
                columnList.add(name);
            }
            //拼接value
            else {
                if (StringUtils.isEmpty(entry.getValue() == null ? "" : entry.getValue().toString())) {
                    columnList.add("null");
                } else {
                    columnList.add("'" + entry.getValue().toString() + "'");
                }
            }
        }
        return Joiner.on(",").join(columnList);
    }

    /**
     * 单条修改SQL语句
     *
     * @param member
     * @return
     */
    private static String singleUpdateSql(Map<String, Object> member) {
        List<String> columnList = new ArrayList<>();
        Iterator iter = member.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = entry.getKey().toString();
            String value = "";
            if (StringUtils.isEmpty(entry.getValue() == null ? "" : entry.getValue().toString())) {
                value = "null";
            } else {
                value = "'" + entry.getValue().toString() + "'";
            }
            String result = name + "=" + value;
            columnList.add(result);
        }
        return Joiner.on(",").join(columnList);
    }
}
