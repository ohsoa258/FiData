package com.fisk.dataservice.utils.database;

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

import static com.fisk.dataservice.doris.DorisDataSource.*;

/**
 * @author WangYan
 * @date 2021/12/1 20:00
 * 数据库连接器
 */
@Slf4j
public class DatabaseConnect {

    /**
     * 数据库连接器
     * @param sql 创建的sql
     * @return 查询结果
     */
    public static List<Map<String, Object>> execQueryResultList(String sql) {
        Connection connection = connection(URL, USER, PASSWORD);
        List<Map<String, Object>> data = execQueryResultMaps(sql, connection);
        return data;
    }

    /**
     * 连接
     */
    public static Connection connection(String connectionStr, String acc, String pwd) {
        try {
            Class.forName(DRIVER);
            Connection connection = DriverManager.getConnection(connectionStr,acc,pwd);
            log.info("【connection】数据库连接成功, 连接信息【" + connectionStr + "】");
            return connection;
        } catch (SQLException e) {
            log.error("【connection】数据库连接获取失败, ex", e);
            throw new FkException(ResultEnum.VISUAL_CONNECTION_ERROR, e.getLocalizedMessage());
        } catch (Exception e) {
            log.error("【connection】" + "MySQL" + "数据库驱动加载失败, ex", e);
            throw new FkException(ResultEnum.VISUAL_LOADDRIVER_ERROR, e.getLocalizedMessage());
        }
    }

    /**
     * 执行查询
     *
     * @param sql 查询语句
     * @param con 数据库连接
     * @return 查询结果Map
     */
    public static List<Map<String, Object>> execQueryResultMaps(String sql, Connection con) {
        return query(sql, con, BeanHelper::resultSetToMaps);
    }


    /**
     * 执行sql
     * @param sql sql
     * @param con 连接器
     * @param func<T>
     * @return
     */
    private static <T> T query(String sql, Connection con, Function<ResultSet, T> func) {
        Statement st = null;
        String code = UUID.randomUUID().toString();
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            log.info("【execQuery】【" + code + "】执行sql: 【" + sql + "】");
            st = con.createStatement();
            ResultSet res = st.executeQuery(sql);
            return func.apply(res);
        } catch (SQLException ex) {
            log.error("【execQuery】【" + code + "】执行sql查询报错, ex", ex);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, ex.getLocalizedMessage());
        } finally {
            closeStatement(st);
            stopWatch.stop();
            log.info("【execQuery】【" + code + "】执行时间: 【" + stopWatch.getTotalTimeMillis() + "毫秒】");
        }
    }

    /**
     * 关闭Statement对象
     *
     * @param st Statement对象
     */
    public static void closeStatement(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException ex) {
                log.error("【closeStatement】数据库操作对象关闭失败, ex", ex);
            }
        }
    }
}
