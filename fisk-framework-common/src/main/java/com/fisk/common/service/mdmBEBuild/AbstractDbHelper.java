package com.fisk.common.service.mdmBEBuild;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.sql.*;
import java.util.UUID;

/**
 * @author WangYan
 * @date 2022/4/13 15:57
 */
@Slf4j
public class AbstractDbHelper {

    /**
     * 连接
     */
    public Connection connection(String connectionStr, String acc, String pwd, DataSourceTypeEnum type) {
        try {
            loadDriver(type);
            Connection connection = getConnectionByType(connectionStr, acc, pwd,type);
            log.info("【connection】数据库连接成功, 连接信息【" + connectionStr + "】");
            return connection;
        } catch (SQLException e) {
            log.error("【connection】数据库连接获取失败, ex", e);
            throw new FkException(ResultEnum.VISUAL_CONNECTION_ERROR, e.getLocalizedMessage());
        } catch (Exception e) {
            log.error("【connection】" + type.getName() + "数据库驱动加载失败, ex", e);
            throw new FkException(ResultEnum.VISUAL_LOADDRIVER_ERROR, e.getLocalizedMessage());
        }
    }

    /**
     * 加载驱动
     *
     * @throws Exception 驱动加载异常
     */
    private void loadDriver(DataSourceTypeEnum type) throws Exception {
        if (type != null) {
            try {
                Class.forName(type.getDriverName());
            } catch (ClassNotFoundException e) {
                throw new Exception("【loadDriver】" + type.getName() + "驱动加载失败, ex", e);
            }
        } else {
            throw new Exception("【loadDriver】错误的驱动类型");
        }
    }

    /**
     * 根据连接类型获取数据库连接
     *
     * @param connectionStr 连接字符串
     * @param acc           账号
     * @param pwd           密码
     * @return 数据源连接
     * @throws Exception 数据源连接异常
     */
    private Connection getConnectionByType(String connectionStr, String acc, String pwd,DataSourceTypeEnum type) throws Exception {
        switch (type) {
            case SQLSERVER:
                return DriverManager.getConnection(connectionStr);
            case PG:
                return DriverManager.getConnection(connectionStr, acc, pwd);
            default:
                return null;
        }
    }

    /**
     * 关闭连接
     *
     * @param connection 连接对象
     */
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                log.info("【connection】数据库连接已关闭");
            } catch (SQLException ex) {
                log.error("【closeConnection】数据库连接关闭失败, ex", ex);
            }
        }
    }

    /**
     * 关闭Statement对象
     *
     * @param st Statement对象
     */
    public void closeStatement(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException ex) {
                log.error("【closeStatement】数据库操作对象关闭失败, ex", ex);
            }
        }
    }

    /**
     * 执行sql
     * @param sql
     * @param connection
     * @return
     */
    public void executeSql(String sql,Connection connection){
        Statement statement =null;
        String code = UUID.randomUUID().toString();
        StopWatch stopWatch = new StopWatch();

        try {
            stopWatch.start();
            log.info("【execCreate】【" + code + "】执行sql: 【" + sql + "】");
            statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException ex) {
            log.error("【execCreate】【" + code + "】执行sql查询报错, ex", ex);
            throw new FkException(ResultEnum.VISUAL_CREATE_ERROR, ex.getLocalizedMessage());
        }finally {
            closeStatement(statement);
            closeConnection(connection);
            stopWatch.stop();
            log.info("【execCreate】【" + code + "】执行时间: 【" + stopWatch.getTotalTimeMillis() + "毫秒】");
        }
    }
}
