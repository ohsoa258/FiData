package com.fisk.datamodel.utils.sql;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.datamodel.entity.dimension.DimensionPO;
import com.fisk.datamodel.entity.fact.FactPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Slf4j
public class ModelPgSqlUtils {

    /**
     * @param conn
     * @return
     */
    public static List<TableNameDTO> getTableName(Connection conn) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            // 获取指定数据库所有表
            rs = stmt.executeQuery(buildQueryTableBySchemaSql());
            List<TableNameDTO> list = new ArrayList<>();
            while (rs.next()) {
                TableNameDTO tablePyhName = new TableNameDTO();
                tablePyhName.tableName = rs.getString("schemaname") + "." + rs.getString("tablename");
                list.add(tablePyhName);
            }
            return list;
        } catch (SQLException e) {
            log.error("【pgSql获取库中所有表失败】,{}", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
            AbstractCommonDbHelper.closeStatement(stmt);
        }
    }

    /**
     * 获取指定schema下所有表名sql
     *
     * @return
     */
    private static String buildQueryTableBySchemaSql() {
        StringBuilder str = new StringBuilder();
        str.append("select ");
        str.append("tablename,schemaname ");
        str.append("from ");
        str.append("pg_tables ");
        str.append("where ");
        str.append("schemaname not in ");
        str.append("(");
        str.append("'pg_catalog','information_schema'");
        str.append(")");
        str.append("ORDER BY ");
        str.append("tablename,schemaname;");

        return str.toString();
    }

    /**
     * 获取表字段信息
     *
     * @param conn
     * @param tableName
     * @return
     */
    public static List<TableColumnDTO> getTableColumnName(Connection conn, String tableName) {
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(buildSelectColumnInfo(tableName));
            List<TableColumnDTO> tableStructures = new ArrayList<>();
            while (rs.next()) {
                TableColumnDTO tableStructure = new TableColumnDTO();
                tableStructure.fieldName = rs.getString("column_name");
                tableStructure.fieldType = rs.getString("udt_name");
                if (rs.getString("character_maximum_length") == null) {
                    if (!StringUtils.isEmpty(rs.getString("numeric_precision"))) {
                        tableStructure.fieldLength = Integer.parseInt(rs.getString("numeric_precision"));
                    }
                } else {
                    tableStructure.fieldLength = Integer.parseInt(rs.getString("character_maximum_length"));
                }

                tableStructures.add(tableStructure);
            }
            return tableStructures;
        } catch (SQLException e) {
            log.error("【pg数据库获取表字段信息失败:】,{}", e);
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return null;
    }

    public static String buildSelectColumnInfo(String tableName) {
        String[] names = null;
        if (tableName.contains(".")) {
            names = tableName.split("\\.");
            tableName = names[1];
        }
        StringBuilder str = new StringBuilder();

        str.append("SELECT ");
        str.append("udt_name,");
        str.append("column_name,");
        str.append("character_maximum_length,");
        str.append("numeric_precision ");
        str.append("FROM ");
        str.append("information_schema.COLUMNS ");
        str.append("WHERE ");
        if (names != null) {
            str.append("LOWER (TABLE_SCHEMA) = LOWER");
            str.append("('");
            str.append(names[0]);
            str.append("')");
            str.append(" and ");
        }
        str.append("LOWER (TABLE_NAME) = LOWER");
        str.append("('");
        str.append(tableName);
        str.append("')");
        log.info("查询sql：{}", str.toString());

        return str.toString();
    }

    /**
     * 获取数仓建模首页 dw表数据量的sql
     *
     * @return
     */
    public static String buildDataModelDimCountSql(List<DimensionPO> dimList) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT\n" +
                "\trelname AS \"TABLE_NAME\",\n" +
                "\treltuples AS \"TABLE_ROWS\" \n" +
                "FROM\n" +
                "\tpg_class CLS\n" +
                "\tLEFT JOIN pg_namespace N ON ( N.oid = CLS.relnamespace ) \n" +
                "WHERE\n" +
                "\tnspname NOT IN ( 'pg_catalog', 'information_schema' ) \n" +
                "\tAND relkind = 'r' \n");
        str.append(" AND relname IN (");
        for (DimensionPO dimensionPO : dimList) {
            str.append("'")
                    .append(dimensionPO.getDimensionTabName())
                    .append("',");
        }
        str.deleteCharAt(str.lastIndexOf(","));
        str.append(") ");
        str.append("ORDER BY reltuples DESC ");

        return str.toString();
    }

    /**
     * 获取数仓建模首页 dw表数据量的sql
     *
     * @return
     */
    public static String buildDataModelCountSql(List<FactPO> factList) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT\n" +
                "\trelname AS \"TABLE_NAME\",\n" +
                "\treltuples AS \"TABLE_ROWS\" \n" +
                "FROM\n" +
                "\tpg_class CLS\n" +
                "\tLEFT JOIN pg_namespace N ON ( N.oid = CLS.relnamespace ) \n" +
                "WHERE\n" +
                "\tnspname NOT IN ( 'pg_catalog', 'information_schema' ) \n" +
                "\tAND relkind = 'r' \n");
        str.append(" AND relname IN (");
        for (FactPO factPO : factList) {
            str.append("'")
                    .append(factPO.getFactTabName())
                    .append("',");
        }
        str.deleteCharAt(str.lastIndexOf(","));
        str.append(") ");
        str.append("ORDER BY reltuples DESC ");
        return str.toString();
    }

}
