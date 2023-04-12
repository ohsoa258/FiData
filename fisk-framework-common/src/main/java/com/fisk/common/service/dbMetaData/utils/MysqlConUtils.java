package com.fisk.common.service.dbMetaData.utils;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.service.dbMetaData.dto.DataBaseViewDTO;
import com.fisk.common.service.dbMetaData.dto.TableNameDTO;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEnum;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MysqlConUtils {

    /**
     * 获取表详情(表信息+字段信息)
     */
    public List<TablePyhNameDTO> getTableNameAndColumns(String url, String user, String password, DataSourceTypeEnum driverTypeEnum) {
        List<TablePyhNameDTO> list = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(driverTypeEnum.getDriverName());
            conn = DriverManager.getConnection(url, user, password);
            stmt = conn.createStatement();

            int tag = 0;
            List<String> tableNames = getTablesPlus(conn);
            if (CollectionUtils.isNotEmpty(tableNames)){
                for (String tableName : tableNames) {
                    // mysql没有架构概念
                    List<TableStructureDTO> colNames = getColNames(stmt, tableName);
                    TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                    // mysql没有架构概念
                    tablePyhNameDTO.setTableFullName(tableName);
                    tablePyhNameDTO.setTableName(tableName);
                    tablePyhNameDTO.setFields(colNames);
                    tag++;
                    tablePyhNameDTO.setTag(tag);
                    list.add(tablePyhNameDTO);
                }
            }
        } catch (Exception e) {
            log.error("【getTableNameAndColumns】获取表名报错：", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                log.error("【getTableNameAndColumnsPlus】关闭数据库连接异常：", e);
                throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
            }
        }
        return list;
    }

    /**
     * 加载视图详情
     */
    public List<DataBaseViewDTO> loadViewDetails(DataSourceTypeEnum driverTypeEnum, String url, String user, String password) {
        List<DataBaseViewDTO> list = null;
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(driverTypeEnum.getDriverName());
            conn = DriverManager.getConnection(url, user, password);
            // 获取数据库中所有视图名称
            List<String> viewNameList = loadViewNameList(conn);
            stmt = conn.createStatement();
            list = new ArrayList<>();
            for (String viewName : viewNameList) {
                List<TableStructureDTO> colNames = getColNames(stmt, viewName);
                DataBaseViewDTO dto = new DataBaseViewDTO();
                dto.viewName = viewName;
                dto.fields = colNames;
                list.add(dto);
            }
        } catch (Exception e) {
            log.error("【loadViewDetails】加载视图详情报错, ex", e);
            throw new FkException(ResultEnum.LOAD_VIEW_STRUCTURE_ERROR);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                log.error("【loadViewDetails】关闭数据库连接异常：", e);
                throw new FkException(ResultEnum.LOAD_VIEW_STRUCTURE_ERROR);
            }
        }

        return list;
    }

    /**
     * 获取数据库中所有表名称
     */
    public List<String> getTablesPlus(Connection conn) {
        // MySql没有架构概念，此处直接查表
        ArrayList<String> tablesList = null;
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet tables = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"});
            tablesList = new ArrayList<>();
            while (tables.next()) {
                tablesList.add(tables.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            log.error("【getTablesPlus】获取数据库中所有表名称异常", e);
            throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR);
        }
        return tablesList;
    }

    /**
     * 获取视图名称列表
     */
    private List<String> loadViewNameList(Connection conn) {
        ArrayList<String> viewNameList = null;
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            String[] types = {"VIEW"};

            ResultSet rs = databaseMetaData.getTables(null, null, "%", types);
            viewNameList = new ArrayList<>();
            while (rs.next()) {
                viewNameList.add(rs.getString(3));
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            log.error("【loadViewNameList】获取视图名称列表异常", e);
            throw new FkException(ResultEnum.LOAD_VIEW_NAME_ERROR);
        }
        return viewNameList;
    }

    /**
     * 获取表中所有字段名称
     */
    public List<TableStructureDTO> getColNames(Statement st, String tableName) {
        // tableName应携带架构名称
        List<TableStructureDTO> colNameList = null;
        try {
            ResultSet rs = st.executeQuery("select * from " + tableName + " LIMIT 0,10;");
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
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            log.error("【getColNames】获取表中所有字段名称异常", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return colNameList;
    }

    public List<TableNameDTO> getViewTableNameAndColumns(String url, String user, String password, String mysql) {
        List<TableNameDTO> list = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(mysql);
            conn = DriverManager.getConnection(url, user, password);
            stmt = conn.createStatement();

            int tag = 0;
            List<String> tableNames = getTablesPlus(conn);
            if (CollectionUtils.isNotEmpty(tableNames)){
                for (String tableName : tableNames) {
                    // mysql没有架构概念
                    List<TableStructureDTO> colNames = getColNames(stmt, tableName);
                    TableNameDTO tablePyhNameDTO = new TableNameDTO();
                    // mysql没有架构概念
                    tablePyhNameDTO.setTableFullName(tableName);
                    tablePyhNameDTO.setTableName(tableName);
                    tablePyhNameDTO.setFields(colNames);
                    tag++;
                    tablePyhNameDTO.setTag(tag);
                    list.add(tablePyhNameDTO);
                }
            }
        } catch (Exception e) {
            log.error("【getTableNameAndColumns】获取表名报错：", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                log.error("【getTableNameAndColumnsPlus】关闭数据库连接异常：", e);
                throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
            }
        }
        return list;
    }
}
