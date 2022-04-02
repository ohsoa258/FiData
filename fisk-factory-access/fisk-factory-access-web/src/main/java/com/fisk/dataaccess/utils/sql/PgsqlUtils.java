package com.fisk.dataaccess.utils.sql;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.json.JsonTableData;
import com.fisk.dataaccess.enums.DriverTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/1/21 17:13
 */
@Slf4j
@Component
public class PgsqlUtils {

    private static String pgUrl;
    private static String pgUsername;
    private static String pgPpassword;

    @Value("${pgsql-ods.url}")
    public void setPgUrl(String pgUrl) {
        PgsqlUtils.pgUrl = pgUrl;
    }

    @Value("${pgsql-ods.username}")
    public void setPgUsername(String pgUsername) {
        PgsqlUtils.pgUsername = pgUsername;
    }

    @Value("${pgsql-ods.password}")
    public void setPgPpassword(String pgPpassword) {
        PgsqlUtils.pgPpassword = pgPpassword;
    }

    /**
     * 创建pgsql连接驱动
     *
     * @return java.sql.Connection
     * @description
     * @author Lock
     * @date 2022/1/21 17:18
     * @version v1.0
     * @params
     */
    public static Connection getPgConn() {
        Connection conn = null;
        try {
            Class.forName(DriverTypeEnum.PGSQL.getName());
            conn = DriverManager.getConnection(pgUrl, pgUsername, pgPpassword);
        } catch (ClassNotFoundException | SQLException e) {
            log.error("【getPgConn】创建pgsql连接驱动失败, ex", e);
            throw new FkException(ResultEnum.CREATE_PG_CONNECTION);
        }
        return conn;
    }

    /**
     * 将数据从stg同步到ods
     *
     * @return void
     * @description 将数据从stg同步到ods
     * @author Lock
     * @date 2022/2/25 15:39
     * @version v1.0
     * @params sqlList sql集合
     * @params flag 0: 推送数据前清空stg; 1: 推送完数据,开始同步stg->ods
     */
    public void stgToOds(List<String> sqlList, int flag) throws SQLException {
        Connection pgConn = getPgConn();
        Statement statement = pgConn.createStatement();
        try {
            // 执行sql
            statement.executeUpdate(sqlList.get(flag));

            statement.close();
            pgConn.close();
        } catch (SQLException e) {
            log.error("批量执行SQL异常: {}", e.getMessage());
            statement.close();
            pgConn.close();
            throw new FkException(ResultEnum.STG_TO_ODS_ERROR);
        }
    }

    /**
     * 批量执行pgsql
     *
     * @return void
     * @description 批量执行pgsql
     * @author Lock
     * @date 2022/1/21 17:24
     * @version v1.0
     * @params jsonStr json字符串
     * @params tablePrefixName pg中的物理表前缀名
     */
    public void executeBatchPgsql(String tablePrefixName, List<JsonTableData> res) throws Exception {
        Connection con = getPgConn();
        Statement statement = con.createStatement();
        //这里必须设置为false，我们手动批量提交
        con.setAutoCommit(false);
        //这里需要注意，SQL语句的格式必须是预处理的这种，就是values(?,?,...,?)，否则批处理不起作用
        //PreparedStatement statement = con.prepareStatement("insert into student(id,`name`,age) values(?,?,?)");

        /*JSONObject json = JSON.parseObject(jsonStr);
        // TODO 调用JsonUtils获取表对象集合
        List<JsonTableData> res = home.get(json);*/
        int countSql = 0;
        try {
            for (JsonTableData re : res) {
                String tableName = re.table;
                JSONArray data = re.data;
                for (Object datum : data) {
                    String insertSqlIndex = "insert into ";
                    String insertSqlLast = "(";
                    String inserSql = "";
                    insertSqlIndex = insertSqlIndex + tablePrefixName + tableName + "(";
                    JSONObject object = (JSONObject) datum;
                    Iterator<Map.Entry<String, Object>> iter = object.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = iter.next();
                        insertSqlIndex = insertSqlIndex + entry.getKey() + ",";
                        insertSqlLast = insertSqlLast + "'" + entry.getValue() + "'" + ",";
                    }
                    insertSqlIndex = insertSqlIndex.substring(0, insertSqlIndex.lastIndexOf(",")) + ") values";
                    insertSqlLast = insertSqlLast.substring(0, insertSqlLast.lastIndexOf(",")) + ")";
                    inserSql = insertSqlIndex + insertSqlLast;
                    System.out.println(inserSql);
                    countSql++;
                    statement.addBatch(inserSql);
                }
                statement.executeBatch();
            }
            System.out.println("本次添加的sql个数为: " + countSql);
            // 提交要执行的批处理，防止 JDBC 执行事务处理
            con.commit();
            statement.close();
            // 关闭相关连接
            con.close();
        } catch (SQLException e) {
            log.error("批量执行SQL异常: {}", e.getMessage());
            statement.close();
            con.close();
            throw new FkException(ResultEnum.PUSH_DATA_ERROR);
        }
    }

}
