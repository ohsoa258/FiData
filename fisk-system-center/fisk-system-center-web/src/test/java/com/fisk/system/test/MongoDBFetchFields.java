package com.fisk.system.test;

import java.sql.*;
import java.util.Properties;
  
public class MongoDBFetchFields {  
    public static void main(String[] args) {  
        String url = "jdbc:mongodb://192.168.21.21:27017/"; // 修改为你的MongoDB地址和端口
        String user = "fisk"; // 如果有身份验证的话
        String password = "password01!"; // 如果有身份验证的话
        String database = "Fisk_Test_Mongodb"; // 修改为你的数据库名称
        String collection = "orders"; // 修改为你的集合名称
  
        Properties props = new Properties();  
        props.put("user", user); // 如果需要身份验证的话设置用户名和密码  
        props.put("password", password); // 如果需要身份验证的话设置密码  
        Connection connection = null;  
        try {  
            connection = DriverManager.getConnection(url + database, props); // 连接MongoDB数据库  
            Statement stmt = connection.createStatement();  
            ResultSet rs = stmt.executeQuery("db." + collection + ".find()"); // 获取集合中的所有文档，实际上是为了获取字段名称  
            ResultSetMetaData rsmd = rs.getMetaData(); // 获取元数据，即字段信息
            int columnCount = rsmd.getColumnCount(); // 获取列数，即字段数量  
            for (int i = 1; i <= columnCount; i++) {  
                String columnName = rsmd.getColumnName(i); // 获取字段名称  
                System.out.println(columnName); // 输出字段名称  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            try {  
                if (connection != null && !connection.isClosed()) {  
                    connection.close(); // 关闭连接  
                }  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
    }  
}