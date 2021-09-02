package com.fisk.dataaccess.utils;

import com.fisk.dataaccess.dto.TablePyhNameDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lock
 * <p>
 * SqlServer 获取表及表字段
 */
@Slf4j
public class SqlServerConUtils {

    private static Connection conn = null;
    private static Statement stmt = null;

    /**
     * 查询获取数据库所有表名
     *
     * @param conn conn
     * @return 表名
     */
    public List<String> getTables(Connection conn,String dbName) {
        ArrayList<String> tableList = null;
        try {
            Statement stmt = conn.createStatement();

            ResultSet resultSet = stmt.executeQuery("SELECT name FROM "+dbName+"..sysobjects Where xtype='U' ORDER BY name");
            tableList = new ArrayList<>();
            while (resultSet.next()) {
                tableList.add(resultSet.getString("name"));
            }
//            System.out.println(tableList);

        } catch (SQLException e) {
            log.error("【getTables】获取表名报错, ex", e);
            return null;
        }
        return tableList;
    }


    /**
     * 查询获取数据库表的所有字段
     *
     * @param tableName tableName
     * @return 表字段
     */
    public List<String> getColumnsName(String tableName) {
        List<String> colNameList = null;
        try {
            // SELECT * FROM syscolumns WHERE id=Object_Id('表名');
            ResultSet resultSet = stmt.executeQuery("SELECT name FROM syscolumns WHERE id=Object_Id('" + tableName + "');");

            colNameList = new ArrayList<>();

            while (resultSet.next()) {

                String name = resultSet.getString("name");
                colNameList.add(name);
            }
//            System.out.println(colNameList);

            return colNameList;
        } catch (Exception e) {
            log.error("【getColumnsName】获取表字段报错, ex", e);
            return null;
        }
    }

    /**
     * 获取表及表字段
     *
     * @param url      url
     * @param user     user
     * @param password password
     * @return 表及表字段
     */
    public List<TablePyhNameDTO> getTableNameAndColumns(String url, String user, String password,String dbName) {

        List<TablePyhNameDTO> list = null;

        try {
            //1.加载驱动程序
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            //2.获得数据库的连接
            conn = (Connection) DriverManager.getConnection(url, user, password);
            stmt = conn.createStatement();
            list = new ArrayList<>();

            // 获取指定数据库所有表
            List<String> tableNames = this.getTables(conn,dbName);

            int tag = 0;
            for (String tableName : tableNames) {
                List<String> columnsName = getColumnsName(tableName);
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                tablePyhNameDTO.setTableName(tableName);
                tablePyhNameDTO.setFields(columnsName);

                tag++;
                tablePyhNameDTO.setTag(tag);
                list.add(tablePyhNameDTO);
            }

            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            log.error("【getTableNameAndColumns】获取表及表字段报错, ex", e);
            return null;
        }
        return list;
    }

}
