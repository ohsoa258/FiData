package com.fisk.dataservice.utils.mysql;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.DataDoFieldDTO;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * @author WangYan
 * @date 2021/9/3 16:01
 * mysql连接器
 */
@Slf4j
public class MysqlConnect {

    /**
     *  1.反射加载Driver
     */
    public static final String DRIVER = "com.mysql.jdbc.Driver";

    /**
     * 2.创建连接
     */
    public static final String URL = "jdbc:mysql://192.168.11.130:3306/dmp_dataservice_db"+"?useUnicode=true&characterEncoding=utf-8&useSSL=false";

    /**
     * 3.用户名
     */
    public static final String USER = "root";

    /**
     * 4.密码
     */
    public static final String PASSWORD = "root123";


    /**
     * 存在非维度
     * @param sql
     * @param apiConfigureFieldList
     * @param aggregation
     * @return
     */
    public static ResultEntity<Object> executeSql(String sql,
                                                  List<DataDoFieldDTO> apiConfigureFieldList,
                                                  String aggregation){
        ResultSet rs;
        StringBuffer str = new StringBuffer();
        String collect;
        try {
            Class.forName(DRIVER);
            String url = URL;
            String user = USER;
            String password = PASSWORD;

            Connection conn = DriverManager.getConnection(url, user, password);
            // 3.1创建执行器
            Statement statement = conn.createStatement();
            // 3.2执行器执行sql语句 获得ResultSet结果集
            rs = statement.executeQuery(sql);
            // 4.解析结果集
            str.append("[");
            while (rs.next()){
                str.append("{");
                collect = apiConfigureFieldList.stream()
                        .filter(e -> e.getFieldType() == DataDoFieldTypeEnum.COLUMN)
                        .map(e -> {
                            try {
                                return "\""+e.getFieldName() + "\"" + ":" + rs.getString(e.getFieldName());
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                            return null;
                        }).collect(joining(","));

                str.append(collect);
                addToAggregation(aggregation,collect,str,rs);
                // 不是最后一条数据库数据
                str.append(",");
            }
            str.append("]");
            str.deleteCharAt(str.length() -2);

            rs.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            log.error("【connection】数据库连接获取失败, ex", e);
            return ResultEntityBuild.build(ResultEnum.ERROR, e);
        }catch (ClassNotFoundException e){
            log.error("MYSQL数据库驱动加载失败, ex", e);
            return ResultEntityBuild.build(ResultEnum.ERROR, e);
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS,str);
    }


    /**
     * 追加聚合的字段
     * @param aggregation
     * @param str
     */
    public static void addToAggregation(String aggregation,String collect,StringBuffer str,ResultSet rs){
        if (StringUtils.isEmpty(aggregation)){
            return;
        }

        if (StringUtils.isBlank(aggregation)){
            str.append("}");
        }

        if (StringUtils.isNotBlank(collect) && StringUtils.isNotBlank(aggregation)){
            str.append(",");
        }

        for (String s : aggregation.split(",")) {
            try {
                str.append("\"" + s + "\"" + ":" + rs.getString(s)+"}");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    /**
     * 不存在非维度的doris库
     * @param sql
     * @param apiConfigureFieldList
     * @return
     */
    public static ResultEntity<Object> executeSql(String sql,
                                                  List<DataDoFieldDTO> apiConfigureFieldList){
        ResultSet rs;
        StringBuffer str = new StringBuffer();
        String collect;
        try {
            Class.forName(DRIVER);
            String url = URL;
            String user = USER;
            String password = PASSWORD;

            Connection conn = DriverManager.getConnection(url, user, password);
            // 3.1创建执行器
            Statement statement = conn.createStatement();
            // 3.2执行器执行sql语句 获得ResultSet结果集
            rs = statement.executeQuery(sql);
            // 4.解析结果集
            str.append("[");
            while (rs.next()){
                str.append("{");
                collect = apiConfigureFieldList.stream()
                        .filter(e -> e.getFieldType() == DataDoFieldTypeEnum.COLUMN)
                        .map(e -> {
                            try {
                                return "\""+e.getFieldName() + "\"" + ":" + rs.getString(e.getFieldName());
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                            return null;
                        }).collect(joining(","));

                str.append(collect);
                str.append("}");
                str.append(",");
            }
            str.append("]");
            str.deleteCharAt(str.length() -2);

            rs.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            log.error("【connection】数据库连接获取失败, ex", e);
            return ResultEntityBuild.build(ResultEnum.ERROR, e);
        } catch (ClassNotFoundException e){
            log.error("MYSQL数据库驱动加载失败, ex", e);
            return ResultEntityBuild.build(ResultEnum.ERROR, e);
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS,str);
    }
}
