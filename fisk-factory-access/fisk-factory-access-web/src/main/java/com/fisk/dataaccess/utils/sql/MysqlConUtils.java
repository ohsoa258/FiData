package com.fisk.dataaccess.utils.sql;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.dataaccess.dto.table.DataBaseViewDTO;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lock
 * <p>
 * MySQL 获取表及表字段
 */
@Slf4j
public class MysqlConUtils {

    /**
     * 获取表
     *
     * @return 查询结果
     */
    public List<TablePyhNameDTO> getTableNameAndColumns(Connection conn) {

        List<TablePyhNameDTO> list = null;
        Statement st = null;
        try {
            // 获取数据库中所有表名称
            List<String> tableNames = getTables(conn);
            st = conn.createStatement();

            list = new ArrayList<>();

            for (String tableName : tableNames) {
                /*ResultSet rs = null;
                try {
                    rs = st.executeQuery("select * from `" + tableName + "` LIMIT 0,1;");
                } catch (SQLException e) {
                    log.error("【getTableNameAndColumns】获取表名报错, ex", e);
                    continue;
                }*/

                //List<TableStructureDTO> colNames = getColNames(rs);

                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(tableName);
                //tablePyhNameDTO.setFields(colNames);

                list.add(tablePyhNameDTO);

                //rs.close();
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
     * 获取表及表字段
     *
     * @return 查询结果
     */
    public List<TablePyhNameDTO> getTrueTableNameAndColumns(Connection conn, String dbName) {

        List<TablePyhNameDTO> list = null;
        try {
            // 获取数据库中所有表名称
            List<String> tableNames = getTables(conn);

            list = new ArrayList<>();

            for (String tableName : tableNames) {

                List<TableStructureDTO> colNames = null;
                try {
                    colNames = getColNamesV2(conn, tableName, dbName);
                }catch (Exception e){
                    continue;
                }

                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(tableName);
                tablePyhNameDTO.setFields(colNames);

                list.add(tablePyhNameDTO);
            }
        } catch (Exception e) {
            log.error("【getTableNameAndColumns】获取表名报错: ", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            AbstractCommonDbHelper.closeConnection(conn);
        }

        return list;
    }

    /**
     * @return java.util.List<com.fisk.dataaccess.table.DataBaseViewDTO>
     * @description 加载视图详情
     * @author Lock
     * @date 2021/12/31 17:46
     * @version v1.0
     * @params conn
     */
    public List<DataBaseViewDTO> loadViewDetails(Connection conn) {

        List<DataBaseViewDTO> list = null;
        Statement st = null;
        try {
            // 获取数据库中所有视图名称
            List<String> viewNameList = loadViewNameList(conn);
            st = conn.createStatement();

            list = new ArrayList<>();

            for (String viewName : viewNameList) {
                DataBaseViewDTO dto = new DataBaseViewDTO();
                /*ResultSet resultSql = null;
                try {
                    resultSql = st.executeQuery("select * from `" + viewName + "`;");

                    List<TableStructureDTO> colNames = getColNames(resultSql);

                    dto.viewName = viewName;
                    dto.fields = colNames;

                } catch (SQLException e) {
                    log.error("无效的视图: " + viewName + ": " + e);
                    dto.flag = 2;
                    continue;
                } finally {
                    // 关闭当前结果集
                    AbstractCommonDbHelper.closeResultSet(resultSql);
                }*/
                list.add(dto);
            }

        } catch (SQLException e) {
            log.error("【loadViewDetails】获取视图详情报错, ex", e);
            throw new FkException(ResultEnum.LOAD_VIEW_STRUCTURE_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }

        return list;
    }

    /**
     * 获取数据库中所有表名称
     *
     * @param conn conn
     * @return 返回值
     */
    private List<String> getTables(Connection conn) {
        ArrayList<String> tablesList = null;
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet tables = databaseMetaData.getTables(null, null, "%", null);
            tablesList = new ArrayList<String>();
            while (tables.next()) {
                tablesList.add(tables.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR);
        }
        return tablesList;
    }

    /**
     * @return java.util.List<java.lang.String>
     * @description 获取视图名称列表
     * @author Lock
     * @date 2021/12/31 17:45
     * @version v1.0
     * @params conn
     */
    private List<String> loadViewNameList(Connection conn) {
        ArrayList<String> viewNameList = null;
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            String[] types = {"VIEW"};

            ResultSet rs = null;
            rs = databaseMetaData.getTables(null, null, "%", types);
            viewNameList = new ArrayList<>();
            while (rs.next()) {
                viewNameList.add(rs.getString(3));
            }
        } catch (SQLException e) {
            log.error("MySQL加载视图失败{}", e);
            throw new FkException(ResultEnum.LOAD_VIEW_NAME_ERROR);
        }
        return viewNameList;
    }

    /**
     * 获取表中所有字段名称
     *
     * @param conn
     * @param tableName
     * @return
     */
    public List<TableStructureDTO> getColNames(Connection conn, String tableName) {
        List<TableStructureDTO> colNameList = new ArrayList<>();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery("select * from `" + tableName + "` LIMIT 0,1;");
            ResultSetMetaData metaData = rs.getMetaData();
            int count = metaData.getColumnCount();

            colNameList = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                TableStructureDTO tableStructureDTO = new TableStructureDTO();

                // 字段名称
                tableStructureDTO.fieldName = metaData.getColumnName(i);
                // 字段类型
                tableStructureDTO.fieldType = metaData.getColumnTypeName(i);
                // 字段长度
                tableStructureDTO.fieldLength = metaData.getColumnDisplaySize(i);
                colNameList.add(tableStructureDTO);
            }
        } catch (SQLException e) {
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return colNameList;
    }

    /**
     * 获取表中所有字段名称
     *
     * @param conn
     * @param tableName
     * @return
     */
    public List<TableStructureDTO> getColNamesV2(Connection conn, String tableName, String dbName) {
        List<TableStructureDTO> colNameList = new ArrayList<>();
        ResultSet resultSet = null;
        ResultSet primaryKeys = null;
        try {
            colNameList = new ArrayList<>();

            DatabaseMetaData metaData = conn.getMetaData();
            resultSet = metaData.getColumns(null, "%", tableName, "%");
            while (resultSet.next()) {
                TableStructureDTO dto = new TableStructureDTO();
                // 获取字段名称
                dto.fieldName = resultSet.getString("COLUMN_NAME");
                // 获取字段长度
                dto.fieldLength = resultSet.getInt("COLUMN_SIZE");
                // 获取字段类型
                dto.fieldType = resultSet.getString("TYPE_NAME");
                // 字段描述
                dto.setFieldDes(resultSet.getString("REMARKS"));
                dto.sourceTblName = tableName;
                dto.sourceDbName = dbName;
                colNameList.add(dto);
            }

            //获取表的主键字段
            List<String> pks = new ArrayList<>();
            primaryKeys = metaData.getPrimaryKeys(null, "%", tableName);
            while (primaryKeys.next()) {
                pks.add(primaryKeys.getString("COLUMN_NAME"));
            }
            colNameList.forEach(tableStructureDTO -> {
                if (pks.contains(tableStructureDTO.fieldName)) {
                    tableStructureDTO.setIsPk(1);
                } else {
                    tableStructureDTO.setIsPk(0);
                }
            });
        } catch (SQLException e) {
            log.error("mysql入参配置获取表名失败：" + e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            AbstractCommonDbHelper.closeResultSet(resultSet);
            AbstractCommonDbHelper.closeResultSet(primaryKeys);
        }
        return colNameList;
    }

    /**
     * 获取所有数据库
     *
     * @param conn
     * @return java.util.List<java.lang.String>
     * @author Lock
     * @date 2022/8/9 13:59
     */
    public List<String> getAllDatabases(Connection conn) {

        List<String> dbName = new ArrayList<>();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery("SHOW DATABASES;");
            while (rs.next()) {
                dbName.add(rs.getString("Database"));
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
}
