package com.fisk.common.service.dbMetaData.utils;

import com.fisk.common.service.dbMetaData.dto.DataBaseViewDTO;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEnum;
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
     * 获取表及表字段
     *
     * @param url      url
     * @param user     user
     * @param password password
     * @return 查询结果
     */
    public List<TablePyhNameDTO> getTableNameAndColumns(String url, String user, String password, DriverTypeEnum driverTypeEnum) {

        List<TablePyhNameDTO> list = null;
        try {
            Class.forName(driverTypeEnum.getName());
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
        } catch (ClassNotFoundException | SQLException e) {
            log.error("【getTableNameAndColumns】获取表名报错, ex", e);
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }

        return list;
    }

    /**
     * @return java.util.List<com.fisk.dataaccess.dto.DataBaseViewDTO>
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
            List<String> viewNameList = loadViewNameList(driverTypeEnum, conn, dbName);
            Statement st = conn.createStatement();

            list = new ArrayList<>();

            for (String viewName : viewNameList) {
//                ResultSet resultSql = st.executeQuery("select * from " + viewName + ";");

                List<TableStructureDTO> colNames = getColNames(st, viewName);

                DataBaseViewDTO dto = new DataBaseViewDTO();
                dto.viewName = viewName;
                dto.fields = colNames;
                // 关闭当前结果集
//                resultSql.close();

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
    public List<String> getTables(Connection conn) {
        ArrayList<String> tablesList = null;
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet tables = databaseMetaData.getTables(null, null, "%",  new String[]{"TABLE"});
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
     * @params dbName
     */
    private List<String> loadViewNameList(DriverTypeEnum driverTypeEnum, Connection conn, String dbName) {
        ArrayList<String> viewNameList = null;
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            String[] types = {"VIEW"};

            ResultSet rs = null;
            switch (driverTypeEnum) {
                case MYSQL:
                    rs = databaseMetaData.getTables(null, null, "%", types);
                    break;
                case SQLSERVER:
                    rs = databaseMetaData.getTables(null, null, dbName + "%", types);
                    break;
                default:
                    break;
            }
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
     * 获取表中所有字段名称
     *
     * @param tableName tableName
     */
    public List<TableStructureDTO> getColNames(Statement st, String tableName) {
        ResultSet rs = null;
        List<TableStructureDTO> colNameList = null;
        try {
            rs = st.executeQuery("select * from " + tableName + " LIMIT 0,10;");

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
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }

        return colNameList;
    }

}
