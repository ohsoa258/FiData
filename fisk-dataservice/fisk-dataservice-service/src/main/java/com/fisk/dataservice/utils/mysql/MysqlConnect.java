package com.fisk.dataservice.utils.mysql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.DataDoFieldDTO;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

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
                                return "\""+e.getFieldName() + "\"" + ":" + "\"" + rs.getString(e.getFieldName()) + "\"";
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                            return null;
                        }).collect(joining(","));

                str.append(collect);
                addToAggregation(aggregation,collect,str,rs,apiConfigureFieldList);
                // 不是最后一条数据库数据
                str.append(",");
            }
            str.append("]");
            str.deleteCharAt(str.length() -2);

            rs.close();
            statement.close();
            conn.close();
        } catch (SQLException e){
            log.error("执行SQL失败:", e);
            return ResultEntityBuild.build(ResultEnum.SQL_ERROR);
        } catch (Exception e){
            log.error("数据解析失败:", e);
            return ResultEntityBuild.build(ResultEnum.SQL_ANALYSIS);
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, JSON.parse(str.toString(), Feature.OrderedField));
    }


    /**
     * 追加聚合的字段
     * @param aggregation
     * @param collect
     * @param str
     * @param rs
     * @param apiConfigureFieldList
     */
    public static void addToAggregation(String aggregation,String collect,StringBuffer str,ResultSet rs,
                                        List<DataDoFieldDTO> apiConfigureFieldList){
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
                List<DataDoFieldDTO> aggregationList = apiConfigureFieldList.stream()
                        .filter(e -> e.getFieldType() == DataDoFieldTypeEnum.VALUE || e.getFieldType() == DataDoFieldTypeEnum.SLICER)
                        .collect(Collectors.toList());

                for (DataDoFieldDTO dto : aggregationList) {
                    str.append("\"" + dto.getFieldName() + "\"" + ":" + "\"" + rs.getString(s)+ "\"" + ",");
                }
                str.deleteCharAt(str.length() -1);
                str.append("}");
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
                                return "\""+e.getFieldName() + "\"" + ":" + "\"" + rs.getString(e.getFieldName()) + "\"";
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
        } catch (SQLException e){
            log.error("执行SQL失败:", e);
            return ResultEntityBuild.build(ResultEnum.SQL_ERROR);
        } catch (Exception e){
            log.error("数据解析失败:", e);
            return ResultEntityBuild.build(ResultEnum.SQL_ANALYSIS);
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, JSON.parse(str.toString(), Feature.OrderedField));
    }
}
