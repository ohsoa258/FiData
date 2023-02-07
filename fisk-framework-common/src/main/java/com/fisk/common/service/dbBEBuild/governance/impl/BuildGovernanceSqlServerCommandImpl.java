package com.fisk.common.service.dbBEBuild.governance.impl;

import com.fisk.common.service.dbBEBuild.governance.IBuildGovernanceSqlCommand;
import com.fisk.common.service.dbBEBuild.governance.dto.KeyValueMapDto;
import com.google.common.base.Joiner;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author dick
 */
public class BuildGovernanceSqlServerCommandImpl implements IBuildGovernanceSqlCommand {

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
        // OFFSET 从0开始
        str.append(" OFFSET " + (pageIndex - 1) * pageSize + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY;");
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
        str.append(" OFFSET " + (pageIndex - 1) * pageSize + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY;");
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
        str.append(" WHERE 1=1 ");
        str.append(editConditions);
        return str.toString();
    }

    @Override
    public String buildQuerySchemaSql() {
        String sql = "SELECT DISTINCT\n" +
                "\t[schema] \n" +
                "FROM\n" +
                "\t( SELECT schema_name( schema_id ) AS [schema] FROM sys.tables ) AS tab \n" +
                "ORDER BY\n" +
                "\t[schema]";
        return sql;
    }

    @Override
    public String buildQuerySchema_TableSql(List<String> schemaList) {
        String sql = "";
        if (CollectionUtils.isEmpty(schemaList)) {
            return sql;
        }
        String schemaParams = "'" + StringUtils.join(schemaList, "','") + "'";
        sql = String.format("SELECT schema_name( tb.schema_id ) AS [schema],\n" +
                "d.name AS tablename\n" +
                "FROM\n" +
                "\tsyscolumns a LEFT JOIN systypes b ON a.xtype= b.xusertype\n" +
                "\tINNER JOIN sysobjects d ON a.id= d.id AND d.xtype= 'U' AND d.name<> 'dtproperties'\n" +
                "\tLEFT JOIN sys.tables tb ON tb.name= d.name\n" +
                "\tLEFT JOIN syscomments e ON a.cdefault= e.id\n" +
                "\tLEFT JOIN sys.extended_properties g ON a.id= g.major_id AND a.colid= g.minor_id\n" +
                "\tLEFT JOIN sys.extended_properties f ON d.id= f.major_id AND f.minor_id = 0\n" +
                "WHERE\n" +
                "\tschema_name( tb.schema_id ) IN ( %s )\n" +
                "GROUP BY\n" +
                "\tschema_name( tb.schema_id ),\n" +
                "\td.name\n" +
                "ORDER BY\n" +
                "\t[schema],\n" +
                "\ttablename", schemaParams);
        return sql;
    }

    @Override
    public String buildQuerySchema_Table_FieldSql(List<String> schemaList, List<String> tableNameList, List<KeyValueMapDto> fieldNameList) {
        String sql = "";
        if (CollectionUtils.isEmpty(schemaList)) {
            return sql;
        }
        String schemaParams = "'" + StringUtils.join(schemaList, "','") + "'";

        String tableParamsSql = "";
        if (CollectionUtils.isNotEmpty(tableNameList)) {
            String tableParams = "'" + StringUtils.join(tableNameList, "','") + "'";
            tableParamsSql = "\t\t\tAND d.name IN ( " + tableParams + " )\n";
        }

        String fieldParamsSql = "";
        if (CollectionUtils.isNotEmpty(fieldNameList)) {
            String fieldParams = "";
            for (KeyValueMapDto item : fieldNameList) {
                // 字段名称为空则跳过，数据库表字段也不允许为空
                if (item.getKey() == null || StringUtils.isEmpty(item.getKey().toString())
                        || item.getValue() == null || StringUtils.isEmpty(item.getValue().toString())) {
                    continue;
                }
                String operator = item.getKey().toString();
                String value = item.getValue().toString().toLowerCase();
                if (operator.equals("等于")) {
                    fieldParams += " a.name = '" + value + "' OR";
                } else if (operator.contains("包含")) {
                    fieldParams += " a.name LIKE '%" + value + "%' OR";
                }
            }
            if (StringUtils.isNotEmpty(fieldParams)) {
                fieldParams = fieldParams.substring(0, fieldParams.length() - 2);
                fieldParamsSql = "\t\t\t\tAND ( " + fieldParams + " )\n";
            }
        }
        sql = String.format("SELECT\n" +
                "\t* \n" +
                "FROM\n" +
                "\t(\n" +
                "\t\tSELECT schema_name( tb.schema_id ) AS [schema],-- 模式\n" +
                "\t\td.name AS tablename,-- 表名称\n" +
                "\t\ta.name AS fieldname,-- 字段名称\n" +
                "\t\tb.name AS fieldtype,-- 字段类型\n" +
                "\t\tCOLUMNPROPERTY( a.id, a.name, 'PRECISION' ) AS fieldlength,-- 字段长度\n" +
                "\t\tisnull( g.[value], '' ) AS fieldcomment,-- 字段注释\n" +
                "\t\tCASE WHEN EXISTS (\n" +
                "\t\t\t\tSELECT 1 FROM sysobjects WHERE xtype = 'PK' AND name IN (\n" +
                "\t\t\t\t\t SELECT name \n" +
                "\t\t\t\tFROM\n" +
                "\t\t\t\t\tsysindexes \n" +
                "\t\t\t\tWHERE\n" +
                "\t\t\t\t\tindid IN ( \n" +
                "\t\t\t\t\t\t SELECT indid FROM sysindexkeys WHERE id = a.id AND colid = a.colid\n" +
                "\t\t\t\t\t ) \n" +
                "\t\t\t\t) \n" +
                "\t\t\t\t)  THEN 'YES' ELSE 'NO' \n" +
                "\t\t\t\tEND AS fieldisprimarykey,-- 是否是主键\n" +
                "\t\t\tisnull( e.text, '' ) AS fielddefaultvalue,-- 字段默认值\n" +
                "\t\t\tCASE WHEN a.isnullable= 1 THEN 'YES' ELSE 'NO' \n" +
                "\t\t\tEND AS fieldisallownull  -- 字段是否允许为空\n" +
                "\t\t\t\n" +
                "\t\tFROM\n" +
                "\t\t\tsyscolumns a  LEFT JOIN systypes b ON a.xtype= b.xusertype\n" +
                "\t\t\t INNER JOIN sysobjects d ON a.id= d.id AND d.xtype= 'U' AND d.name<> 'dtproperties'\n" +
                "\t\t\tLEFT JOIN sys.tables tb ON tb.name= d.name\n" +
                "\t\t\t LEFT JOIN syscomments e ON a.cdefault= e.id\n" +
                "\t\t\t LEFT JOIN sys.extended_properties g ON a.id= g.major_id AND a.colid= g.minor_id\n" +
                "\t\t\t LEFT JOIN sys.extended_properties f ON d.id= f.major_id AND f.minor_id = 0  " +
                "-- schema查询条件\n" +
                "\t\t\t WHERE schema_name( tb.schema_id ) IN ( %s )\n" +
                "\t\t\t\n" +
                "-- table查询条件\n" +
                "%s" +
                "-- field查询条件\n" +
                "%s" +
                "\t\t\t\n" +
                "\t\t) T \n" +
                "\tGROUP BY\n" +
                "\t\t[schema],\n" +
                "\t\ttablename,\n" +
                "\t\tfieldname,\n" +
                "\t\tfieldcomment,\n" +
                "\t\tfieldtype,\n" +
                "\t\tfieldlength,\n" +
                "\t\tfieldisprimarykey,\n" +
                "\t\tfielddefaultvalue,\n" +
                "\t\tfieldisallownull \n" +
                "\tORDER BY\n" +
                "\t[schema],\n" +
                "\ttablename", schemaParams, tableParamsSql, fieldParamsSql);
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
