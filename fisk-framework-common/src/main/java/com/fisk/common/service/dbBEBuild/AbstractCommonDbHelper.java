package com.fisk.common.service.dbBEBuild;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.BeanHelper;
import com.fisk.common.core.utils.Dto.Excel.SheetDataDto;
import com.fisk.common.framework.exception.FkException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * @author WangYan
 * @date 2022/4/13 15:57
 */
@Slf4j
public class AbstractCommonDbHelper {

    /**
     * 关闭连接
     *
     * @param connection 连接对象
     */
    public static void closeConnection(Connection connection) {
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
     * 回滚事务
     *
     * @param connection 连接对象
     */
    public static void rollbackConnection(Connection connection) {
        if (connection != null) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ec) {
                log.error("【closeConnection】数据库连接关闭失败, ex", ec);
            }
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

    /**
     * 关闭ResultSet对象
     *
     * @param rs
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            rs = null;
        }
    }

    /**
     * 新增数据，返回主键id
     *
     * @param sql
     * @param connection
     * @return
     */
    public static Integer executeSqlReturnKey(String sql, Connection connection) {
        Statement statement = null;
        String code = UUID.randomUUID().toString();
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            log.info("【executeSqlReturnKey】【" + code + "】执行sql: 【" + sql + "】");
            statement = connection.createStatement();
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = statement.getGeneratedKeys();
            int keyValue = -1;
            if (rs.next()) {
                keyValue = rs.getInt(1);
            }
            return keyValue;
        } catch (SQLException ex) {
            log.error("【executeSqlReturnKey】【" + code + "】执行sql查询报错, ex", ex);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, ex.getLocalizedMessage());
        } finally {
            closeStatement(statement);
            stopWatch.stop();
            log.info("【executeSqlReturnKey】【" + code + "】执行时间: 【" + stopWatch.getTotalTimeMillis() + "毫秒】");
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
     * 执行查询
     *
     * @param sql  查询语句
     * @param conn 数据库连接
     * @return 查询结果Map
     */
    public static JSONArray execQueryResultArrays(String sql, Connection conn) {
        Statement st = null;
        JSONArray dataArray = new JSONArray();
        try {
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            assert st != null;
            ResultSet rs = st.executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                JSONObject jsonObj = new JSONObject();
                // 遍历每一列
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    //获取sql查询数据集合
                    Object value = rs.getObject(columnName);
                    jsonObj.put(columnName, value);
                }
                dataArray.add(jsonObj);
            }
            rs.close();
        } catch (Exception ex) {
            log.error("【execQueryResultArrays】执行SQL异常：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        } finally {
            closeStatement(st);
            closeConnection(conn);
        }
        return dataArray;
    }

    /**
     * 执行查询 直接返回组装后的SheetDataDto
     *
     * @param sql  查询语句
     * @param conn 数据库连接
     * @return 查询结果Map
     */
    public static SheetDataDto execQueryResultSheet(String sql, Connection conn) {
        SheetDataDto sheetDataDto = new SheetDataDto();
        List<String> columnList = new ArrayList<>();
        List<List<String>> mapList = new ArrayList<>();
        Statement st = null;
        try {
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            assert st != null;
            ResultSet rs = st.executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                columnList.add(metaData.getColumnLabel(columnIndex));
            }
            while (rs.next()) {
                List<String> objectMap = new ArrayList<>();
                // 遍历每一列
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    //获取sql查询数据集合
                    Object value = rs.getObject(columnName);
                    objectMap.add(value != null ? value.toString() : "");
                }
                mapList.add(objectMap);
            }
            rs.close();
            sheetDataDto.setColumns(columnList);
            sheetDataDto.setColumnData(mapList);
        } catch (Exception ex) {
            log.error("【execQueryResultArrays】执行SQL异常：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        } finally {
            closeStatement(st);
            closeConnection(conn);
        }
        return sheetDataDto;
    }

    /**
     * 执行sql
     *
     * @param sql     sql
     * @param con     连接器
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
            closeConnection(con);
            stopWatch.stop();
            log.info("【execQuery】【" + code + "】执行时间: 【" + stopWatch.getTotalTimeMillis() + "毫秒】");
        }
    }

    /**
     * 执行查询
     *
     * @param sql 查询语句
     * @param con 数据库连接
     * @return 查询结果Map
     */
    public static List<Map<String, Object>> batchExecQueryResultMaps(String sql, Connection con) {
        return batchQuery(sql, con, BeanHelper::resultSetToMaps);
    }

    /**
     * 执行sql
     *
     * @param sql     sql
     * @param con     连接器
     * @param func<T>
     * @return
     */
    private static <T> T batchQuery(String sql, Connection con, Function<ResultSet, T> func) {
        Statement st = null;
        String code = UUID.randomUUID().toString();
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            log.info("【execQuery】【" + code + "】执行sql: 【" + sql + "】");
            st = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet res = st.executeQuery(sql);
            return func.apply(res);
        } catch (SQLException ex) {
            log.error("【execQuery】【" + code + "】执行sql查询报错, ex", ex);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, ex.getLocalizedMessage());
        } finally {
            closeStatement(st);
            closeConnection(con);
            stopWatch.stop();
            log.info("【execQuery】【" + code + "】执行时间: 【" + stopWatch.getTotalTimeMillis() + "毫秒】");
        }
    }

    /**
     * 执行查询
     *
     * @param sql 查询语句
     * @param con 数据库连接
     * @return 查询结果Map
     */
    public static List<Map<String, Object>> batchExecQueryResultMaps_noClose(String sql, Connection con) {
        return batchQuery_noClose(sql, con, BeanHelper::resultSetToMaps);
    }

    /**
     * 执行sql
     *
     * @param sql     sql
     * @param con     连接器
     * @param func<T>
     * @return
     */
    private static <T> T batchQuery_noClose(String sql, Connection con, Function<ResultSet, T> func) {
        Statement st = null;
        String code = UUID.randomUUID().toString();
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            log.info("【execQuery】【" + code + "】执行sql: 【" + sql + "】");
            st = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet res = st.executeQuery(sql);
            return func.apply(res);
        } catch (SQLException ex) {
            log.error("【execQuery】【" + code + "】执行sql查询报错, ex", ex);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, ex.getLocalizedMessage());
        } finally {
            closeStatement(st);
//            closeConnection(con);
            stopWatch.stop();
            log.info("【execQuery】【" + code + "】执行时间: 【" + stopWatch.getTotalTimeMillis() + "毫秒】");
        }
    }

    /**
     * 执行查询
     *
     * @param sql 查询语句
     * @param con 数据库连接
     * @return 查询结果List
     */
    public static <T> List<T> execQueryResultList(String sql, Connection con, Class<T> tClass) {
        return query(sql, con, e -> BeanHelper.resultSetToList(e, tClass));
    }

    /**
     * 连接
     */
    public Connection connection(String connectionStr, String acc, String pwd, DataSourceTypeEnum type) {
        try {
            loadDriver(type);
            Connection connection = getConnectionByType(connectionStr, acc, pwd, type);
            log.info("【connection】数据库连接成功, 连接信息【" + connectionStr + "】");
            return connection;
        } catch (SQLException e) {
            log.error("【connection】数据库连接获取失败, ex", e);
            if (e.getErrorCode() == 1045) {
                //抛出密码不正确异常
                throw new FkException(ResultEnum.USER_ACCOUNTPASSWORD_ERROR, e.getLocalizedMessage());
            }
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
    private Connection getConnectionByType(String connectionStr, String acc, String pwd, DataSourceTypeEnum type) throws Exception {
        switch (type) {
            case SQLSERVER:
            case POSTGRESQL:
            case MYSQL:
            case DORIS:
            case ORACLE:
            case OPENEDGE:
            case DORIS_CATALOG:
            case DM8:
                return DriverManager.getConnection(connectionStr, acc, pwd);
            default:
                return null;
        }
    }

    /**
     * 执行sql
     *
     * @param sql
     * @param connection
     * @return
     */
    public void executeSql(String sql, Connection connection) throws SQLException {
        Statement statement = null;
        String code = UUID.randomUUID().toString();
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        log.info("【execCreate】【" + code + "】执行sql: 【" + sql + "】");
        statement = connection.createStatement();
        statement.execute(sql);
    }

    /**
     * 执行sql
     *
     * @param sql
     * @param connection
     * @return
     */
    public static void executeSql_Close(String sql, Connection connection) {
        Statement statement = null;
        StopWatch stopWatch = new StopWatch();
        String code = UUID.randomUUID().toString();
        try {
            stopWatch.start();
            log.info("【executeSql_Close】【" + code + "】执行sql: 【" + sql + "】");
            statement = connection.createStatement();
            statement.execute(sql);
        } catch (Exception ex) {
            log.error("【executeSql_Close】系统异常：" + ex);
        } finally {
            closeStatement(statement);
            closeConnection(connection);
            stopWatch.stop();
            log.info("【executeSql_Close】【" + code + "】执行时间: 【" + stopWatch.getTotalTimeMillis() + "毫秒】");
        }
    }

    /**
     * 事务批处理执行sql
     *
     * @param sql
     * @param connection
     * @return
     */
    public void executeSql(List<String> sql, Connection connection) {
        Statement statement = null;
        try {
            //设为手动提交
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            for (int i = 0; i < sql.size(); i++) {
                statement.addBatch(sql.get(i));
            }
            statement.executeBatch();
            connection.commit(); //提交事务
        } catch (SQLException e) {
            log.info("执行发生了异常，回滚撤销事务");
            rollbackConnection(connection);
            log.error(e.toString());
        } finally {
            AbstractCommonDbHelper.closeStatement(statement);
            AbstractCommonDbHelper.closeConnection(connection);
        }
    }

    /**
     * 获取count条数
     *
     * @param sql
     * @param connection
     * @return
     */
    public static String executeTotalSql(String sql, Connection connection, String labelName) {
        Statement st = null;
        ResultSet rs = null;
        String result = null;
        try {
            st = connection.createStatement();
            rs = st.executeQuery(sql);
            if (rs.next()) {
                result = rs.getString(labelName);
            }
        } catch (SQLException e) {
            log.error("executeTotalSql ex:{}", e);
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
            AbstractCommonDbHelper.closeStatement(st);
        }
        return result;
    }
}
