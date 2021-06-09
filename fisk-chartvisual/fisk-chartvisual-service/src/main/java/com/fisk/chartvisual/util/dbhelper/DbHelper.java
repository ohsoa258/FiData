package com.fisk.chartvisual.util.dbhelper;

import com.fisk.chartvisual.vo.DataSourceConVO;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * @author gy
 */
public class DbHelper {

    /**
     * 执行sql，返回结果
     *
     * @param sql   执行的sql语句
     * @param model 数据源连接信息
     * @return 查询结果
     */
    public static List<Map<String, Object>> execQueryResultMap(String sql, DataSourceConVO model) {
        AbstractDbHelper db = DbHelperFactory.getDbHelper(model.conType);
        Connection connection = db.connection(model.conStr, model.conAccount, model.conPassword);
        List<Map<String, Object>> data = db.execQueryResultMap(sql, connection);
        db.closeConnection(connection);
        return data;
    }

    /**
     * 执行sql，返回结果
     *
     * @param sql   执行的sql语句
     * @param model 数据源连接信息
     * @return 查询结果
     */
    public static <T> List<T> execQueryResultList(String sql, DataSourceConVO model, Class<T> tClass) {
        AbstractDbHelper db = DbHelperFactory.getDbHelper(model.conType);
        Connection connection = db.connection(model.conStr, model.conAccount, model.conPassword);
        List<T> data = db.execQueryResultList(sql, connection, tClass);
        db.closeConnection(connection);
        return data;
    }
}
