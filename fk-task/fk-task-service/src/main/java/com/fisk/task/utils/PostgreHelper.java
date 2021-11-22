package com.fisk.task.utils;

import com.alibaba.fastjson.JSONArray;
import com.fisk.common.enums.task.BusinessTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;
@Slf4j
@Component
public class PostgreHelper {

    private static String pgsqlDatamodelUrl;
    private static String pgsqlDriverClassName;
    private static String pgsqlDatainputUrl;
    private static String pgsqlUsername;
    private static String pgsqlPassword;

    @Value("${pgsql-datamodel.url}")
    public void setPgsqlDatamodelUrl(String pgsqlDatamodelUrl) {
        PostgreHelper.pgsqlDatamodelUrl = pgsqlDatamodelUrl;
    }
    @Value("${pgsql-datainput.driverClassName}")
    public void setPgsqlDriverClassName(String pgsqlDriverClassName) {
        PostgreHelper.pgsqlDriverClassName = pgsqlDriverClassName;
    }
    @Value("${pgsql-datainput.url}")
    public void setPgsqlDatainputUrl(String pgsqlDatainputUrl) {
        PostgreHelper.pgsqlDatainputUrl = pgsqlDatainputUrl;
    }
    @Value("${pgsql-datainput.username}")
    public void setPgsqlUsername(String pgsqlUsername) {
        PostgreHelper.pgsqlUsername = pgsqlUsername;
    }
    @Value("${pgsql-datainput.password}")
    public void setPgsqlPassword(String pgsqlPassword) {
        PostgreHelper.pgsqlPassword = pgsqlPassword;
    }

    public static Connection getConnection(String url) {
        Connection conn = null;
        try {
            // 加载驱动类
            Class.forName(pgsqlDriverClassName);
            conn = DriverManager.getConnection(url, pgsqlUsername, pgsqlPassword);
        } catch (ClassNotFoundException e) {
            System.out.println("找不到驱动程序类 ，加载驱动失败！");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("数据库连接失败！");
            e.printStackTrace();
        }
        return conn;
    }

    public static <T> T postgreQuery(String executsql, BusinessTypeEnum businessTypeEnum, T data) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            // 1获得连接
            conn = getConnection(businessTypeEnum==BusinessTypeEnum.DATAMODEL?pgsqlDatamodelUrl:pgsqlDatainputUrl);
            // 2执行对象
            stmt = conn.createStatement();
            // 3执行,executeUpdate用来执行除了查询的操作,executeQuery用来执行查询操作
            resultSet = stmt.executeQuery(executsql);
            //转换为对象
            data = ResultSetHelper.bindDataToDTO(resultSet, data);
            /*JSONArray objects = JsonHelper.resultSetToJsonArry(resultSet);
            JSON.parseArray();*/
        } catch (Exception e) {
            //捕捉错误
            log.error(e.getMessage());
        } finally {
            //关闭操作对象
            PostgreHelper.closeStatement(stmt);
            //关闭连接
            PostgreHelper.closeConn(conn);
            //关闭结果集
            PostgreHelper.closeResultSet(resultSet);
        }
        return data;
    }

    public static JSONArray postgreQuery(String executsql, BusinessTypeEnum businessTypeEnum) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        JSONArray objects=null;
        try {
            // 1获得连接
            conn = getConnection(businessTypeEnum==BusinessTypeEnum.DATAMODEL?pgsqlDatamodelUrl:pgsqlDatainputUrl);
            // 2执行对象
            stmt = conn.createStatement();
            // 3执行,executeUpdate用来执行除了查询的操作,executeQuery用来执行查询操作
            resultSet = stmt.executeQuery(executsql);

            objects = ResultSetHelper.resultSetToJsonArry(resultSet);


        } catch (Exception e) {
            //捕捉错误
            log.error(e.getMessage());
        } finally {
            //关闭操作对象
            PostgreHelper.closeStatement(stmt);
            //关闭连接
            PostgreHelper.closeConn(conn);
            //关闭结果集
            PostgreHelper.closeResultSet(resultSet);
        }
        return objects;
    }

    public static void postgreUpdate(String executsql, BusinessTypeEnum businessTypeEnum) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 1获得连接
            conn = getConnection(businessTypeEnum==BusinessTypeEnum.DATAMODEL?pgsqlDatamodelUrl:pgsqlDatainputUrl);
            // 2执行对象
            stmt = conn.createStatement();
            // 3执行,executeUpdate用来执行除了查询的操作,executeQuery用来执行查询操作
            stmt.executeUpdate(executsql);
        } catch (Exception e) {
            //捕捉错误
            log.error(e.getMessage());
        } finally {
            //关闭操作对象
            PostgreHelper.closeStatement(stmt);
            //关闭连接
            PostgreHelper.closeConn(conn);
        }
    }

    /**
     * 执行pgsql语句 2021年08月27日10:29:12 Dennyhui
     * @param executsql
     * @param businessTypeEnum
     */
    public static void postgreExecuteSql(String executsql, BusinessTypeEnum businessTypeEnum) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 1获得连接
            conn = getConnection(businessTypeEnum==BusinessTypeEnum.DATAMODEL?pgsqlDatamodelUrl:pgsqlDatainputUrl);
            // 2执行对象
            stmt = conn.createStatement();
            // 3执行,executeUpdate用来执行除了查询的操作,executeQuery用来执行查询操作
            stmt.executeUpdate(executsql);
        } catch (Exception e) {
            //捕捉错误
            log.error(e.getMessage());
        } finally {
            //关闭操作对象
            PostgreHelper.closeStatement(stmt);
            //关闭连接
            PostgreHelper.closeConn(conn);
        }
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
