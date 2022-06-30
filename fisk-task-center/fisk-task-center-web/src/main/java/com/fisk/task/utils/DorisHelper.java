package com.fisk.task.utils;

import java.sql.*;

/**
 * @Author:DennyHui
 * CreateTime: 2021/6/30 16:13
 * Description:
 */
public class DorisHelper {
    //DorisSqlHelper类
    //定义了数据库连接函数，关闭查询结果集，关闭Statement对象，关闭数据库连接
    //这样的做法是执行上述4个操作时可以直接调用函数（面向对象的思想），可以好好理解一下
    public static Connection getConnection(String _jdbcurl,String _driver,String _username,String _pwd) {
        Connection conn = null;
        // 驱动
        String driver = _driver;
        // SqlServer链接地址
        String url = _jdbcurl;
        // 用户名
        String username = _username;
        // 密码
        String password = _pwd;
        try {
            // 加载驱动类
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            System.out.println("找不到驱动程序类 ，加载驱动失败！" + e);
        } catch (SQLException e) {
            System.out.println("数据库连接失败！" + e);
        }
        return conn;
    }

    //关闭连接
    public static void closeConn(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    //关闭执行对象
    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    //关闭结果集
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
