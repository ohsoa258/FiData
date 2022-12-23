package com.fisk.common.core.utils.dbutils.utils;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.dbdatatype.OracleTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import lombok.extern.slf4j.Slf4j;

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
public class OracleUtils {

    /**
     * 读取Oracle表、字段信息
     *
     * @param conn
     * @return
     */
    public static List<TableNameDTO> getTableName(Connection conn) {
        List<TableNameDTO> list = new ArrayList<>();

        // 获取数据库中所有表名称
        List<String> tableList = getTables(conn);
        if (CollectionUtils.isEmpty(tableList)) {
            return null;
        }
        list = new ArrayList<>();
        for (String tableName : tableList) {
            TableNameDTO tablePyhNameDTO = new TableNameDTO();
            tablePyhNameDTO.setTableName(tableName);
            list.add(tablePyhNameDTO);
        }

        return list;
    }

    private static List<String> getTables(Connection conn) {
        Statement st = null;
        ResultSet rs = null;
        List<String> list = new ArrayList<>();
        try {
            st = conn.createStatement();
            rs = st.executeQuery(buildAllTableSql());
            while (rs.next()) {
                list.add(rs.getString("table_name"));
            }
        } catch (SQLException e) {
            log.error("getTables ex:{}", e);
            throw new FkException(ResultEnum.DATAACCESS_CONNECTDB_ERROR);
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
            AbstractCommonDbHelper.closeStatement(st);
        }
        return list;
    }

    public static List<TableColumnDTO> getTableColumnInfoList(Connection conn, String dbName, String tableName) {
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(buildUserSelectTableColumnSql(dbName, tableName));
            List<TableColumnDTO> colNameList = new ArrayList<>();
            while (rs.next()) {
                colNameList.add(conversionType(rs));
            }
            return colNameList;
        } catch (SQLException e) {
            log.error("【oracle获取表字段信息失败】,{}", e);
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return null;
    }

    /**
     * 获取该用户下所有表sql
     *
     * @return
     */
    public static String buildAllTableSql() {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append("table_name ");
        str.append("FROM ");
        str.append("user_tables");
        return str.toString();
    }

    /**
     * 根据user_tab_cols表,拼接查询表字段信息sql
     *
     * @param dbName
     * @param tableName
     * @return
     */
    public static String buildUserSelectTableColumnSql(String dbName, String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append("a.TABLE_NAME,");
        str.append("a.DATA_PRECISION,");
        str.append("a.DATA_SCALE,");
        str.append("a.COLUMN_NAME,");
        str.append("a.DATA_TYPE,");
        str.append("a.DATA_LENGTH ");
        str.append("FROM ");
        str.append("user_tab_cols a ");
        str.append("LEFT JOIN ALL_COL_COMMENTS b ON a.TABLE_NAME = b.TABLE_NAME ");
        str.append("WHERE ");
        str.append("b.owner='" + dbName + "' ");
        str.append("and ");
        str.append("a.TABLE_NAME='" + tableName + "' ");
        return str.toString();
    }

    /**
     * 根据类型判断精度
     *
     * @param rs
     * @return
     */
    public static TableColumnDTO conversionType(ResultSet rs) {
        try {
            TableColumnDTO dto = new TableColumnDTO();
            dto.fieldName = rs.getString("COLUMN_NAME");
            dto.fieldType = rs.getString("DATA_TYPE");
            dto.fieldLength = Integer.parseInt(rs.getString("DATA_LENGTH"));
            dto.fieldPrecision = 0;
            OracleTypeEnum typeEnum = OracleTypeEnum.getValue(dto.fieldType);
            switch (typeEnum) {
                case NUMBER:
                    if (rs.getString("DATA_PRECISION") == null) {
                        dto.fieldLength = 0;
                        dto.fieldPrecision = 0;
                        break;
                    }
                    dto.fieldLength = Integer.parseInt(rs.getString("DATA_PRECISION"));
                    dto.fieldPrecision = Integer.parseInt(rs.getString("DATA_SCALE"));
                    break;
                default:
                    break;
            }
            return dto;
        } catch (SQLException e) {
            log.error("conversionType ex:", e);
            throw new FkException(ResultEnum.DATA_OPS_SQL_EXECUTE_ERROR);
        }

    }

}
