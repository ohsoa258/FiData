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
public class BuildDataServicePgCommandImpl implements IBuildDataServiceSqlCommand {

    @Override
    public String buildPagingSql(String tableName,String fields, String orderBy, Integer pageIndex, Integer pageSize) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append(fields);
        str.append(" FROM ");
        str.append(tableName);
        if (StringUtils.isNotEmpty(orderBy)) {
            str.append(" ORDER BY " + orderBy);
        }
        str.append(" LIMIT " + pageSize + " OFFSET " + pageIndex);
        return str.toString();
    }

    @Override
    public String buildPagingSql(String tableName,List<String> fields, String orderBy, Integer pageIndex, Integer pageSize) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append(Joiner.on(",").join(fields));
        str.append(" FROM ");
        str.append(tableName);
        if (StringUtils.isNotEmpty(orderBy)) {
            str.append(" ORDER BY " + orderBy);
        }
        str.append(" LIMIT " + pageSize + " OFFSET " + pageIndex);
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
    public  String buildQuerySql(String tableName, String fields, String queryConditions) {
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
    public String buildUseExistTableFiled(String dbName, String tableName) {
       String sql = String.format("SELECT c.relname as originalTableName,a.attname as originalFieldName,col_description(a.attrelid,a.attnum) as originalFieldDesc,'' AS originalFramework \n" +
                "FROM pg_class as c,pg_attribute as a inner join pg_type on pg_type.oid = a.atttypid\n" +
                "where c.relname in  (SELECT tablename FROM pg_tables ) and a.attrelid = c.oid and a.attnum>0\n" +
                "and c.relname ='%s'", tableName);
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
