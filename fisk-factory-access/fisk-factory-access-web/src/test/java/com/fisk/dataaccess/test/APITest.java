package com.fisk.dataaccess.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.dataaccess.controller.Home;
import com.fisk.dataaccess.dto.json.JsonTableData;
import com.fisk.dataaccess.enums.DriverTypeEnum;
import com.microsoft.sqlserver.jdbc.SQLServerBulkCopy;
import com.microsoft.sqlserver.jdbc.SQLServerBulkCopyOptions;
import com.sun.rowset.CachedRowSetImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author gy
 * @version 1.0
 * @description TODO
 * @date 2022/1/20 13:46
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class APITest {

    private String jsonStr = "{\n" +
            "\t\"data\": [{\n" +
            "\t\t\"id\": 1,\n" +
            "\t\t\"name\": \"张三\",\n" +
            "\t\t\"age\": 16,\n" +
            "\t\t\"createTime\": \"2022-1-22 12:00:00\",\n" +
            "\t\t\"role\": [{\n" +
            "\t\t\t\"userId\": 1,\n" +
            "\t\t\t\"roleId\": 1,\n" +
            "\t\t\t\"roleName\": \"role1\",\n" +
            "\t\t\t\"menus\": [{\n" +
            "\t\t\t\t\"roleId\": 1,\n" +
            "\t\t\t\t\"menuName\": \"menu1\",\n" +
            "\t\t\t\t\"menuSrc\": \"/menu1\"\n" +
            "\t\t\t}, {\n" +
            "\t\t\t\t\"roleId\": 1,\n" +
            "\t\t\t\t\"menuName\": \"menu2\",\n" +
            "\t\t\t\t\"menuSrc\": \"/menu2\"\n" +
            "\t\t\t}, {\n" +
            "\t\t\t\t\"roleId\": 1,\n" +
            "\t\t\t\t\"menuName\": \"menu3\",\n" +
            "\t\t\t\t\"menuSrc\": \"/menu3\"\n" +
            "\t\t\t}]\n" +
            "\t\t}, {\n" +
            "\t\t\t\"userId\": 1,\n" +
            "\t\t\t\"roleId\": 2,\n" +
            "\t\t\t\"roleName\": \"role2\",\n" +
            "\t\t\t\"menus\": [{\n" +
            "\t\t\t\t\"roleId\": 2,\n" +
            "\t\t\t\t\"menuName\": \"menu21\",\n" +
            "\t\t\t\t\"menuSrc\": \"/menu21\"\n" +
            "\t\t\t}, {\n" +
            "\t\t\t\t\"roleId\": 2,\n" +
            "\t\t\t\t\"menuName\": \"menu22\",\n" +
            "\t\t\t\t\"menuSrc\": \"/menu22\"\n" +
            "\t\t\t}, {\n" +
            "\t\t\t\t\"roleId\": 2,\n" +
            "\t\t\t\t\"menuName\": \"menu23\",\n" +
            "\t\t\t\t\"menuSrc\": \"/menu23\"\n" +
            "\t\t\t}]\n" +
            "\t\t}, {\n" +
            "\t\t\t\"userId\": 1,\n" +
            "\t\t\t\"roleId\": 3,\n" +
            "\t\t\t\"roleName\": \"role3\"\n" +
            "\t\t}]\n" +
            "\t}, {\n" +
            "\t\t\"id\": 2,\n" +
            "\t\t\"name\": \"李四\",\n" +
            "\t\t\"age\": 17,\n" +
            "\t\t\"createTime\": \"2022-1-22 12:30:00\",\n" +
            "\t\t\"role\": [{\n" +
            "\t\t\t\"userId\": 2,\n" +
            "\t\t\t\"roleId\": 4,\n" +
            "\t\t\t\"roleName\": \"role4\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"userId\": 2,\n" +
            "\t\t\t\"roleId\": 5,\n" +
            "\t\t\t\"roleName\": \"role5\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"userId\": 2,\n" +
            "\t\t\t\"roleId\": 6,\n" +
            "\t\t\t\"roleName\": \"role6\"\n" +
            "\t\t}]\n" +
            "\t}]\n" +
            "}";

    private String jsonStr2 = "{\"data\":[{\"role\":[{\"roleId\":1,\"roleName\":\"role1\",\"menus\":[{\"menuSrc\":\"menu1\",\"roleId\":1,\"menuName\":\"menu1\"},{\"menuSrc\":\"menu2\",\"roleId\":1,\"menuName\":\"menu2\"},{\"menuSrc\":\"menu3\",\"roleId\":1,\"menuName\":\"menu3\"}],\"userId\":1},{\"roleId\":2,\"roleName\":\"role2\",\"menus\":[{\"menuSrc\":\"menu21\",\"roleId\":2,\"menuName\":\"menu21\"},{\"menuSrc\":\"menu22\",\"roleId\":2,\"menuName\":\"menu22\"},{\"menuSrc\":\"menu23\",\"roleId\":2,\"menuName\":\"menu23\"}],\"userId\":1},{\"roleId\":3,\"roleName\":\"role3\",\"userId\":1}],\"createTime\":\"2022-1-22 12:00:00\",\"name\":\"张三\",\"id\":1,\"age\":16},{\"role\":[{\"roleId\":4,\"roleName\":\"role4\",\"userId\":2},{\"roleId\":5,\"roleName\":\"role5\",\"userId\":2},{\"roleId\":6,\"roleName\":\"role6\",\"userId\":2}],\"createTime\":\"2022-1-22 12:30:00\",\"name\":\"李四\",\"id\":2,\"age\":17}]}\n";

    private String jsonStr3 = "{\n" +
            "  \"data\": [\n" +
            "    {\n" +
            "      \"menus\":[\"str1\",\"str2\"],\n" +
            "      \"role\": [\n" +
            "        {\n" +
            "          \"roleId\": 1,\n" +
            "          \"roleName\": \"role1\",\n" +
            "          \"userId\": 1\n" +
            "        }\n" +
            "      ],\n" +
            "      \"createTime\": \"2022-1-22 12:00:00\",\n" +
            "      \"name\": \"张三\",\n" +
            "      \"id\": 1,\n" +
            "      \"age\": 16\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    @Resource
    private Home home;

    @Test
    public void TestAPI() throws Exception {
        JSONObject json = JSON.parseObject(jsonStr2);
//        System.out.println("json: \n" + json);
        List<JsonTableData> res = home.get(json);
//        res.forEach(System.out::println);
    }

    @Test
    public void sp() {
        Calendar c = Calendar.getInstance();
        String str = "yyyy/MM/dd";
        SimpleDateFormat format = new SimpleDateFormat(str);
        java.util.Date date = null;
        try {
            date = format.parse("2022/10/01");
            c.setTime(date);
        } catch (Exception ex) {
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int dates = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        int week = c.get(Calendar.DAY_OF_WEEK);
        System.out.printf("%d年%02d月%02d日 %02d:%02d:%02d %tA", year, month, dates, hour, minute, second, c);
    }

    /**
     * parseDate
     *
     * @param strDate
     * @param pattern
     * @return
     */
    public static Date parseDate(String strDate, String pattern) {
        Date date = null;
        try {
            if (pattern == null) {
                pattern = "YYYYMMDD";
            }
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            date = (Date) format.parse(strDate);
        } catch (Exception e) {
        }
        return date;
    }

    @Test
    public void Test() throws Exception {
        Connection con = getConn();
        Statement statement = con.createStatement();
        //这里必须设置为false，我们手动批量提交
        con.setAutoCommit(false);
        //这里需要注意，SQL语句的格式必须是预处理的这种，就是values(?,?,...,?)，否则批处理不起作用
        //PreparedStatement statement = con.prepareStatement("insert into student(id,`name`,age) values(?,?,?)");
        JSONObject json = JSON.parseObject(jsonStr);
        List<JsonTableData> res = home.get(json);

        int a = 1 / 0;
        try {
            for (JsonTableData re : res) {
                String tableName = re.table;
                JSONArray data = re.data;
                for (Object datum : data) {
                    String insertSqlIndex = "insert into ";
                    String insertSqlLast = "(";
                    String inserSql = "";
                    insertSqlIndex = insertSqlIndex + tableName + "(";
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
                    statement.addBatch(inserSql);
                }
                statement.executeBatch();
            }
            // 提交要执行的批处理，防止 JDBC 执行事务处理
            con.commit();
            statement.close();
            // 关闭相关连接
            con.close();
        } catch (SQLException e) {
            System.out.println("执行SQL异常失效: " + e.getMessage());
            statement.close();
            con.close();
        }
    }

    public Connection getConn() {
        Connection conn = null;
        try {
            Class.forName(DriverTypeEnum.PGSQL.getName());
            conn = DriverManager.getConnection("jdbc:postgresql://192.168.1.250:5432/dmp_ods?stringtype=unspecified", "postgres", "Password01!");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("驱动连接异常关闭: " + e.getMessage());
        }
        return conn;
    }

    /**
     * 获取空的CachedRowSet对象
     * 其中BaseDao为最常见的JDBC操作,这里就不贴出,相信大家看的懂
     *
     * @throws SQLException
     */
    public CachedRowSetImpl getCachedRowSet(Connection con, String tableName) throws SQLException {
        //查询出空值用于构建CachedRowSetImpl对象以省去列映射的步骤
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery("select * from " + tableName + " where 1=0");
        CachedRowSetImpl crs = new CachedRowSetImpl();
        crs.populate(rs);
        //获取crs以后关闭数据库连接
        statement.close();
        return crs;
    }

    /**
     * 使用BulkCopy和RowSet进行批量插入
     *
     * @param crs
     * @param batchSize
     */
    public void insertBatch(CachedRowSetImpl crs, int batchSize) throws SQLException {
        //数据库连接字符串
        String url = "jdbc:sqlserver://localhost:1433;DatabaseName=datebaseName"
                + ";user=userName;password=userPassword";
        SQLServerBulkCopyOptions copyOptions = new SQLServerBulkCopyOptions();
        copyOptions.setKeepIdentity(true);
        copyOptions.setBatchSize(8000);
        copyOptions.setUseInternalTransaction(true);
        SQLServerBulkCopy bulkCopy =
                new SQLServerBulkCopy(url);
        bulkCopy.setBulkCopyOptions(copyOptions);
        bulkCopy.setDestinationTableName("tableName");
        bulkCopy.writeToServer(crs);
        crs.close();
        bulkCopy.close();
    }


}
