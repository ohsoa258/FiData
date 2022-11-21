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
public class PostgresConUtils {

    /**
     * 获取表详情(表信息+字段信息)
     */
    public List<TablePyhNameDTO> getTableNameAndColumns(String url, String user, String password, DataSourceTypeEnum driver) {
        List<TablePyhNameDTO> tablesPlus = null;
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(driver.getDriverName());
            conn = DriverManager.getConnection(url, user, password);
            stmt = conn.createStatement();

            tablesPlus = getTablesPlus(conn);
            int tag = 0;
            if (CollectionUtils.isNotEmpty(tablesPlus)) {
                for (TablePyhNameDTO tablePyhNameDTO : tablesPlus) {
                    List<TableStructureDTO> colNames = getColumns_V1(stmt, tablePyhNameDTO.getTableFullName());
                    tablePyhNameDTO.setFields(colNames);
                    tag++;
                    tablePyhNameDTO.setTag(tag);
                }
            }
        } catch (Exception e) {
            log.error("【getTableNameAndColumnsPlus】获取表详情(表信息+字段信息)异常：", e);
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
        List<DataBaseViewDTO> list = null;
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(driverTypeEnum.getDriverName());
            conn = DriverManager.getConnection(url, user, password);
            // 获取所有架构名
            List<String> schemaList = getSchemaList(conn);
            // 获取数据库中所有视图名称
            List<String> viewNameList = loadViewNameList(conn, schemaList);
            stmt = conn.createStatement();
            list = new ArrayList<>();
            for (String viewName : viewNameList) {
                List<TableStructureDTO> colNames = getViewColumns(conn, viewName);
                DataBaseViewDTO dto = new DataBaseViewDTO();
                dto.viewName = viewName;
                dto.fields = colNames;
                list.add(dto);
            }
        } catch (Exception e) {
            log.error("【loadViewDetails】获取视图信息异常", e);
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
     * 根据架构名获取视图名
     */
    private List<String> loadViewNameList(Connection conn, List<String> schemaList) {
        List<String> viewNameList = new ArrayList<>();
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            String[] types = {"VIEW"};
            for (String schema : schemaList) {
                ResultSet rs = databaseMetaData.getTables(null, schema, null, types);
                // schema下不存在view，走到这里为空会跳过
                while (rs.next()) {
                    String viewName = rs.getString(3);
                    viewNameList.add(schema + "." + viewName);
                }
                // 关闭
                rs.close();
            }
        } catch (Exception e) {
            log.error("【loadViewNameList】根据架构名获取视图名异常：", e);
            throw new FkException(ResultEnum.LOAD_VIEW_NAME_ERROR);
        }
        return viewNameList;
    }

    /**
     * 获取某个库下的所有架构名
     */
    public List<String> getSchemaList(Connection conn) {
        List<String> schemaList = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT\n" +
                    "\tschemata.SCHEMA_NAME AS schemaName \n" +
                    "FROM\n" +
                    "\tinformation_schema.schemata AS schemata\n" +
                    "\tLEFT JOIN pg_tables tables ON schemata.SCHEMA_NAME = tables.schemaname \n" +
                    "WHERE\n" +
                    "\ttables.tablename IS NOT NULL \n" +
                    "\tAND tables.tablename <> '' \n" +
                    "\tAND schemata.SCHEMA_NAME NOT IN ( 'pg_catalog', 'information_schema' ) \n" +
                    "GROUP BY\n" +
                    "SCHEMA_NAME \n" +
                    "ORDER BY\n" +
                    "SCHEMA_NAME");
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
     * 获取某个库下面的表信息
     */
    public List<TablePyhNameDTO> getTablesPlus(Connection conn) {
        List<TablePyhNameDTO> tableList = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT schemata.\"schema_name\" AS schemaName,tables.tablename AS tableName \n" +
                    "FROM information_schema.schemata AS schemata\n" +
                    "LEFT JOIN pg_tables tables ON schemata.\"schema_name\"=tables.schemaname\n" +
                    "WHERE tables.tablename IS NOT NULL AND tables.tablename<>''\n" +
                    "AND schemata.\"schema_name\" NOT IN ('pg_catalog','information_schema')\n" +
                    "ORDER BY \"schema_name\", tablename ");
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
        } catch (SQLException e) {
            log.error("【PostgresConUtils/getTablesPlus】读取表信息异常, ex", e);
            throw new FkException(ResultEnum.PG_READ_TABLE_ERROR);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error("【PostgresConUtils/getTablesPlus】关闭连接异常, ex", e);
                    throw new FkException(ResultEnum.PG_READ_TABLE_ERROR);
                }
            }
        }
        return tableList;
    }

    /**
     * 获取某张表下面的列信息
     */
    public List<TableStructureDTO> getColumns(Connection conn, String tableName) {
        // tableName 表名应携带架构名查询
        List<TableStructureDTO> colNameList = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from " + tableName + " LIMIT 0;");
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
            rs.close();
        } catch (SQLException e) {
            log.error("【getColumns】获取表字段异常：", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error("【getColumns】关闭数据库连接异常：", e);
                    throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
                }
            }
        }
        return colNameList;
    }

    /**
     * 获取某张表下面的列信息
     */
    public List<TableStructureDTO> getColumns_V1(Statement st, String tableName) {
        // tableName 表名应携带架构名查询
        List<TableStructureDTO> colNameList = null;
        try {
            ResultSet rs = st.executeQuery("select * from " + tableName + " LIMIT 0;");
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
            rs.close();
        } catch (SQLException e) {
            log.error("【getColumns_V1】获取表字段异常：", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return colNameList;
    }

    /**
     * 获取指定表视图字段信息
     */
    public List<TableStructureDTO> getViewColumns(Connection conn, String viewName) {
        List<TableStructureDTO> colNameList = new ArrayList<>();
        try {
            ResultSet rs = conn.getMetaData().getColumns(null, "%", viewName, "%");
            while (rs.next()) {
                TableStructureDTO tableStructureDTO = new TableStructureDTO();
                //字段名称
                tableStructureDTO.setFieldName(rs.getString("COLUMN_NAME"));
                //字段类型
                tableStructureDTO.setFieldType(rs.getString("TYPE_NAME"));
                //字段长度
                tableStructureDTO.setFieldLength(Integer.parseInt(rs.getString("COLUMN_SIZE")));
                //备注
                //tableStructureDTO.setFieldDes(rs.getString("REMARKS"));
                colNameList.add(tableStructureDTO);
            }
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            log.error("【getViewColumns】读取字段信息异常, ex", e);
            throw new FkException(ResultEnum.PG_READ_FIELD_ERROR);
        }
        return colNameList;
    }
}
