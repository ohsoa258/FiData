package com.fisk.dataservice.utils.mysql;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fisk.dataservice.entity.ApiConfigureFieldPO;
import com.fisk.dataservice.mapper.ApiConfigureMapper;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.fisk.dataservice.enums.ConfigureFieldTypeEnum.*;
import static java.util.stream.Collectors.joining;

/**
 * @author Lock
 */
public class CreateMysqlUtil {

    @Resource
    private ApiConfigureMapper configureMapper;

    /**
     * 数据分组拼接
     *
     * @param apiConfigureFieldList 查询字段
     * @param tableName             字段来源表
     * @param currentPage           当前页
     * @param pageSize              每页显示条数
     * @return 查询字段分类拼接
     */
    @DS("datamodel")
    public List<Map> filterData(List<ApiConfigureFieldPO> apiConfigureFieldList, String tableName, Integer currentPage, Integer pageSize) {
        String queryFieldList = apiConfigureFieldList.stream()
                // 查询字段: select列
                // 日期.年,日期.月,维度.金额
                // `date.year`,`date.month`,`dimension.money`
                .filter(e -> e.getFieldType().equals(QUERY))
                .map(e -> "`" + e.getField() + "`")
                .collect(joining(","));

        String groupingList = apiConfigureFieldList.stream()
                // 分组字段: group by
                // 根据年,月,产维度金额分组: `date.year`,`date.month`,`dimension.money`
                .filter(e -> e.getFieldType().equals(GROUPING))
                .map(e -> "`" + e.getField() + "`")
                .collect(joining(","));

        String aggregationList = apiConfigureFieldList.stream()
                // 聚合字段: avg()  sum()  max()  min()  count()
                // 如维度金额求和:  sum(`money`)
                .filter(e -> e.getFieldType().equals(AGGREGATION))
                .map(e -> e.getFieldConditionValue() + "(" + "`" + e.getField() + "`" + ")")
                .collect(joining(","));

        String conditionList = apiConfigureFieldList.stream()
                // 权限控制字段: where条件
                // `date.year`=`2021` and `date.month`=`3`
                .filter(e -> e.getFieldType().equals(RESTRICT))
                .map(e -> "`" + e.getField() + "`" + e.getFieldConditionValue() + "'" + e.getFieldValue() + "'")
                .collect(joining("AND "));

        //
        return this.splicingSql(queryFieldList, aggregationList, groupingList, conditionList, currentPage, pageSize, tableName);
    }

    /**
     * 拼接sql
     *
     * @param aggregationList 聚合
     * @param groupingList    分组
     * @param conditionList   条件
     * @param currentPage     分页
     * @param pageSize
     * @param tableName       表名
     * @return
     */
    public List<Map> splicingSql(String queryFieldList,
                                 String aggregationList,
                                 String groupingList,
                                 String conditionList,
                                 Integer currentPage, Integer pageSize,
                                 String tableName) {
        // 最终SQL
        String splitSql = this.splitSql(queryFieldList, aggregationList, groupingList, conditionList, tableName);

        // 添加分页
        if (currentPage == null) {
            currentPage = 1;
        }
        if (pageSize == null) {
            pageSize = 50;
        }
        List<Map> objects = configureMapper.queryData(splitSql, (currentPage - 1) * pageSize, pageSize);
        return objects;
    }

    /**
     * 拼接sql主方法
     *
     * @param queryFieldList  查询
     * @param aggregationList 聚合
     * @param groupingList    分组
     * @param conditionList   条件
     * @param tableName       表名
     * @return
     */
    @DS("datamodel")
    public String splitSql(String queryFieldList, String aggregationList, String groupingList, String conditionList, String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        // select
        // SELECT `date.year`,`date.month`,`dimension.money`
        this.queryField(str, queryFieldList, aggregationList, groupingList);

        // from date


        str.append(" FROM ").append("`" + tableName + "`").append(" ");

        // TODO: 缺join
        str.append("join ").append("`" + tableName + "`").append(" ");

        // TODO: 缺on
        str.append("on ").append("`" + tableName + "`").append(" ");



        // where `date.year`=`2021` and `date.month`=`3`
        if (StringUtils.isNotBlank(conditionList)) {
//            str.append("WHERE 1 = 1 AND " + conditionList);
            str.append("WHERE " + conditionList+" ");
        }

//         order by
        if (StringUtils.isNotBlank(queryFieldList)) {
            str.append(" ORDER BY " + queryFieldList.substring(0, queryFieldList.indexOf(",")) + " DESC ");
        }

        // group
        if (StringUtils.isNotBlank(groupingList)) {
            str.append("GROUP BY ");
            // group by data.year
            str.append(groupingList);
        }

        // groupBy
        this.aggregation(str, queryFieldList, aggregationList, groupingList);
        this.noAggregation(str, queryFieldList, aggregationList, groupingList);
        return str.toString();
    }

    /**
     * select
     *
     * @param str             字符串拼接
     * @param queryFieldList  查询
     * @param aggregationList 聚合
     * @param groupingList    分组
     * @return
     */
    public void queryField(StringBuilder str, String queryFieldList, String aggregationList, String groupingList) {
        if (StringUtils.isNotBlank(queryFieldList)) {
            str.append(queryFieldList);
        }
        if (StringUtils.isNotBlank(queryFieldList) && StringUtils.isNotBlank(groupingList)) {
            str.append(",");
        }
        if (StringUtils.isNotBlank(groupingList)) {
            str.append(groupingList);
        }
        if (StringUtils.isNotBlank(groupingList) && StringUtils.isNotBlank(aggregationList)) {
            str.append(",");
        }
        if (StringUtils.isNotBlank(aggregationList)) {
            str.append(aggregationList);
        }
    }

    /**
     * 如果查询条件中有聚合，那么查询的字段必须分组
     *
     * @param str             字符串拼接
     * @param queryFieldList  查询
     * @param aggregationList 聚合
     * @param groupingList    分组
     * @return
     */
    public void aggregation(StringBuilder str, String queryFieldList, String aggregationList, String groupingList) {
        if (StringUtils.isNotBlank(aggregationList)) {
            if (StringUtils.isNotBlank(groupingList) && StringUtils.isNotBlank(queryFieldList)) {
                str.append(",");
            }

            if (StringUtils.isBlank(groupingList)) {
                str.append("GROUP BY ");
            }

            if (StringUtils.isNotBlank(queryFieldList)) {
                str.append(queryFieldList);
            }
        }
    }

    /**
     * 如果没有聚合字段,但是有分组的字段，那么查询的字段必须分组。
     *
     * @param str             字符串拼接
     * @param queryFieldList  查询
     * @param aggregationList 聚合
     * @param groupingList    分组
     * @return
     */
    public void noAggregation(StringBuilder str, String queryFieldList, String aggregationList, String groupingList) {
        if (StringUtils.isBlank(aggregationList) && StringUtils.isNotBlank(groupingList)) {
            if (StringUtils.isNotBlank(groupingList) && StringUtils.isNotBlank(queryFieldList)) {
                str.append(",");
            }

            if (StringUtils.isBlank(groupingList)) {
                str.append("GROUP BY ");
            }

            if (StringUtils.isNotBlank(queryFieldList)) {
                str.append(queryFieldList);
            }
        }
    }

}
