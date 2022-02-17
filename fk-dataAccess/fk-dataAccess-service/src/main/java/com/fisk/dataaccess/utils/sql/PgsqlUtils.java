package com.fisk.dataaccess.utils.sql;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.dataaccess.controller.Home;
import com.fisk.dataaccess.dto.json.JsonTableData;
import com.fisk.dataaccess.enums.DriverTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
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
public class PgsqlUtils {

    @Value("${pgsql-ods.url}")
    private String pgUrl;
    @Value("${pgsql-ods.username}")
    private String username;
    @Value("${pgsql-ods.password}")
    private String password;
    @Resource
    private Home home;

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
    public Connection getConn() {
        Connection conn = null;
        try {
            Class.forName(DriverTypeEnum.PGSQL.getName());
            conn = DriverManager.getConnection("jdbc:postgresql://192.168.1.250:5432/dmp_ods?stringtype=unspecified", "postgres", "Password01!");
//            conn = DriverManager.getConnection(pgUrl, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("驱动连接异常关闭: " + e.getMessage());
        }
        return conn;
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
     * @params tablePrefixName pg中的物理表名
     */
    public void executeBatchPgsql(String tablePrefixName, List<JsonTableData> res) throws Exception {
        Connection con = getConn();
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
//                    System.out.println(inserSql);
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
        }
    }

}
