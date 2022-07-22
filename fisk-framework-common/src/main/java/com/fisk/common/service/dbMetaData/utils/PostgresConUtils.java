package com.fisk.common.service.dbMetaData.utils;

import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbMetaData.dto.DataBaseViewDTO;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dick
 * @version 1.0
 * @description Postgres 获取表及表字段
 * @date 2022/4/22 14:46
 */
@Slf4j
public class PostgresConUtils {
    /**
     * 获取表及表字段
     *
     * @param url      url
     * @param user     user
     * @param password password
     * @return 查询结果
     */
    public List<TablePyhNameDTO> getTableNameAndColumns(String url, String user, String password, DriverTypeEnum driver) {

        List<TablePyhNameDTO> list = null;
        try {
            Class.forName(driver.getName());
            Connection conn = DriverManager.getConnection(url, user, password);
            // 获取数据库中所有表名称
            List<String> tableNames = getTables(conn);
            Statement st = conn.createStatement();

            list = new ArrayList<>();

            int tag = 0;

            for (String tableName : tableNames) {

                List<TableStructureDTO> colNames = getColNames(st, tableName);

                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(tableName);
                tablePyhNameDTO.setFields(colNames);

                tag++;
                tablePyhNameDTO.setTag(tag);
                list.add(tablePyhNameDTO);

            }
            st.close();
            conn.close();
        } catch (ClassNotFoundException e) {
            System.out.println("找不到驱动程序类 ，加载驱动失败！");
            throw new FkException(ResultEnum.CREATE_PG_CONNECTION);
        } catch (SQLException e) {
            log.error("【PostgresConUtils/getTableNameAndColumns】建立pg数据库连接异常, ex", e);
            throw new FkException(ResultEnum.PG_CONNECT_ERROR);
        }
        return list;
    }

    /**
     * @return java.util.List<com.fisk.dataaccess.dto.DataBaseViewDTO>
     * @description 加载视图详情
     * @author dick
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
            List<String> viewNameList = loadViewNameList(conn);
            Statement st = conn.createStatement();

            list = new ArrayList<>();
            for (String viewName : viewNameList) {
                List<TableStructureDTO> colNames = getViewColumns(conn, viewName);
                DataBaseViewDTO dto = new DataBaseViewDTO();
                dto.viewName = viewName;
                dto.fields = colNames;
                list.add(dto);
            }
            st.close();
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            log.error("【loadViewDetails】获取视图信息报错, ex", e);
            throw new FkException(ResultEnum.LOAD_VIEW_STRUCTURE_ERROR);
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
            throw new FkException(ResultEnum.LOAD_VIEW_NAME_ERROR);
        }
        return viewNameList;
    }

    /**
     * 获取数据库中所有表名称
     *
     * @return 返回值
     */
    public List<String> getTableList(String url, String user, String password, String driver) {
        List<String> tableNames = null;
        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, user, password);
            tableNames = getTables(conn);
            conn.close();
        } catch (ClassNotFoundException e) {
            System.out.println("找不到驱动程序类 ，加载驱动失败！");
            throw new FkException(ResultEnum.CREATE_PG_CONNECTION);
        } catch (SQLException e) {
            log.error("【PostgresConUtils/getTableList】建立pg数据库连接异常, ex", e);
            throw new FkException(ResultEnum.PG_READ_TABLE_ERROR);
        }
        return tableNames;
    }

    /**
     * 获取数据库某张表的字段
     *
     * @return 返回值
     */
    public Map<String, List<TableStructureDTO>> getTableColumnList(String url, String user,
                                                                   String password, String driver, List<String> tableNames) {
        Map<String, List<TableStructureDTO>> map = new IdentityHashMap<>();
        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement st = conn.createStatement();
            for (String tableName : tableNames) {
                List<TableStructureDTO> colNames = getColNames(st, tableName);
                map.put(tableName, colNames);
            }
            st.close();
            conn.close();
        } catch (ClassNotFoundException e) {
            System.out.println("找不到驱动程序类 ，加载驱动失败！");
            throw new FkException(ResultEnum.CREATE_PG_CONNECTION);
        } catch (SQLException e) {
            log.error("【PostgresConUtils/getTableColumnList】建立pg数据库连接异常, ex", e);
            throw new FkException(ResultEnum.PG_READ_FIELD_ERROR);
        }
        return map;
    }


    /**
     * 获取数据库中所有表名称
     *
     * @param conn conn
     * @return 返回值
     */
    public List<String> getTables(Connection conn) {
        ArrayList<String> tablesList = null;
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet tables = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"});
            tablesList = new ArrayList<String>();
            while (tables.next()) {
                tablesList.add(tables.getString("TABLE_NAME"));
            }
            tables.close();
        } catch (SQLException e) {
            log.error("【PostgresConUtils/getTables】读取表信息异常, ex", e);
            throw new FkException(ResultEnum.PG_READ_TABLE_ERROR);
        }
        return tablesList;
    }

    /**
     * 获取表中所有字段名称
     *
     * @param tableName tableName
     */
    public List<TableStructureDTO> getColNames(Statement st, String tableName) {
        ResultSet rs = null;
        List<TableStructureDTO> colNameList = null;
        try {
            rs = st.executeQuery("select * from " + tableName + " LIMIT 0;");

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
            log.error("【PostgresConUtils/getColNames】读取字段信息异常, ex", e);
            throw new FkException(ResultEnum.PG_READ_FIELD_ERROR);
        }

        return colNameList;
    }

    /**
     * 获取指定表视图字段信息
     *
     * @param viewName
     * @return
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
        } catch (Exception e) {
            log.error("【PostgresConUtils/getViewColumns】读取字段信息异常, ex", e);
            throw new FkException(ResultEnum.PG_READ_FIELD_ERROR);
        }
        return colNameList;
    }
}
