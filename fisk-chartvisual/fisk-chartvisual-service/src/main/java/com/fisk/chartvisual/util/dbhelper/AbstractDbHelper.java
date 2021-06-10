package com.fisk.chartvisual.util.dbhelper;

import com.fisk.chartvisual.util.dbhelper.buildsql.BuildMySqlCommandImpl;
import com.fisk.chartvisual.util.dbhelper.buildsql.BuildSqlServerCommandImpl;
import com.fisk.chartvisual.util.dbhelper.buildsql.IBuildSQLCommand;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.utils.BeanHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * 数据源连接接口
 *
 * @author gy
 */
@Slf4j
public abstract class AbstractDbHelper {

    private final DataSourceTypeEnum type;

    public AbstractDbHelper(DataSourceTypeEnum type) {
        this.type = type;
    }


    /**
     * 连接
     */
    public Connection connection(String connectionStr, String acc, String pwd) {
        try {
            loadDriver();
            return getConnectionByType(connectionStr, acc, pwd);
        } catch (SQLException e) {
            log.error("【connection】数据库连接获取失败, ex", e);
            throw new FkException(ResultEnum.VISUAL_CONNECTION_ERROR, e.getLocalizedMessage());
        } catch (Exception e) {
            log.error("【connection】" + type.getName() + "数据库驱动加载失败, ex", e);
            throw new FkException(ResultEnum.VISUAL_LOADDRIVER_ERROR, e.getLocalizedMessage());
        }
    }

    /**
     * 执行查询
     *
     * @param sql 查询语句
     * @param con 数据库连接
     * @return 查询结果List
     */
    public <T> List<T> execQueryResultList(String sql, Connection con, Class<T> tClass) {
        return query(sql, con, e -> BeanHelper.resultSetToList(e, tClass));
    }

    /**
     * 执行查询
     *
     * @param sql 查询语句
     * @param con 数据库连接
     * @return 查询结果Map
     */
    public List<Map<String, Object>> execQueryResultMap(String sql, Connection con) {
        return query(sql, con, BeanHelper::resultSetToMaps);
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

    /* ---------------------------------------------------- */

    /**
     * 根据连接类型获取数据库连接
     *
     * @param connectionStr 连接字符串
     * @param acc           账号
     * @param pwd           密码
     * @return 数据源连接
     * @throws Exception 数据源连接异常
     */
    private Connection getConnectionByType(String connectionStr, String acc, String pwd) throws Exception {
        switch (type) {
            case SQLSERVER:
                return DriverManager.getConnection(connectionStr);
            case MYSQL:
                return DriverManager.getConnection(connectionStr, acc, pwd);
            default:
                return null;
        }
    }

    /**
     * 加载驱动
     *
     * @throws Exception 驱动加载异常
     */
    private void loadDriver() throws Exception {
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

    private <T> List<T> query(String sql, Connection con, Function<ResultSet, List<T>> func) {
        Statement st = null;
        String code = UUID.randomUUID().toString();
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            log.info("【execQuery】【" + code + "】执行sql: 【" + sql + "】");
            st = con.createStatement();
            ResultSet res = st.executeQuery(sql);
            List<T> data = func.apply(res);
            log.info("【execQuery】【" + code + "】Total: 【" + (data == null ? 0 : data.size()) + "】");
            return data;
        } catch (SQLException ex) {
            log.error("【execQuery】【" + code + "】执行sql查询报错, ex", ex);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, ex.getLocalizedMessage());
        } finally {
            closeStatement(st);
            stopWatch.stop();
            log.info("【execQuery】【" + code + "】执行时间: 【" + stopWatch.getTotalTimeMillis() + "毫秒】");
        }
    }
}
