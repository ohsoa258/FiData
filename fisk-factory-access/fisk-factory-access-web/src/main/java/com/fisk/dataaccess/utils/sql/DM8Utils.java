package com.fisk.dataaccess.utils.sql;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DM8Utils {

    public List<String> getAllDatabases(Connection conn) {

        List<String> dbName = new ArrayList<>();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
//            DatabaseMetaData dbmd = conn.getMetaData();
            rs = st.executeQuery("SELECT DISTINCT object_name FROM ALL_OBJECTS  WHERE OBJECT_TYPE = 'SCH';");
            while (rs.next()) {
                dbName.add(rs.getString("object_name"));
            }
        } catch (SQLException e) {
            log.error("获取所有数据库失败,{}", e);
            throw new FkException(ResultEnum.GET_DATABASE_ERROR);
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }

        return dbName;
    }

    /**
     * 获取表及表字段
     *
     * @return 查询结果
     */
    public List<TablePyhNameDTO> getTableNameAndColumns(Connection conn, String dbName) {

        List<TablePyhNameDTO> list = null;
        Statement st = null;
        try {
            // 获取数据库中所有表名称
            List<String> tableNames = getTables(conn, dbName);
            st = conn.createStatement();

            list = new ArrayList<>();

            for (String tableName : tableNames) {

                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(tableName);
                list.add(tablePyhNameDTO);

            }
        } catch (SQLException e) {
            log.error("【getTableNameAndColumns】获取表名报错, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }

        return list;
    }

    /**
     * 获取数据库中所有表名称
     *
     * @param conn   conn
     * @param dbName
     * @return 返回值
     */
    private List<String> getTables(Connection conn, String dbName) {
        List<String> tablesList = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();

            ResultSet resultSet = stmt.executeQuery("SELECT table_name FROM dba_tables WHERE owner ='" + dbName + "';");
            while (resultSet.next()) {
                // TABLE_NAME
                String name = resultSet.getString("table_name");
                tablesList.add(name);
            }
        } catch (Exception e) {
            log.error("【getTablesPlus】获取表名及架构名失败, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR);
        }
        return tablesList;
    }

    /**
     * 根据tableName获取tableFields
     *
     * @param tableName tableName
     * @return tableName中的表字段
     */
    public List<TableStructureDTO> getColumnsName(Connection conn, String tableName, String dbName) {
        List<TableStructureDTO> colNameList = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            String query = "SELECT COLUMN_NAME,DATA_TYPE,DATA_LENGTH FROM all_tab_columns WHERE owner = ? AND Table_Name = ?;";
            statement = conn.prepareStatement(query);
            if (tableName.contains(".")) {
                tableName = tableName.split("\\.")[1];
            }
            statement.setString(1, dbName);
            statement.setString(2, tableName);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                TableStructureDTO dto = new TableStructureDTO();
                dto.fieldName = resultSet.getString("COLUMN_NAME");
                dto.fieldLength = Long.valueOf(resultSet.getString("DATA_LENGTH"));
                dto.fieldType = resultSet.getString("DATA_TYPE");
                colNameList.add(dto);
            }
        } catch (Exception e) {
            log.error("【getColumnsName】获取表字段报错, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    log.error("JDBC: openEdge关闭ResultSet失败...", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    log.error("JDBC: openEdge关闭PreparedStatement失败...", e);
                }
            }
        }
        return colNameList;
    }

}
