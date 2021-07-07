package com.fisk.chartvisual.util.dbhelper;

import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.util.dbhelper.buildsql.IBuildSqlCommand;
import com.fisk.chartvisual.vo.DataServiceResult;
import com.fisk.chartvisual.vo.DataSourceConVO;
import com.fisk.common.enums.chartvisual.InteractiveTypeEnum;

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
    public static List<Map<String, Object>> execQueryResultMaps(String sql, DataSourceConVO model) {
        AbstractDbHelper db = DbHelperFactory.getDbHelper(model.conType);
        Connection connection = db.connection(model.conStr, model.conAccount, model.conPassword);
        List<Map<String, Object>> data = db.execQueryResultMaps(sql, connection);
        db.closeConnection(connection);
        return data;
    }

    /**
     * 执行sql，返回结果(表格聚合查询)
     *
     * @param query 查询参数
     * @param model 数据源连接信息
     * @return 查询结果
     */
    public static DataServiceResult getDataService(ChartQueryObject query, DataSourceConVO model) {
        DataServiceResult res = new DataServiceResult();
        //创建sql语句
        IBuildSqlCommand command = DbHelperFactory.getSqlBuilder(model.conType);
        AbstractDbHelper db = DbHelperFactory.getDbHelper(model.conType);
        //数据源连接
        Connection connection = db.connection(model.conStr, model.conAccount, model.conPassword);
        //执行sql
        res.data = db.execQueryResultMaps(command.buildQueryData(query, false), connection);
        //table查询需要统计列
        if(query.interactiveType == InteractiveTypeEnum.TABLE) {
            res.aggregation = db.execQueryResultMap(command.buildQueryData(query, true), connection);
        }
        //关闭连接
        db.closeConnection(connection);
        return res;
    }

    /**
     * 执行sql，返回结果
     *
     * @param sql   执行的sql语句
     * @param model 数据源连接信息
     * @return 查询结果
     */
    public static Map<String, Object> execQueryResultMap(String sql, DataSourceConVO model) {
        AbstractDbHelper db = DbHelperFactory.getDbHelper(model.conType);
        Connection connection = db.connection(model.conStr, model.conAccount, model.conPassword);
        Map<String, Object> data = db.execQueryResultMap(sql, connection);
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
