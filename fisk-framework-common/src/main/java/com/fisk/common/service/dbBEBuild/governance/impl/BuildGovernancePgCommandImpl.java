package com.fisk.common.service.dbBEBuild.governance.impl;

import com.fisk.common.service.dbBEBuild.governance.IBuildGovernanceSqlCommand;
import com.fisk.common.service.dbBEBuild.governance.dto.KeyValueMapDto;
import com.google.common.base.Joiner;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @author dick
 */
public class BuildGovernancePgCommandImpl implements IBuildGovernanceSqlCommand {

    @Override
    public String buildPagingSql(String tableName, String fields, String orderBy, Integer pageIndex, Integer pageSize) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append(fields);
        str.append(" FROM ");
        str.append(tableName);
        if (StringUtils.isNotEmpty(orderBy)) {
            str.append(" ORDER BY " + orderBy);
        }
        // OFFSET 从0开始
        str.append(String.format(" LIMIT %s OFFSET %s ", pageSize, (pageIndex - 1) * pageSize));
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
        }
        str.append(String.format(" LIMIT %s OFFSET %s ", pageSize, (pageIndex - 1) * pageSize));
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
        String sql = "SELECT\n" +
                "\tschemata.\"schema_name\" AS \"schema\" \n" +
                "FROM\n" +
                "\tinformation_schema.schemata AS schemata\n" +
                "\tLEFT JOIN pg_tables tables ON schemata.\"schema_name\" = tables.schemaname \n" +
                "WHERE\n" +
                "\t\tNULLIF ( tables.tablename, '' ) != ''  \n" +
                "\tAND schemata.\"schema_name\" NOT IN ( 'pg_catalog', 'information_schema' ) \n" +
                "GROUP BY\n" +
                "\t\"schema_name\" \n" +
                "ORDER BY\n" +
                "\t\"schema_name\"";
        return sql;
    }

    @Override
    public String buildQuerySchema_TableSql(List<String> schemaList) {
        String sql = "";
        if (CollectionUtils.isEmpty(schemaList)) {
            return sql;
        }
        String schemaParams = "'" + StringUtils.join(schemaList, "','") + "'".toLowerCase();
        sql = String.format("SELECT\n" +
                "\tsc.table_schema AS \"schema\",\n" +
                "\tsc.TABLE_NAME AS tablename \n" +
                "FROM\n" +
                "\tinformation_schema.COLUMNS sc\n" +
                "\tLEFT JOIN pg_tables tables ON sc.table_schema = tables.schemaname \n" +
                "\tAND sc.\"table_name\" = tables.tablename \n" +
                "\tWHERE-- PG SQL区分大小写，转小写查询\n" +
                "\tLOWER ( table_schema ) IN ( %s ) \n" +
                "\tAND NULLIF ( tables.tablename, '' ) != '' -- 此条件可过滤视图\n" +
                "\t\n" +
                "GROUP BY\n" +
                "\ttable_schema,\n" +
                "TABLE_NAME \n" +
                "ORDER BY\n" +
                "\t\"schema\",\n" +
                "\ttablename", schemaParams);
        return sql;
    }

    @Override
    public String buildQuerySchema_Table_FieldSql(List<String> schemaList, List<String> tableNameList,
                                                  List<KeyValueMapDto> fieldNameList) {
        String sql = "";
        if (CollectionUtils.isEmpty(schemaList)) {
            return sql;
        }
        String schemaParams = "'" + StringUtils.join(schemaList, "','") + "'".toLowerCase();

        String tableParamsSql = "";
        if (CollectionUtils.isNotEmpty(tableNameList)) {
            String tableParams = "'" + StringUtils.join(tableNameList, "','") + "'".toLowerCase();
            tableParamsSql = String.format("\t\t\t\tAND LOWER ( TABLE_NAME ) IN ( %s )\n", tableParams);
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
                    fieldParams += " LOWER ( COLUMN_NAME ) = '" + value + "' OR";
                } else if (operator.contains("包含")) {
                    fieldParams += " COLUMN_NAME ILIKE '%" + value + "%' OR";
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
                "\tSELECT\n" +
                "\t\ttable_schema AS \"schema\",-- 模式\n" +
                "\t\tTABLE_NAME AS tablename,-- 表名称\n" +
                "\t\tCOLUMN_NAME AS fieldname,-- 字段名称\n" +
                "\t\tudt_name AS fieldtype,-- 字段类型\n" +
                "\t\tCOALESCE ( character_maximum_length, numeric_precision,- 1 ) AS fieldlength,-- 字段长度\n" +
                "\t\t(\n" +
                "\t\tSELECT\n" +
                "\t\t\tCOALESCE ( NULLIF ( pg_catalog.col_description ( C.oid, sc.ordinal_position :: INT ) , '' ), '' ) \n" +
                "\t\tFROM\n" +
                "\t\t\tpg_catalog.pg_class C \n" +
                "\t\tWHERE\n" +
                "\t\t\tC.oid = ( SELECT ( '\"' || sc.TABLE_NAME || '\"' ) :: REGCLASS :: OID ) \n" +
                "\t\t\tAND C.relname = sc.TABLE_NAME \n" +
                "\t\t) AS fieldcomment,-- 字段注释\n" +
                "\t\t(\n" +
                "\t\tCASE\n" +
                "\t\t\t\t\n" +
                "\t\t\t\tWHEN (\n" +
                "\t\t\t\tSELECT COUNT\n" +
                "\t\t\t\t\t( pg_constraint.* ) \n" +
                "\t\t\t\tFROM\n" +
                "\t\t\t\t\tpg_constraint\n" +
                "\t\t\t\t\tINNER JOIN pg_class ON pg_constraint.conrelid = pg_class.oid\n" +
                "\t\t\t\t\tINNER JOIN pg_attribute ON pg_attribute.attrelid = pg_class.oid \n" +
                "\t\t\t\t\tAND pg_attribute.attnum = ANY ( pg_constraint.conkey )\n" +
                "\t\t\t\t\tINNER JOIN pg_type ON pg_type.oid = pg_attribute.atttypid \n" +
                "\t\t\t\tWHERE\n" +
                "\t\t\t\t\tpg_class.relname = sc.TABLE_NAME \n" +
                "\t\t\t\t\tAND pg_constraint.contype = 'p' \n" +
                "\t\t\t\t\tAND pg_attribute.attname = sc.COLUMN_NAME \n" +
                "\t\t\t\t\t) > 0 THEN\n" +
                "\t\t\t\t\t'YES' ELSE'NO' \n" +
                "\t\t\t\tEND \n" +
                "\t\t\t\t) AS fieldisprimarykey,-- 是否是主键\n" +
                "\t\t\tCASE\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\tWHEN column_default = '''''::character varying' THEN\n" +
                "\t\t\t\t\t'Empty String' \n" +
                "\t\t\t\t\tWHEN column_default = 'NULL::character varying' THEN\n" +
                "\t\t\t\t\t'NULL' ELSE COALESCE ( NULLIF ( TRIM ( column_default ), '' ), '' ) \n" +
                "\t\t\t\tEND AS fielddefaultvalue,-- 字段默认值\n" +
                "\t\t\t\tis_nullable AS fieldisallownull -- 字段是否允许为空\n" +
                "\t\t\t\t\n" +
                "\t\t\tFROM\n" +
                "\t\t\t\tinformation_schema.COLUMNS sc \n" +
                "\t\t\tWHERE\n" +
                "-- schema查询条件，在PGSQL中区分大小写\n" +
                "\t\t\t\tLOWER ( table_schema ) IN ( %s )\n" +
                "\t\t\t\t\n" +
                "-- table查询条件，在PGSQL中区分大小写\n" +
                "%s" +
                "\t\t\t\t\n" +
                "-- field查询条件，在PGSQL中区分大小写，ILIKE PGSQL独有语法，不区分大小写\n" +
                "%s" +
                "\t\t\t\t\n" +
                "\t\t\t) T \n" +
                "\t\tGROUP BY\n" +
                "\t\t\t\"schema\",\n" +
                "\t\t\ttablename,\n" +
                "\t\t\tfieldname,\n" +
                "\t\t\tfieldcomment,\n" +
                "\t\t\tfieldtype,\n" +
                "\t\t\tfieldlength,\n" +
                "\t\t\tfieldisprimarykey,\n" +
                "\t\t\tfielddefaultvalue,\n" +
                "\t\t\tfieldisallownull \n" +
                "\t\tORDER BY\n" +
                "\t\t\"schema\",\n" +
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
