package com.fisk.dataaccess.utils.sql;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.dbdatatype.OracleTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.dataaccess.dto.table.DataBaseViewDTO;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import com.fisk.dataaccess.enums.DriverTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lock
 * @version 1.0
 * @description Oracle工具类
 * @date 2022/1/10 10:20
 */
@Slf4j
public class OracleUtils {

    public List<String> getAllDatabases(String url, String user, String password) {
        List<String> dbName = new ArrayList<>();
        try {
            Class.forName(DriverTypeEnum.ORACLE.getName());
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("select OWNER from all_tables where OWNER not in ('SYS','SYSTEM')");
            while (resultSet.next()) {
                dbName.add(resultSet.getString("OWNER"));
            }
            if (!CollectionUtils.isEmpty(dbName)) {
                dbName = dbName.stream().distinct().collect(Collectors.toList());
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new FkException(ResultEnum.GET_DATABASE_ERROR);
        }
        return dbName;
    }

    /**
     * 获取表及表字段
     *
     * @param url      url
     * @param user     user
     * @param password password
     * @return 查询结果
     */
    public List<TablePyhNameDTO> getTableNameAndColumns(String url, String user, String password, DriverTypeEnum driverTypeEnum) {

        List<TablePyhNameDTO> list;
        try {
            Class.forName(driverTypeEnum.getName());
            Connection conn = DriverManager.getConnection(url, user, password);
            // 获取数据库中所有表名称
            List<String> tableNames = getTables(conn,user.toUpperCase());
            if (CollectionUtils.isEmpty(tableNames)) {
                return null;
            }
            Statement st = conn.createStatement();

            list = new ArrayList<>();
            for (String tableName : tableNames) {
                ResultSet rs = st.executeQuery("select * from " + tableName + " OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY");

                List<TableStructureDTO> colNames = getColNames(rs);
                if (CollectionUtils.isEmpty(colNames)) {
                    break;
                }
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(tableName);
                tablePyhNameDTO.setFields(colNames);
                list.add(tablePyhNameDTO);

                rs.close();
            }

            st.close();
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            log.error("【getTableNameAndColumns】获取表名报错, ex", e);
            return null;
        }

        return list;
    }

    /**
     * @return java.util.List<com.fisk.dataaccess.table.DataBaseViewDTO>
     * @description 加载视图详情
     * @author Lock
     * @date 2021/12/31 17:46
     * @version v1.0
     * @params driverTypeEnum
     * @params url
     * @params user
     * @params password
     * @params dbName
     */
    public List<DataBaseViewDTO> loadViewDetails(DriverTypeEnum driverTypeEnum, String url, String user, String password, String dbName) {

        List<DataBaseViewDTO> list = null;
        try {
            Class.forName(driverTypeEnum.getName());
            Connection conn = DriverManager.getConnection(url, user, password);
            // 获取数据库中所有视图名称
            List<String> viewNameList = loadViewNameList(driverTypeEnum, conn, user.toUpperCase());
            if (CollectionUtils.isEmpty(viewNameList)) {
                return null;
            }
            Statement st = conn.createStatement();

            list = new ArrayList<>();

            for (String viewName : viewNameList) {
                ResultSet resultSql = st.executeQuery("SELECT * FROM \"" + user.toUpperCase() + "\".\"" + viewName + "\" OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY");

                List<TableStructureDTO> colNames = getColNames(resultSql);

                DataBaseViewDTO dto = new DataBaseViewDTO();
                dto.viewName = viewName;
                dto.fields = colNames;
                // 关闭当前结果集
                resultSql.close();

                list.add(dto);
            }

            st.close();
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            log.error("【getTableNameAndColumns】获取表名报错, ex", e);
            throw new FkException(ResultEnum.LOAD_VIEW_STRUCTURE_ERROR);
        }

        return list;
    }

    /**
     * 获取数据库中所有表名称
     *
     * @param conn conn
     * @return 返回值
     */
    private List<String> getTables(Connection conn, String user) {
        ArrayList<String> tablesList;
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            String[] types = {"TABLE"};
            ResultSet tables = databaseMetaData.getTables(null, user, null, types);
            tablesList = new ArrayList<>();
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
     * @params dbName
     */
    private List<String> loadViewNameList(DriverTypeEnum driverTypeEnum, Connection conn, String user) {
        ArrayList<String> viewNameList = null;
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            String[] types = {"VIEW"};

            ResultSet rs = null;
            rs = databaseMetaData.getTables(null, user, null, types);

            viewNameList = new ArrayList<>();
            while (rs.next()) {
                viewNameList.add(rs.getString(3));
            }
        } catch (SQLException e) {
            throw new FkException(ResultEnum.LOAD_VIEW_NAME_ERROR);
        }
        return viewNameList;
    }

    /**
     * 获取oracle主键
     *
     * @param url
     * @param account
     * @param password
     * @param dbName
     * @param tableName
     * @return
     */
    public static List<String> getTablePrimaryKey(String url, String account, String password, String dbName, String tableName) {
        Connection conn = null;
        Statement st = null;
        List<String> list = new ArrayList<>();
        try {
            Class.forName(DriverTypeEnum.ORACLE.getName());
            conn = DriverManager.getConnection(url, account, password);
            st = conn.createStatement();
            ResultSet rs = st.executeQuery(buildSelectTablePrimaryKeySql(dbName, tableName));
            list = new ArrayList<>();
            while (rs.next()) {
                list.add(rs.getString("COLUMN_NAME"));
            }
        } catch (ClassNotFoundException | SQLException e) {
            log.error("【getTablePrimaryKey】获取表主键报错, ex", e);
            throw new FkException(ResultEnum.SQL_ERROR);
        } finally {
            AbstractDbHelper.closeStatement(st);
            AbstractDbHelper.closeConnection(conn);
            return list;
        }
    }

    /**
     * 拼接查询表主键信息
     *
     * @param dbName
     * @param tableName
     * @return
     */
    public static String buildSelectTablePrimaryKeySql(String dbName, String tableName) {
        StringBuilder str = new StringBuilder();
        str.append(" SELECT ");
        str.append(" a.COLUMN_NAME ");
        str.append(" FROM ");
        str.append(" ALL_CONS_COLUMNS a, ");
        str.append(" ALL_CONSTRAINTS b ");
        str.append(" WHERE ");
        str.append(" a.CONSTRAINT_NAME = b.CONSTRAINT_NAME ");
        str.append(" AND b.CONSTRAINT_TYPE = 'P' ");
        str.append(" AND a.table_name ='" + tableName + "' ");
        str.append(" AND a.OWNER = '" + dbName + "'");
        return str.toString();
    }

    /**
     * 获取表中所有字段名称
     *
     * @param rs rs
     */
    private List<TableStructureDTO> getColNames(ResultSet rs) {
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
                tableStructureDTO.fieldDes = metaData.getCatalogName(i);
                colNameList.add(tableStructureDTO);
            }
            rs.close();
        } catch (SQLException e) {
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return colNameList;
    }

    /**
     * 读取Oracle表、字段信息
     *
     * @param url
     * @param account
     * @param password
     * @param dbName
     * @param driverTypeEnum
     * @return
     */
    public List<TablePyhNameDTO> getTableNameAndColumns(String url, String account, String password, String dbName, DriverTypeEnum driverTypeEnum) {
        Connection conn = null;
        Statement st = null;
        List<TablePyhNameDTO> list = new ArrayList<>();
        try {
            Class.forName(driverTypeEnum.getName());
            conn = DriverManager.getConnection(url, account, password);
            // 获取数据库中所有表名称
            List<String> tableList = getTables(conn, dbName);
            if (CollectionUtils.isEmpty(tableList)) {
                return null;
            }
            st = conn.createStatement();
            list = new ArrayList<>();
            for (String tableName : tableList) {
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(tableName);
                ResultSet rs = st.executeQuery(this.buildSelectTableColumnSql(dbName, tableName));
                List<TableStructureDTO> colNameList = new ArrayList<>();
                while (rs.next()) {
                    colNameList.add(conversionType(rs));
                }
                tablePyhNameDTO.setFields(colNameList);
                list.add(tablePyhNameDTO);
                rs.close();
            }
        } catch (ClassNotFoundException | SQLException e) {
            log.error("【getTableNameAndColumns】获取表名报错, ex", e);
            throw new FkException(ResultEnum.SQL_ERROR);
        } finally {
            AbstractDbHelper.closeStatement(st);
            AbstractDbHelper.closeConnection(conn);
            return list;
        }
    }

    /**
     * 拼接查询表字段信息sql
     *
     * @param dbName
     * @param tableName
     * @return
     */
    public String buildSelectTableColumnSql(String dbName, String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append("a.TABLE_NAME ");
        str.append(",b.COLUMN_NAME ");
        str.append(",b.DATA_TYPE ");
        str.append(",b.DATA_LENGTH ");
        str.append(",b.DATA_PRECISION ");
        str.append(",b.DATA_SCALE ");
        str.append(",a.COMMENTS ");
        str.append("FROM ");
        str.append("ALL_COL_COMMENTS a,ALL_TAB_COLUMNS b ");
        str.append("WHERE a.TABLE_NAME = b.TABLE_NAME ");
        str.append("AND a.OWNER = b.OWNER ");
        str.append("AND a.COLUMN_NAME = b.COLUMN_NAME ");
        str.append("AND ");
        str.append("a.OWNER='" + dbName + "' ");
        str.append("AND ");
        str.append("a.TABLE_NAME='" + tableName + "' ");
        return str.toString();
    }

    /**
     * 根据类型判断精度
     *
     * @param rs
     * @return
     */
    public TableStructureDTO conversionType(ResultSet rs) {
        try {
            TableStructureDTO dto = new TableStructureDTO();
            dto.fieldDes = rs.getString("COMMENTS");
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
        }
        return null;

    }


}
