package com.fisk.system.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PIJdbcExample {
    public static void main(String[] args) {
        // 连接字符串
        String url = "jdbc:pidb://your-pi-server:5460;Database=your-database";
        String user = "your-username";
        String password = "your-password";

        // 加载PI JDBC驱动程序
        try {
            Class.forName("com.osisoft.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("PI JDBC驱动程序未找到");
            e.printStackTrace();
            return;
        }

        // 建立连接
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM your_table")) {

            // 处理结果集
            while (rs.next()) {
                System.out.println(rs.getString("column_name"));
            }
        } catch (SQLException e) {
            System.out.println("连接或查询失败");
            e.printStackTrace();
        }
    }
}
