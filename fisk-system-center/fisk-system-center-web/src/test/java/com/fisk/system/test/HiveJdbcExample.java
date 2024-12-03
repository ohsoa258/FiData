package com.fisk.system.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class HiveJdbcExample {
    private static String driverName = "org.apache.hive.jdbc.HiveDriver";
    private static String url = "jdbc:hive2://192.168.11.136:10001/default"; // 修改为您的Hive服务器地址和端口
    private static String user = "root"; // 修改为您的用户名
    private static String password = "root123"; // 修改为您的密码

    public static void main(String[] args) {
        try {
            // 加载Hive JDBC驱动
            Class.forName(driverName);

            // 创建连接
            Connection connection = DriverManager.getConnection(url, user, password);

            // 创建Statement对象
            Statement statement = connection.createStatement();

            // 执行查询
            String sql = "show databases"; // 修改为您要查询的表
            ResultSet resultSet = statement.executeQuery(sql);

            // 处理结果集
            while (resultSet.next()) {
                System.out.println("Column1: " + resultSet.getString(1));
            }

            // 关闭资源
            resultSet.close();
            statement.close();
            connection.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
