package com.fisk.dataaccess.utils;

import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.TablePyhNameDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
public class MysqlConUtils {

    /**
     * 获取实时及非实时的表 表字段
     * @param url url
     * @param user user
     * @param pwd pwd
     * @return 查询结果
     */
    public Map<String, List<String>> getTable(String url,String user,String pwd) {


        Map<String, List<String>> map = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, pwd);
            List<String> tableNames = getTables(conn);
//        System.out.println(tableNames);
//        System.out.println("====================================================");
            Statement st = conn.createStatement();

            map = new HashMap<>(1000);
            for (String tableName : tableNames) {
                ResultSet rs = st.executeQuery("select * from " + tableName);
    //            System.out.print(tableName+":  ");
                List<String> colNames = getColNames(rs);
    //            System.out.println(colNames);

    //            System.out.println("==================================================");

                map.put(tableName, colNames);
    //            System.out.println(map);
                rs.close();
            }

            st.close();
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }

        return map;
    }

    /**
     * 获取非实时 表及表字段
     * @param url url
     * @param user user
     * @param pwd pwd
     * @return 查询结果
     */
    public List<TablePyhNameDTO> getnrttable(String url, String user, String pwd){

        List<TablePyhNameDTO> list = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, pwd);
            List<String> tableNames = getTables(conn);
            Statement st = conn.createStatement();

            list = new ArrayList<>();

            int tag = 0;

            for (String tableName : tableNames) {
                ResultSet rs = st.executeQuery("select * from " + tableName);

                List<String> colNames = getColNames(rs);

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
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }

        return list;
    }

    /**
     * 获取数据库中所有表名称
     *
     * @param conn conn
     * @return 返回值
     */
    private List<String> getTables(Connection conn){
        ArrayList<String> tablesList = null;
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet tables = databaseMetaData.getTables(null, null, "%", null);
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
     * 获取表中所有字段名称
     *
     * @param rs rs
     */
    private List<String> getColNames(ResultSet rs){
        List<String> colNameList = null;
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            int count = metaData.getColumnCount();

            colNameList = new ArrayList<String>();
            for (int i = 1; i <= count; i++) {
                colNameList.add(metaData.getColumnName(i));
            }
            // 打印
//        System.out.println(colNameList);
//		rs.close();
            rs.first();
        } catch (SQLException e) {
            throw new FkException(ResultEnum.DATAACCESS_GETFIELD_ERROR);
        }
        return colNameList;
    }

}
