package com.fisk.common.service.dbMetaData.utils;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbMetaData.dto.DataBaseViewDTO;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.sql.*;
import java.util.*;

@Slf4j
public class SqlServerPlusUtils {

    /**
     * 获取某张表下面的列信息
     */
    public List<TableStructureDTO> getColumns(Connection conn, String tableName, String tableFramework) {
        // 注意：这里的tableName不能带架构名，否则会查询不到数据
        List<TableStructureDTO> colNameList = null;
        try {
            colNameList = new ArrayList<>();
            tableFramework = tableFramework == null || tableFramework == "" ? "%" : tableFramework;
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet resultSet = metaData.getColumns(null, tableFramework, tableName, "%");
            while (resultSet.next()) {
                TableStructureDTO dto = new TableStructureDTO();
                dto.fieldName = resultSet.getString("COLUMN_NAME");
                dto.fieldType = resultSet.getString("TYPE_NAME");
                dto.fieldLength = Integer.parseInt(resultSet.getString("COLUMN_SIZE"));
                dto.fieldDes = resultSet.getString("REMARKS");
                colNameList.add(dto);
            }
            resultSet.close();
        } catch (Exception e) {
            log.error("【getColumns】获取表字段异常：", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return colNameList;
    }

    /**
     * 获取某个库下面的表信息
     */
    public List<TablePyhNameDTO> getTablesPlus(Connection conn) {
        List<TablePyhNameDTO> tableList = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT tableName,schemaName \n" +
                    "FROM\n" +
                    "\t( SELECT name AS tableName, schema_name( schema_id ) AS schemaName FROM sys.tables ) AS tab \n" +
                    "ORDER BY\n" +
                    "\tschemaName,tableName");
            while (resultSet.next()) {
                // 表名称
                String tableName = resultSet.getString("tableName");
                // 架构名
                String schemaName = resultSet.getString("schemaName");
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(tableName);
                tablePyhNameDTO.setTableFramework(schemaName);
                String tableFullName = StringUtils.isEmpty(schemaName) ? tableName : schemaName + "." + tableName;
                tablePyhNameDTO.setTableFullName(tableFullName);
                tableList.add(tablePyhNameDTO);
            }
            resultSet.close();
        } catch (SQLException e) {
            log.error("【getTablesPlus】获取表名及架构名异常：", e);
            throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error("【getTablesPlus】关闭数据库连接异常：", e);
                    throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR);
                }
            }
        }
        return tableList;
    }

    /**
     * 获取某个库下的所有架构名
     */
    public List<String> getSchemaList(Connection conn) {
        List<String> schemaList = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT DISTINCT schemaName \n" +
                    "FROM\n" +
                    "\t( SELECT schema_name( schema_id ) AS schemaName FROM sys.tables ) AS tab \n" +
                    "ORDER BY\n" +
                    "\tschemaName");
            while (resultSet.next()) {
                String schemaName = resultSet.getString("schemaName");
                schemaList.add(schemaName);
            }
            resultSet.close();
        } catch (SQLException e) {
            log.error("【getSchemaList】获取架构名异常：", e);
            throw new FkException(ResultEnum.DATAACCESS_GETSCHEMA_ERROR);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error("【getSchemaList】关闭数据库连接异常：", e);
                    throw new FkException(ResultEnum.DATAACCESS_GETSCHEMA_ERROR);
                }
            }
        }
        return schemaList;
    }

    /**
     * 获取表详情(表信息+字段信息)
     */
    public List<TablePyhNameDTO> getTableNameAndColumnsPlus(String url, String user, String password, DataSourceTypeEnum driverTypeEnum) {
        List<TablePyhNameDTO> tablesPlus = null;
        Connection conn = null;
        Statement stmt = null;
        try {
            //1.加载驱动程序
            Class.forName(driverTypeEnum.getDriverName());
            //2.获得数据库的连接
            conn = DriverManager.getConnection(url, user, password);
            stmt = conn.createStatement();

            // 获取指定数据库所有表
            tablesPlus = this.getTablesPlus(conn);
            if (CollectionUtils.isNotEmpty(tablesPlus)) {
                for (TablePyhNameDTO tablePyhNameDTO : tablesPlus) {
                    List<TableStructureDTO> columnsName = getColumns(conn, tablePyhNameDTO.getTableName(), tablePyhNameDTO.getTableFramework());
                    tablePyhNameDTO.setFields(columnsName);
                }
            }
        } catch (Exception e) {
            log.error("【getTableNameAndColumnsPlus】获取表详情(表信息+字段信息)异常", e);
            throw new FkException(ResultEnum.DATAACCESS_GETTABLEANDFIELD_ERROR);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                log.error("【getTableNameAndColumnsPlus】关闭数据库连接异常：", e);
                throw new FkException(ResultEnum.DATAACCESS_GETTABLEANDFIELD_ERROR);
            }
        }
        return tablesPlus;
    }

    /**
     * 获取视图详情(视图名称 + 字段)
     */
    public List<DataBaseViewDTO> loadViewDetails(DataSourceTypeEnum driverTypeEnum, String url, String user, String password) {
        List<DataBaseViewDTO> dataBaseViewDTOS = null;
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(driverTypeEnum.getDriverName());
            conn = DriverManager.getConnection(url, user, password);
            stmt = conn.createStatement();
            // 获取所有架构名
            List<String> schemaList = getSchemaList(conn);
            dataBaseViewDTOS = loadViewNameList(conn, schemaList);
            for (DataBaseViewDTO dto : dataBaseViewDTOS) {
                ResultSet resultSql = null;
                try {
                    resultSql = stmt.executeQuery("select * from " + dto.viewName + ";");
                    List<TableStructureDTO> colNames = getViewFields(resultSql);
                    dto.fields = colNames;
                    dto.flag = 1;
                } catch (SQLException e) {
                    log.error("无效的视图: " + dto.viewName);
                    dto.flag = 2;
                    continue;
                } finally {
                    if (resultSql != null) {
                        // 关闭当前结果集
                        resultSql.close();
                    }
                }
            }

        } catch (ClassNotFoundException | SQLException e) {
            log.error("【loadViewDetails】获取表名异常：", e);
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
        return dataBaseViewDTOS;
    }

    /**
     * 根据架构名获取视图名
     */
    private List<DataBaseViewDTO> loadViewNameList(Connection conn, List<String> schemaList) {
        ArrayList<DataBaseViewDTO> viewNameList = new ArrayList<>();
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            String[] types = {"VIEW"};

            for (String schema : schemaList) {
                ResultSet rs = databaseMetaData.getTables(null, schema, null, types);
                while (rs.next()) {
                    DataBaseViewDTO dto = new DataBaseViewDTO();
                    String viewRelName = rs.getString(3);
                    String viewName = schema + ".[" + viewRelName + "]";
                    dto.setViewFramework(schema);
                    dto.setViewRelName(viewRelName);
                    dto.setViewName(viewName);
                    viewNameList.add(dto);
                }
                // 关闭
                rs.close();
            }
        } catch (SQLException e) {
            log.error("【loadViewNameList】根据架构名获取视图名异常：", e);
            throw new FkException(ResultEnum.LOAD_VIEW_NAME_ERROR);
        }
        return viewNameList;
    }

    /**
     * 获取视图的所有表字段
     */
    private List<TableStructureDTO> getViewFields(ResultSet rs) {
        List<TableStructureDTO> colNameList = null;
        try {
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
            log.error("【getViewField】获取视图的所有表字段：", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return colNameList;
    }
}
