package com.fisk.common.core.utils.dbutils.utils;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Slf4j
public class MySqlConUtils {

    /**
     * 获取表及表字段
     *
     * @return 查询结果
     */
    public static List<TableNameDTO> getTableName(Connection conn) {
        // 获取数据库中所有表名称
        List<String> tableNames = getTable(conn);

        List<TableNameDTO> list = new ArrayList<>();
        for (String tableName : tableNames) {
            TableNameDTO tablePyhNameDTO = new TableNameDTO();
            tablePyhNameDTO.setTableName(tableName);
            list.add(tablePyhNameDTO);
        }
        return list;

    }

    /**
     * 获取数据库中所有表名称
     *
     * @param conn conn
     * @return 返回值
     */
    private static List<String> getTable(Connection conn) {
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet tables = databaseMetaData.getTables(null, null, "%", null);
            ArrayList<String> tablesList = new ArrayList<String>();
            while (tables.next()) {
                tablesList.add(tables.getString("TABLE_NAME"));
            }
            return tablesList;
        } catch (SQLException e) {
            log.error("【MySQL获取指定库中所有表失败,{}】", e);
            throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR);
        }
    }

    /**
     * 获取表中所有字段名称
     *
     * @param conn
     * @param tableName
     * @return
     */
    public static List<TableColumnDTO> getColNames(Connection conn, String tableName) {
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery("select * from `" + tableName + "` LIMIT 0,1;");
            ResultSetMetaData metaData = rs.getMetaData();
            int count = metaData.getColumnCount();

            List<TableColumnDTO> colNameList = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                TableColumnDTO tableStructureDTO = new TableColumnDTO();

                // 字段名称
                tableStructureDTO.fieldName = metaData.getColumnName(i);
                // 字段类型
                tableStructureDTO.fieldType = metaData.getColumnTypeName(i);
                // 字段长度
                tableStructureDTO.fieldLength = metaData.getColumnDisplaySize(i);
                //字段精度
                tableStructureDTO.fieldPrecision = metaData.getPrecision(i);
                colNameList.add(tableStructureDTO);
            }
            return colNameList;
        } catch (SQLException e) {
            log.error("【MySQL根据表名获取字段信息失败,{}】", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
            AbstractCommonDbHelper.closeStatement(st);
        }
    }

}
