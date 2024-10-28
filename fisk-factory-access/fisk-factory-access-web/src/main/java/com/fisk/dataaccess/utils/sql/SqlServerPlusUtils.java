package com.fisk.dataaccess.utils.sql;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.dataaccess.dto.table.DataBaseViewDTO;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * SqlServer 获取表及表字段
 * </p>
 *
 * @author Lock
 */
@Slf4j
public class SqlServerPlusUtils {

    /**
     * 根据tableName获取tableFields
     *
     * @param tableName tableName
     * @return tableName中的表字段
     */
    public List<TableStructureDTO> getColumnsName(Connection conn, String tableName) {
        List<TableStructureDTO> colNameList = null;
        try {
            colNameList = new ArrayList<>();

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet resultSet = metaData.getColumns(null, "%", tableName, "%");
            while (resultSet.next()) {
                TableStructureDTO dto = new TableStructureDTO();
                dto.fieldName = resultSet.getString("COLUMN_NAME");
                colNameList.add(dto);
            }

        } catch (Exception e) {
            log.error("【getColumnsName】获取表字段报错, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return colNameList;
    }

    /**
     * 根据tableName获取tableFields
     *
     * @param tableName tableName
     * @return tableName中的表字段
     */
    public List<TableStructureDTO> getColumnsNameV2(Connection conn, String tableName, String dbName) {
        List<TableStructureDTO> colNameList = new ArrayList<>();
        ResultSet resultSet = null;
        ResultSet primaryKeys = null;
        try {
            colNameList = new ArrayList<>();

            DatabaseMetaData metaData = conn.getMetaData();
            String schemaName = null;
            if (tableName.contains(".")) {
                String[] parts = tableName.split("\\.");
                schemaName = parts[0];
                tableName = parts[1];

            }

            resultSet = metaData.getColumns(null, "%", tableName, "%");
            while (resultSet.next()) {
                TableStructureDTO dto = new TableStructureDTO();
                // 获取字段名称
                dto.fieldName = resultSet.getString("COLUMN_NAME");
                // 获取字段长度
                dto.fieldLength = (long) resultSet.getInt("COLUMN_SIZE");
                // 获取字段类型
                dto.fieldType = resultSet.getString("TYPE_NAME");
                // 字段描述
                dto.setFieldDes(resultSet.getString("REMARKS"));
                dto.sourceTblName = tableName;
                dto.sourceDbName = dbName;
                colNameList.add(dto);
            }

            log.info("表名：" + tableName);
            //获取表的主键字段
            List<String> pks = new ArrayList<>();
            log.info("截取后的表名：" + tableName);
            log.info("截取后的schema名：" + schemaName);
            primaryKeys = metaData.getPrimaryKeys(null, schemaName, tableName);
            log.info("主键字段：" + primaryKeys.toString());
            while (primaryKeys.next()) {
                pks.add(primaryKeys.getString("COLUMN_NAME"));
            }
            log.info("主键：" + pks);
            colNameList.forEach(tableStructureDTO -> {
                if (pks.contains(tableStructureDTO.fieldName)) {
                    tableStructureDTO.setIsPk(1);
//                    tableStructureDTO.setFieldDes("主键");
                } else {
                    tableStructureDTO.setIsPk(0);
                }
            });
        } catch (SQLException e) {
            log.error("获取表字段失败:" + e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            AbstractCommonDbHelper.closeResultSet(resultSet);
            AbstractCommonDbHelper.closeResultSet(primaryKeys);
        }
        return colNameList;
    }

    /**
     * 获取sqlserver架构名+表名
     *
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @description 获取sqlserver架构名 + 表名
     * @author Lock
     * @date 2022/4/1 14:52
     * @version v1.0
     * @params conn 连接驱动
     * @params dbName 数据库名
     */
    public Map<String, String> getTablesPlus(Connection conn, String dbName) {
        Map<String, String> tableMap = new LinkedHashMap<>();
        try {
            Statement stmt = conn.createStatement();

            ResultSet resultSet = stmt.executeQuery("select *,RANK() over(order by tabl.field) from \n" +
                    "(\n" +
                    "select name, schema_name(schema_id) as field from sys.tables\n" +
                    ") as tabl");
//            tableList = new ArrayList<>();
            while (resultSet.next()) {
                // TABLE_NAME
                String name = resultSet.getString("name");
                // 架构名
                String field2 = resultSet.getString("field");
                if (field2.equals("cdc")) continue;
                tableMap.put(name, field2);
            }
        } catch (SQLException e) {
            log.error("【getTablesPlus】获取表名及架构名失败, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR);
        }
        return tableMap;
    }

    /**
     * 获取架构名
     *
     * @return java.util.List<java.lang.String>
     * @description 获取架构名
     * @author Lock
     * @date 2022/4/1 15:13
     * @version v1.0
     * @params conn
     * @params dbName
     */
    public List<String> getSchemaList(Connection conn) {
        List<String> schemaList = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();

            ResultSet resultSet = stmt.executeQuery("select *,RANK() over(order by tabl.field) from \n" +
                    "(\n" +
                    "select name, schema_name(schema_id) as field from sys.tables\n" +
                    ") as tabl");
            while (resultSet.next()) {
                // 架构名
                String field2 = resultSet.getString("field");
                schemaList.add(field2);
            }
        } catch (SQLException e) {
            log.error("【getTablesPlus】获取表名及架构名失败, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR);
        }

        // 给架构名去重
        return schemaList.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 获取sqlserver表详情(表名+字段)
     *
     * @return java.util.List<com.fisk.dataaccess.table.TablePyhNameDTO>
     * @description 获取sqlserver表详情(表名 + 字段)
     * @author Lock
     * @date 2022/4/1 14:56
     * @version v1.0
     * @params url
     * @params user
     * @params password
     * @params dbName
     */
    public List<TablePyhNameDTO> getTableNameAndColumnsPlus(Connection conn, String dbName) {

        List<TablePyhNameDTO> list = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            list = new ArrayList<>();

            // 获取指定数据库所有表
            Map<String, String> mapList = this.getTablesPlus(conn, dbName);

            List<TablePyhNameDTO> finalList = list;

            Iterator<Map.Entry<String, String>> iterator = mapList.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                // 根据表名获取字段
                //List<TableStructureDTO> columnsName = getColumnsName(conn, entry.getKey());
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(entry.getValue() + "." + entry.getKey());
                //tablePyhNameDTO.setFields(columnsName);
                finalList.add(tablePyhNameDTO);
            }
        } catch (SQLException e) {
            log.error("【getTableNameAndColumnsPlus】获取表名及表字段失败, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(stmt);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return list;
    }

    /**
     * 获取sqlserver表详情(表名+字段)
     *
     * @return java.util.List<com.fisk.dataaccess.table.TablePyhNameDTO>
     * @description 获取sqlserver表详情(表名 + 字段)
     * @author Lock
     * @date 2022/4/1 14:56
     * @version v1.0
     * @params url
     * @params user
     * @params password
     * @params dbName
     */
    public List<TablePyhNameDTO> getTrueTableNameAndColumnsPlus(Connection conn, String dbName) {

        List<TablePyhNameDTO> list = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            list = new ArrayList<>();

            // 获取指定数据库所有表
            Map<String, String> mapList = this.getTablesPlus(conn, dbName);

            List<TablePyhNameDTO> finalList = list;

            Iterator<Map.Entry<String, String>> iterator = mapList.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                // 根据表名获取字段
                List<TableStructureDTO> columnsName = getColumnsNameV2(conn, entry.getValue() + "." + entry.getKey(), dbName);
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(entry.getValue() + "." + entry.getKey());
                tablePyhNameDTO.setFields(columnsName);
                finalList.add(tablePyhNameDTO);
            }
        } catch (SQLException e) {
            log.error("【getTableNameAndColumnsPlus】获取表名及表字段失败, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(stmt);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return list;
    }

    /**
     * 加载视图详情(视图名称 + 字段)
     *
     * @return java.util.List<com.fisk.dataaccess.table.DataBaseViewDTO>
     * @description 加载视图详情(视图名称 + 字段)
     * @author Lock
     * @date 2022/4/1 14:57
     * @version v1.0
     * @params driverTypeEnum
     * @params url
     * @params user
     * @params password
     * @params dbName
     */
    public List<DataBaseViewDTO> loadViewDetails(Connection conn) {

        List<DataBaseViewDTO> list = null;
        Statement st = null;
        try {
            // 获取所有架构名
            List<String> schemaList = getSchemaList(conn);
            // 获取数据库中所有视图名称
            List<String> viewNameList = loadViewNameList(conn, schemaList);
            st = conn.createStatement();

            list = new ArrayList<>();

            for (String viewName : viewNameList) {

                DataBaseViewDTO dto = new DataBaseViewDTO();
                dto.viewName = viewName;

                /*ResultSet resultSql = null;
                try {
                    resultSql = st.executeQuery("select * from " + viewName + ";");
                    List<TableStructureDTO> colNames = getViewField(resultSql);
                    dto.fields = colNames;
                    dto.flag = 1;
                } catch (SQLException e) {
                    log.error("无效的视图: " + viewName);
                    dto.flag = 2;
                } finally {
                    AbstractCommonDbHelper.closeResultSet(resultSql);
                }*/
                list.add(dto);
            }
        } catch (SQLException e) {
            log.error("【loadViewDetails】获取表名报错, ex", e);
            throw new FkException(ResultEnum.LOAD_VIEW_STRUCTURE_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }

        return list;
    }

    /**
     * 根据架构名获取视图名
     *
     * @return java.util.List<java.lang.String> 架构名+表名
     * @description 根据架构名获取视图名
     * @author Lock
     * @date 2022/4/1 15:02
     * @version v1.0
     * @params driverTypeEnum 驱动类型
     * @params conn 连接驱动
     * @params dbName 数据库名
     * @params schema 架构名
     */
    private List<String> loadViewNameList(Connection conn, List<String> schemaList) {
        ArrayList<String> viewNameList = null;
        try {
            viewNameList = new ArrayList<>();
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            String[] types = {"VIEW"};

            for (String schema : schemaList) {
                ResultSet rs = databaseMetaData.getTables(null, schema, null, types);
                while (rs.next()) {
                    rs.getString(3);
                    viewNameList.add(schema + ".[" + rs.getString(3) + "]");
                }
                // 关闭
                rs.close();
            }

        } catch (SQLException e) {
            log.error("获取SqlServer视图失败,{}", e);
            throw new FkException(ResultEnum.LOAD_VIEW_NAME_ERROR);
        }

        return viewNameList;
    }

    /**
     * 获取视图的所有表字段
     *
     * @return java.util.List<com.fisk.dataaccess.dto.tablestructure.TableStructureDTO>
     * @description 获取视图的所有表字段
     * @author Lock
     * @date 2022/4/1 14:59
     * @version v1.0
     * @params rs
     */
    public List<TableStructureDTO> getViewField(Connection conn, String viewName) {
        List<TableStructureDTO> colNameList = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery("select * from " + viewName + ";");
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
                tableStructureDTO.fieldLength = (long) metaData.getColumnDisplaySize(i);
                colNameList.add(tableStructureDTO);
            }
        } catch (SQLException e) {
            log.error("获取SqlServer视图字段失败,{}", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
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
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT name FROM  master..sysdatabases WHERE name NOT IN ( 'master', 'model', 'msdb', 'tempdb', 'northwind','pubs' )");
            while (resultSet.next()) {
                dbName.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            log.error("获取SqlServer所有库失败,{}", e);
            throw new FkException(ResultEnum.GET_DATABASE_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(stmt);
            AbstractCommonDbHelper.closeConnection(conn);
        }

        return dbName;
    }

}
