package com.fisk.dataaccess.utils.sql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.json.JsonTableData;
import com.fisk.dataaccess.dto.pgsqlmetadata.ApiSqlResultDTO;
import com.fisk.dataaccess.enums.DriverTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
    public ResultEnum stgToOds(List<String> sqlList, int flag) throws SQLException {
        Connection pgConn = getPgConn();
        Statement statement = pgConn.createStatement();
        try {
            // 执行sql
            System.out.println("操作类型AE86: 0: 推送数据前清空stg; 1: 推送完数据,开始同步stg->ods;  " + flag);
            System.out.println("pg推送数据的函数: " + sqlList.get(flag));
            statement.executeUpdate(sqlList.get(flag));

            statement.close();
            pgConn.close();
        } catch (SQLException e) {
            log.error("批量执行SQL异常: {}", e.getMessage());
            statement.close();
            pgConn.close();
            return ResultEnum.STG_TO_ODS_ERROR;
        }

        return ResultEnum.SUCCESS;
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
    public ResultEntity<Object> executeBatchPgsql(String tablePrefixName, List<JsonTableData> res) throws Exception {
        Connection con = getPgConn();
        Statement statement = con.createStatement();
        //这里必须设置为false，我们手动批量提交
        con.setAutoCommit(false);
        //这里需要注意，SQL语句的格式必须是预处理的这种，就是values(?,?,...,?)，否则批处理不起作用
        ////PreparedStatement statement = con.prepareStatement("insert into student(id,`name`,age) values(?,?,?)");

        // TODO 调用JsonUtils获取表对象集合
        int countSql = 0;
        List<ApiSqlResultDTO> list = new ArrayList<>();
        try {
            for (JsonTableData re : res) {

                ApiSqlResultDTO apiSqlResultDto = new ApiSqlResultDTO();

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
                    log.info("数据推送到stg的sql为: " + inserSql);
                    countSql++;
                    statement.addBatch(inserSql);
                }
                // 批量执行sql
                statement.executeBatch();

                // 保存本次信息
                apiSqlResultDto.setCount(countSql);
                // 多表插入时重新清空条数
                countSql = 0;
                // stg表名
                apiSqlResultDto.setTableName(tablePrefixName + tableName);
                apiSqlResultDto.setMsg("成功");

                list.add(apiSqlResultDto);
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
            // 执行sql异常,重置记录的条数
            countSql = 0;
            ApiSqlResultDTO apiSqlResultDto = new ApiSqlResultDTO();
            apiSqlResultDto.setMsg("失败");
            apiSqlResultDto.setCount(0);
            list.add(apiSqlResultDto);
            return ResultEntityBuild.build(ResultEnum.PUSH_DATA_SQL_ERROR, list);
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, JSON.toJSONString(list));
    }

}
