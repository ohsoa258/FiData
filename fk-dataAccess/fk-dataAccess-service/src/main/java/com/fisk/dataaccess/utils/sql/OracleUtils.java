package com.fisk.dataaccess.utils.sql;

import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.DataBaseViewDTO;
import com.fisk.dataaccess.dto.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import com.fisk.dataaccess.enums.DriverTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lock
 * @version 1.0
 * @description Oracle工具类
 * @date 2022/1/10 10:20
 */
@Slf4j
public class OracleUtils {

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
            List<String> tableNames = getTables(conn, user.toUpperCase());
            Statement st = conn.createStatement();

            list = new ArrayList<>();

            int tag = 0;

            for (String tableName : tableNames) {
                ResultSet rs = st.executeQuery("select * from \" + tableName + \" OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY");

                List<TableStructureDTO> colNames = getColNames(rs);

                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(tableName);
                tablePyhNameDTO.setFields(colNames);

                tag++;
                tablePyhNameDTO.setTag(tag);

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
            List<String> viewNameList = loadViewNameList(driverTypeEnum, conn, user.toUpperCase());
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
        ArrayList<String> tablesList = null;
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
                colNameList.add(tableStructureDTO);
            }
            rs.close();
        } catch (SQLException e) {
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return colNameList;
    }
}
