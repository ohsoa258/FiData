package com.fisk.dataservice.utils.mysql;

import com.alibaba.fastjson.JSON;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.DataDoFieldDTO;
import com.fisk.dataservice.dto.TableDataDTO;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.List;

import static com.fisk.dataservice.doris.DorisDataSource.*;
import static java.util.stream.Collectors.joining;

/**
 * @author WangYan
 * @date 2021/9/3 16:01
 * mysql连接器
 */
@Slf4j
public class MysqlConnect {

    /**
     * 存在非维度  todo 代码可优化
     * @param sql
     * @param apiConfigureFieldList
     * @param aggregation
     * @return
     */
    public static ResultEntity<Object> executeSql(String sql,
                                                  List<DataDoFieldDTO> apiConfigureFieldList,
                                                  String aggregation,
                                                  List<TableDataDTO> noTableData){
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
                addToAggregation(aggregation,collect,str,rs,noTableData);
                // 不是最后一条数据库数据
                str.append(",");
            }
            str.append("]");
            str.deleteCharAt(str.length() -2);

            rs.close();
            statement.close();
            conn.close();
        } catch (Exception e){
            log.error("执行SQL失败:", e);
            return ResultEntityBuild.build(ResultEnum.SQL_ERROR);
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, JSON.parse(str.toString()));
    }


    /**
     * 追加聚合的字段
     * @param aggregation
     * @param str
     */
    public static void addToAggregation(String aggregation,String collect,StringBuffer str,ResultSet rs,List<TableDataDTO> noTableData){
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
                for (TableDataDTO datum : noTableData) {
                    str.append("\"" + s.replace(datum.getAlias()+".", "") + "\"" + ":" + rs.getString(s)+"}");
                }
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
        } catch (Exception e){
            log.error("执行SQL失败:", e);
            return ResultEntityBuild.build(ResultEnum.SQL_ERROR);
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, JSON.parse(str.toString()));
    }
}
