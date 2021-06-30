package com.fisk.dataaccess.utils;

import org.aspectj.lang.annotation.Before;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Lock
 *
 * SqlServer 连接工具
 */
public class SqlServerConUtils {

    //这里可以设置数据库名称
    private final static String URL = "jdbc:sqlserver://192.168.1.35:1433";
    private static final String USER="sa";
    private static final String PASSWORD="password01!";
    private static Connection conn= null;
    private static Statement stmt = null;

    /**
     *加载驱动、连接数据库
     */
//    @Before
    public void init(){
        try {
            //1.加载驱动程序
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            //2.获得数据库的连接
            conn=(Connection) DriverManager.getConnection(URL,USER,PASSWORD);
            stmt = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询获取所有数据库名
     */
//    @Test
    public void findAllDatabases(){
        try {
            ResultSet resultSet = stmt.executeQuery("SELECT name FROM  master..sysdatabases WHERE name NOT IN ( 'master', 'model', 'msdb', 'tempdb', 'northwind','pubs' )");
            while(resultSet.next()){//如果对象中有数据，就会循环打印出来
                System.out.println(resultSet.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询获取数据库所有表名
     */
//    @Test
    public void findAllTables(){
        try {
            ResultSet resultSet = stmt.executeQuery("SELECT name FROM studb..sysobjects Where xtype='U' ORDER BY name");
            while(resultSet.next()){//如果对象中有数据，就会循环打印出来
                System.out.println(resultSet.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 查询获取数据库表的所有字段
     */
//    @Test
    public void findAllColumns(){

        try {
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM syscolumns WHERE id=Object_Id('表名')");
//            while(resultSet.next()){//如果对象中有数据，就会循环打印出来
//                System.out.println(resultSet.getString("name"));
//            }
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 0; i < columnCount; i++){
                //list.add(metaData.getColumnName(i + 1));
                System.out.println(metaData.getColumnName(i + 1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
