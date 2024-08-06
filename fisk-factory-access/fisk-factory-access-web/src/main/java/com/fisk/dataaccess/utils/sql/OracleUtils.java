package com.fisk.dataaccess.utils.sql;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.dbdatatype.OracleTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.dataaccess.dto.table.DataBaseViewDTO;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
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

    /**
     * 获取oracle主键
     *
     * @param conn
     * @param dbName
     * @param tableName
     * @return
     */
    public static List<String> getTablePrimaryKey(Connection conn, String dbName, String tableName) {
        Statement st = null;
        List<String> list = new ArrayList<>();
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(buildSelectTablePrimaryKeySql(dbName, tableName));
            list = new ArrayList<>();
            while (rs.next()) {
                list.add(rs.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            log.error("【getTablePrimaryKey】获取表主键报错, ex", e);
            throw new FkException(ResultEnum.SQL_ERROR);
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
            AbstractDbHelper.closeStatement(st);
            AbstractDbHelper.closeConnection(conn);
        }
        return list;
    }

    public List<String> getAllDatabases(Connection conn) {
        List<String> dbName = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery("select OWNER from all_tables where OWNER not in ('SYS','SYSTEM')");
            while (resultSet.next()) {
                dbName.add(resultSet.getString("OWNER"));
            }
            if (!CollectionUtils.isEmpty(dbName)) {
                dbName = dbName.stream().distinct().collect(Collectors.toList());
            }
        } catch (SQLException e) {
            log.error("Oracle获取数据库失败,{}", e);
            throw new FkException(ResultEnum.GET_DATABASE_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(stmt);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return dbName;
    }

    /**
     * 获取表及表字段
     *
     * @param conn
     * @param user
     * @return 查询结果
     */
    public List<TablePyhNameDTO> getTableNameAndColumns(Connection conn, String user) {

        List<TablePyhNameDTO> list = new ArrayList<>();
        Statement st = null;
        try {
            // 获取数据库中所有表名称
            List<String> tableNames = getTables(conn, user.toUpperCase());
            if (CollectionUtils.isEmpty(tableNames)) {
                return null;
            }
            st = conn.createStatement();

            list = new ArrayList<>();
            for (String tableName : tableNames) {
                ResultSet rs = st.executeQuery("select * from " + tableName + " WHERE ROWNUM=1;");
                List<TableStructureDTO> colNames = getColNames(rs);
                if (CollectionUtils.isEmpty(colNames)) {
                    break;
                }
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(tableName);
                tablePyhNameDTO.setFields(colNames);
                list.add(tablePyhNameDTO);

            }
        } catch (SQLException e) {
            log.error("【getTableNameAndColumns】获取表名报错, ex", e);
            throw new FkException(ResultEnum.SQL_ERROR);
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

    private List<String> getTables(Connection conn) {
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
    public List<DataBaseViewDTO> loadViewDetails(Connection conn, String user) {

        List<DataBaseViewDTO> list = null;
        Statement st = null;
        try {
            // 获取数据库中所有视图名称
            List<String> viewNameList = loadViewNameList(conn, user.toUpperCase());
            if (CollectionUtils.isEmpty(viewNameList)) {
                return null;
            }
            st = conn.createStatement();

            list = new ArrayList<>();

            for (String viewName : viewNameList) {
                ResultSet resultSql = st.executeQuery("SELECT * FROM \"" + user.toUpperCase() + "\".\"" + viewName + "\" OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY");

                List<TableStructureDTO> colNames = getColNames(resultSql);

                DataBaseViewDTO dto = new DataBaseViewDTO();
                dto.viewName = viewName;
                dto.fields = colNames;

                list.add(dto);
            }
        } catch (SQLException e) {
            log.error("【getTableNameAndColumns】获取表名报错, ex", e);
            throw new FkException(ResultEnum.LOAD_VIEW_STRUCTURE_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }

        return list;
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
    private List<String> loadViewNameList(Connection conn, String user) {
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
            log.error("Oracle获取视图列表失败,{}", e);
            throw new FkException(ResultEnum.LOAD_VIEW_NAME_ERROR);
        }
        return viewNameList;
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
        } catch (SQLException e) {
            log.error("Oracle获取字段列表失败,{}", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
        }
        return colNameList;
    }

    /**
     * 读取Oracle表、字段信息
     *
     * @param conn
     * @param dbName
     * @return
     */
    public List<TablePyhNameDTO> getTableNameList(Connection conn, String dbName) {
        Statement st = null;
        List<TablePyhNameDTO> list = new ArrayList<>();
        try {
            // 获取数据库中所有表名称
            List<String> tableList = getTables(conn);
            if (CollectionUtils.isEmpty(tableList)) {
                return null;
            }
            st = conn.createStatement();
            list = new ArrayList<>();
            for (String tableName : tableList) {
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                /*ResultSet rs = st.executeQuery(this.buildUserSelectTableColumnSql(dbName));
                List<TableStructureDTO> colNameList = new ArrayList<>();
                while (rs.next()) {
                    colNameList.add(conversionType(rs));
                }
                tablePyhNameDTO.setFields(colNameList);*/
                tablePyhNameDTO.setTableName(tableName);
                list.add(tablePyhNameDTO);
            }
        } catch (SQLException e) {
            log.error("【getTableNameAndColumns】获取表名报错, ex", e);
            throw new FkException(ResultEnum.SQL_ERROR);
        } finally {
            AbstractDbHelper.closeStatement(st);
            AbstractDbHelper.closeConnection(conn);
        }
        return list;
    }

    /**
     * 读取Oracle表、字段信息
     *
     * @param conn
     * @param schemaName
     * @return
     */
    public List<TablePyhNameDTO> getTrueTableNameList(Connection conn, String schemaName, String dbName) {
        Statement st = null;
        List<TablePyhNameDTO> list = new ArrayList<>();
        try {
            // 获取数据库中所有表名称
            List<String> tableList = getTables(conn);
            if (CollectionUtils.isEmpty(tableList)) {
                return null;
            }
            st = conn.createStatement();
            list = new ArrayList<>();
            for (String tableName : tableList) {
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                log.info("开始获取oracle指定schema表下的字段：db" + schemaName + "tbl:" + tableName);
                ResultSet rs = st.executeQuery(this.buildUserSelectTableColumnSqlV2(schemaName, tableName));
                List<TableStructureDTO> colNameList = new ArrayList<>();
                while (rs.next()) {
                    colNameList.add(conversionTypeV2(rs, tableName, dbName));
                }
                tablePyhNameDTO.setFields(colNameList);
                //schema名字加表名
                tablePyhNameDTO.setTableName(schemaName + "." + tableName);
                list.add(tablePyhNameDTO);
                rs.close();
            }
        } catch (SQLException e) {
            log.error("【getTableNameAndColumns】获取表名报错, ex", e);
            throw new FkException(ResultEnum.SQL_ERROR);
        } finally {
            AbstractDbHelper.closeStatement(st);
            AbstractDbHelper.closeConnection(conn);
        }
        return list;
    }

    /**
     * 获取表字段信息
     *
     * @param conn
     * @param dbName
     * @param tableName
     * @return
     */
    public List<TableStructureDTO> getTableColumnInfoList(Connection conn, String dbName, String tableName) {
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(this.buildUserSelectTableColumnSql(dbName, tableName));
            List<TableStructureDTO> colNameList = new ArrayList<>();
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
     * 根据ALL_COL_COMMENTS表,拼接查询表字段信息sql
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
     * 根据user_tab_cols表,拼接查询表字段信息sql
     *
     * @param dbName
     * @param tableName
     * @return
     */
    public String buildUserSelectTableColumnSql(String dbName, String tableName) {
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
     * 根据user_tab_cols表,拼接查询表字段信息sql 查询字段是否主键
     *
     * @param dbName
     * @param tableName
     * @return
     */
    public String buildUserSelectTableColumnSqlV2(String dbName, String tableName) {
        String str = "SELECT " +
                "a.TABLE_NAME," +
                "a.DATA_PRECISION," +
                "a.DATA_SCALE," +
                "a.COLUMN_NAME," +
                "a.DATA_TYPE," +
                "a.DATA_LENGTH," +
                //加上字段是否是主键的判断
                "     CASE WHEN EXISTS (\n" +
                "        SELECT 1 \n" +
                "        FROM all_cons_columns k \n" +
                "        JOIN all_constraints c ON k.constraint_name = c.constraint_name AND k.owner = c.owner\n" +
                "        WHERE \n" +
                "            c.constraint_type = 'P' AND \n" +
                "            c.table_name = a.TABLE_NAME AND \n" +
                "            k.column_name = a.COLUMN_NAME AND \n" +
                "            c.status = 'ENABLED'\n" +
                "    ) THEN '1' ELSE '0' END AS IS_PRIMARY_KEY " +
                "FROM " +
                "user_tab_cols a " +
                "WHERE " +
                "a.TABLE_NAME='" + tableName + "' ";
        return str;
    }

    /**
     * 获取该用户下所有表sql
     *
     * @return
     */
    public String buildAllTableSql() {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append("table_name ");
        str.append("FROM ");
        str.append("user_tables");
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
            //dto.fieldDes = rs.getString("COMMENTS");
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

    /**
     * 根据类型判断精度
     *
     * @param rs
     * @return
     */
    public TableStructureDTO conversionTypeV2(ResultSet rs, String tableName, String dbName) {
        try {
            TableStructureDTO dto = new TableStructureDTO();
            //dto.fieldDes = rs.getString("COMMENTS");
            dto.fieldName = rs.getString("COLUMN_NAME");
            dto.fieldType = rs.getString("DATA_TYPE");
            dto.fieldLength = Integer.parseInt(rs.getString("DATA_LENGTH"));
            dto.fieldPrecision = 0;
            dto.sourceTblName = tableName;
            dto.sourceDbName = dbName;
            dto.setIsPk(Integer.valueOf(rs.getString("IS_PRIMARY_KEY")));
            OracleTypeEnum typeEnum = OracleTypeEnum.getValue(dto.fieldType);
//            switch (typeEnum) {
//                case NUMBER:
//                    if (rs.getString("DATA_PRECISION") == null) {
//                        dto.fieldLength = 0;
//                        dto.fieldPrecision = 0;
//                        break;
//                    }
//                    dto.fieldLength = Integer.parseInt(rs.getString("DATA_PRECISION"));
//                    dto.fieldPrecision = Integer.parseInt(rs.getString("DATA_SCALE"));
//                    break;
//                default:
//                    break;
//            }
            return dto;
        } catch (SQLException e) {
            log.error("conversionType ex:", e);
            throw new FkException(ResultEnum.DATA_OPS_SQL_EXECUTE_ERROR);
        }

    }


}
