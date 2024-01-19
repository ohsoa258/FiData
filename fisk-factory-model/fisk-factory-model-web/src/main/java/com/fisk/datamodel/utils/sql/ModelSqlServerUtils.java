package com.fisk.datamodel.utils.sql;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.datamodel.entity.dimension.DimensionPO;
import com.fisk.datamodel.entity.fact.FactPO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.*;

/**
 * @author JianWenYang
 */
@Slf4j
public class ModelSqlServerUtils {


    public static List<TableNameDTO> getTableName(Connection conn) {
        // 获取指定数据库所有表
        Map<String, String> mapList = getTables(conn);
        List<TableNameDTO> finalList = new ArrayList<>();
        Iterator<Map.Entry<String, String>> iterator = mapList.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            TableNameDTO tablePyhNameDTO = new TableNameDTO();
            tablePyhNameDTO.setTableName(entry.getValue() + "." + entry.getKey());
            finalList.add(tablePyhNameDTO);
        }
        return finalList;

    }

    /**
     * 获取架构名+表名
     *
     * @param conn
     * @return
     */
    public static Map<String, String> getTables(Connection conn) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buildTableColumnBySchemaSql());

            Map<String, String> tableMap = new LinkedHashMap<>();
            while (rs.next()) {
                // TABLE_NAME
                String name = rs.getString("name");
                // 架构名
                String schema = rs.getString("structure");
                tableMap.put(name, schema);
            }

            return tableMap;
        } catch (SQLException e) {
            log.error("【SqlServer获取表名及架构名失败】,{}", e);
            throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR);
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
            AbstractCommonDbHelper.closeStatement(stmt);
        }
    }

    /**
     * 根据tableName获取tableFields
     *
     * @param tableName tableName
     * @return tableName中的表字段
     */
    public static List<TableColumnDTO> getColumnsName(Connection conn, String tableName) {
        List<TableColumnDTO> colNameList = null;
        ResultSet resultSet = null;
        try {
            colNameList = new ArrayList<>();

            DatabaseMetaData metaData = conn.getMetaData();
            if (tableName.contains(".")) {
                String[] str = tableName.split("\\.");
                resultSet = metaData.getColumns(null, str[0], str[1], "%");
            } else {
                resultSet = metaData.getColumns(null, "%", tableName, "%");
            }
            while (resultSet.next()) {
                TableColumnDTO dto = new TableColumnDTO();
                dto.fieldName = resultSet.getString("COLUMN_NAME");
                dto.fieldType = resultSet.getString("TYPE_NAME");
                dto.fieldLength = Integer.parseInt(resultSet.getString("COLUMN_SIZE"));
                dto.fieldDes = resultSet.getString("REMARKS");
                colNameList.add(dto);
            }

        } catch (Exception e) {
            log.error("【getColumnsName】获取表字段报错, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            AbstractCommonDbHelper.closeResultSet(resultSet);
        }
        return colNameList;
    }

    public static List<TableColumnDTO> getColumnsNameBySql(Connection conn, String tableName) {
        List<TableColumnDTO> colNameList = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            colNameList = new ArrayList<>();

            st = conn.createStatement();
            String sql = "SELECT TOP 1 * FROM " + tableName;

            rs = st.executeQuery(sql);

            // 获取列数
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                TableColumnDTO dto = new TableColumnDTO();
                dto.fieldName = metaData.getColumnName(i);
                dto.fieldType = metaData.getColumnTypeName(i).toLowerCase();
                colNameList.add(dto);
            }

        } catch (Exception e) {
            log.error("【getColumnsName】获取表字段报错, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeResultSet(rs);
        }
        return colNameList;
    }

    /**
     * 获取schema下所有表sql
     *
     * @return
     */
    private static String buildTableColumnBySchemaSql() {
        StringBuilder str = new StringBuilder();
        str.append("select *,");
        str.append("RANK() over(order by tabl.structure) ");
        str.append("from ");
        str.append("(");
        str.append("select name, schema_name(schema_id) as structure from sys.tables");
        str.append(")");
        str.append("as tabl");
        str.append(";");

        return str.toString();
    }

    /**
     * 获取数仓建模首页 dw表数据量的sql
     *
     * @return
     */
    public static String buildDataModelDimCountSql(List<DimensionPO> dimList) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT   \n" +
                "    t.name AS TABLE_NAME,   \n" +
                "    p.rows AS TABLE_ROWS  \n" +
                "FROM   \n" +
                "    sys.tables t  \n" +
                "INNER JOIN   \n" +
                "    sys.partitions p ON t.object_id = p.object_id  \n" +
                "WHERE   \n" +
                "    p.index_id IN (0,1) AND t.type_desc = 'USER_TABLE'");
        str.append(" AND t.name IN (");
        for (DimensionPO dimensionPO : dimList) {
            str.append("\"")
                    .append(dimensionPO.getDimensionTabName())
                    .append("\",");
        }
        str.deleteCharAt(str.lastIndexOf(","));
        str.append(")");
        return str.toString();
    }

    /**
     * 获取数仓建模首页 dw表数据量的sql
     *
     * @return
     */
    public static String buildDataModelCountSql(List<FactPO> factList) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT   \n" +
                "    t.name AS TABLE_NAME,   \n" +
                "    p.rows AS TABLE_ROWS  \n" +
                "FROM   \n" +
                "    sys.tables t  \n" +
                "INNER JOIN   \n" +
                "    sys.partitions p ON t.object_id = p.object_id  \n" +
                "WHERE   \n" +
                "    p.index_id IN (0,1) AND t.type_desc = 'USER_TABLE'");
        str.append(" AND t.name IN (");
        for (FactPO factPO : factList) {
            str.append("\"")
                    .append(factPO.getFactTabName())
                    .append("\",");
        }
        str.deleteCharAt(str.lastIndexOf(","));
        str.append(")");
        return str.toString();
    }

}
