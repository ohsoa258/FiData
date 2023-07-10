package com.fisk.dataaccess.utils.sql;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

/**
 * @author lsj
 * @date 20230707
 */
@Component
@Slf4j
public class OpenEdgeUtils {

    /**
     * 根据tableName获取tableFields
     *
     * @param tableName tableName
     * @return tableName中的表字段
     */
    public static List<TableStructureDTO> getColumnsName(Connection conn, String tableName) {
        List<TableStructureDTO> colNameList = null;
        try {
            colNameList = new ArrayList<>();
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet resultSet = metaData.getColumns(null, null, tableName, null);
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
     * 获取OpenEdge指定架构名下的架构名+表名
     *
     * @param conn   连接驱动
     * @param dbName 数据库名
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @return 表名及架构名的映射
     * @description 获取OpenEdge架构名 + 表名
     */
    public static Map<String, String> getTablesPlus(Connection conn, String dbName) {
        Map<String, String> tableMap = new LinkedHashMap<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT TBL as TABLE_NAME, OWNER as TABLE_SCHEMA FROM sysprogress.systables WHERE OWNER='" + dbName + "'");
            while (resultSet.next()) {
                // TABLE_NAME
                String tableName = resultSet.getString("TABLE_NAME");
                // 架构名
                String schemaName = resultSet.getString("TABLE_SCHEMA");
                tableMap.put(tableName, schemaName);
            }
        } catch (SQLException e) {
            log.error("【getTablesPlus】获取表名及架构名失败, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR);
        }
        return tableMap;
    }

    /**
     * 获取OpenEdge指定架构(数据库)下的所有表详情(表名+字段)
     *
     * @return java.util.List<com.fisk.dataaccess.table.TablePyhNameDTO>
     * @description 获取OpenEdge表详情(表名 + 字段)
     * @param conn
     * @param dbName
     * @return
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
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(entry.getValue() + "." + entry.getKey());
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

//    /**
//     * 获取OpenEdge架构名
//     *
//     * @param conn 连接驱动
//     * @return java.util.List<java.lang.String>
//     * @throws SQLException
//     * @description 获取OpenEdge架构名
//     */
//    public static List<String> getSchemaList(Connection conn) throws SQLException {
//        List<String> schemaList = new ArrayList<>();
//        Statement stmt = null;
//        try {
//            stmt = conn.createStatement();
//            ResultSet resultSet = stmt.executeQuery("SELECT DISTINCT OWNER FROM sysprogress.systables");
//            while (resultSet.next()) {
//                // 架构名
//                String schemaName = resultSet.getString("schema_name");
//                schemaList.add(schemaName);
//            }
//        } catch (SQLException e) {
//            log.error("【getSchemaList】获取架构名失败, ex", e);
//            throw new FkException(ResultEnum.DATAACCESS_GETSCHEMA_ERROR);
//        } finally {
//            AbstractCommonDbHelper.closeStatement(stmt);
//            AbstractCommonDbHelper.closeConnection(conn);
//        }
//        return schemaList;
//    }

    /**
     * 获取所有数据库/获取OpenEdge架构名
     *
     * @param conn
     * @return java.util.List<java.lang.String>
     * @author Lock
     * @date 2022/8/9 13:59
     */
    public static List<String> getAllDatabases(Connection conn) {
        List<String> dbName = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT DISTINCT OWNER FROM sysprogress.systables");
            while (resultSet.next()) {
                dbName.add(resultSet.getString("OWNER"));
            }
        } catch (SQLException e) {
            log.error("获取OpenEdge所有库失败,{}", e);
            throw new FkException(ResultEnum.GET_DATABASE_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(stmt);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return dbName;
    }

}
