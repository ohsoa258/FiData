package com.fisk.task.utils;

import com.alibaba.fastjson.JSONArray;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.*;
import java.util.Objects;

@Slf4j
@Component
public class PostgreHelper {
    //改类名称


    @Resource
    UserClient userClient;
    private static String pgsqlDatamodelUrl;
    private static String pgsqlDatamodelDriverClassName;
    private static String pgsqlDatamodelUsername;
    private static String pgsqlDatamodelPassword;
    private static String dataSourceOdsId;

    @Value("${pgsql-datamodel.url}")
    public void setPgsqlDatamodelUrl(String pgsqlDatamodelUrl) {
        PostgreHelper.pgsqlDatamodelUrl = pgsqlDatamodelUrl;
    }

    @Value("${pgsql-datamodel.driverClassName}")
    public void setPgsqlDatamodelDriverClassName(String pgsqlDatamodelDriverClassName) {
        PostgreHelper.pgsqlDatamodelDriverClassName = pgsqlDatamodelDriverClassName;
    }

    @Value("${pgsql-datamodel.username}")
    public void setPgsqlDatamodelUsername(String pgsqlDatamodelUsername) {
        PostgreHelper.pgsqlDatamodelUsername = pgsqlDatamodelUsername;
    }

    @Value("${pgsql-datamodel.password}")
    public void setPgsqlDatamodelPassword(String pgsqlDatamodelPassword) {
        PostgreHelper.pgsqlDatamodelPassword = pgsqlDatamodelPassword;
    }

    @Value("${fiData-data-ods-source}")
    public static void setDataSourceOdsId(String dataSourceOdsId) {
        PostgreHelper.dataSourceOdsId = dataSourceOdsId;
    }

    public Connection getConnection(BusinessTypeEnum businessTypeEnum) {
        Connection conn = null;
        try {
            if (Objects.equals(businessTypeEnum, BusinessTypeEnum.DATAMODEL)) {
                // 加载驱动类
                Class.forName(pgsqlDatamodelDriverClassName);
                conn = DriverManager.getConnection(pgsqlDatamodelUrl, pgsqlDatamodelUsername, pgsqlDatamodelPassword);
            } else if (Objects.equals(businessTypeEnum, BusinessTypeEnum.DATAINPUT)) {
                ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceOdsId));
                if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
                    DataSourceDTO data = fiDataDataSource.data;
                    // 加载驱动类
                    Class.forName(data.conType.getDriverName());
                    conn = DriverManager.getConnection(data.conStr, data.conAccount, data.conPassword);
                } else {
                    log.error("userclient无法查询到ods库的连接信息");
                    throw new FkException(ResultEnum.ERROR);
                }
            }

        } catch (ClassNotFoundException e) {
            log.error("找不到驱动程序类 ，加载驱动失败！" + StackTraceHelper.getStackTraceInfo(e));
        } catch (SQLException e) {
            log.error("数据库连接失败！" + StackTraceHelper.getStackTraceInfo(e));
        }
        return conn;
    }

    public <T> T postgreQuery(String executsql, BusinessTypeEnum businessTypeEnum, T data) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            // 1获得连接
            conn = getConnection(businessTypeEnum);
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

    public JSONArray postgreQuery(String executsql, BusinessTypeEnum businessTypeEnum) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        JSONArray objects = null;
        try {
            // 1获得连接
            conn = getConnection(businessTypeEnum);
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

    public void postgreUpdate(String executsql, BusinessTypeEnum businessTypeEnum) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 1获得连接
            conn = getConnection(businessTypeEnum);
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
     *
     * @param executsql
     * @param businessTypeEnum
     */
    public void postgreExecuteSql(String executsql, BusinessTypeEnum businessTypeEnum) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 1获得连接
            conn = getConnection(businessTypeEnum);
            // 2执行对象
            stmt = conn.createStatement();
            // 3执行,executeUpdate用来执行除了查询的操作,executeQuery用来执行查询操作
            stmt.executeUpdate(executsql);
        } catch (Exception e) {
            //捕捉错误
            log.error(e.getMessage());
            throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
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
                log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
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
                log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
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
                log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
            }
        }
    }
}
