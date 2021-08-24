package com.fisk.dataservice.utils.mysql;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.client.DimensionClient;
import com.fisk.datamodel.dto.table.TableDataDTO;
import com.fisk.dataservice.dto.DataDoFieldDTO;
import com.fisk.dataservice.mapper.ApiConfigureMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import static com.fisk.dataservice.enums.DataDoFieldTypeEnum.COLUMN;
import static com.fisk.dataservice.enums.DataDoFieldTypeEnum.WHERE;
import static com.fisk.dataservice.enums.DataDoFieldTypeEnum.VALUE;
import static java.util.stream.Collectors.*;

/**
 * @author Lock
 */
@Component
public class CreateMysqlUtil {

    @Resource
    private ApiConfigureMapper configureMapper;

    @Resource
    private DimensionClient dimensionClient;

    /**
     * 数据分组拼接
     *
     * @param apiConfigureFieldList 查询字段
     * @param currentPage           当前页
     * @param pageSize              每页显示条数
     * @return 查询字段分类拼接
     */
    public List<Map> filterData(List<DataDoFieldDTO> apiConfigureFieldList, Integer currentPage, Integer pageSize) {
        // 转义符
        String[] escapeStr = getEscapeStr();

        // 所有列的表名field集合
        List<DataDoFieldDTO> fieldList = apiConfigureFieldList.stream()
                .filter(e -> e.getFieldType().equals(COLUMN))
                .collect(toList());


       ResultEntity<TableDataDTO> resultEntity = dimensionClient.getTableName(155, COLUMN, "year");

        // k fileds v 表名  (列)
        Map<Integer, String> map = apiConfigureFieldList.stream()
                .filter(e -> e.getFieldType().equals(COLUMN))
                .collect(toMap(DataDoFieldDTO::getFieldId,
                        e -> dimensionClient.getTableName(e.getFieldId(), COLUMN, e.getFieldName()).getData().tableName));

        String queryFieldList = apiConfigureFieldList.stream()
                // 查询字段: select列
                // 日期.年,日期.月,维度.金额
                // `date.year`,`date.month`,`dimension.money`
                .filter(e -> e.getFieldType().equals(COLUMN))
                .map(e -> escapeStr[0] + dimensionClient.getTableName(e.getFieldId(),e.getFieldType(),e.getFieldName()) + "." + e.getFieldName()
                        + escapeStr[1])
                .collect(joining(","));

        String conditionList = apiConfigureFieldList.stream()
                // 权限控制字段: where条件
                // `date.year`=`2021` and `date.month`=`3`
                .filter(e -> e.getFieldType().equals(WHERE))
                .map(e -> escapeStr[0] + dimensionClient.getTableName(e.getFieldId(), e.getFieldType(),e.getFieldName()).getData().tableName
                        + "."+ e.getFieldName() + escapeStr[1] + e.getWhere() + escapeStr[0] + e.getWhereValue() + escapeStr[1])
                .collect(joining("AND "));

        String aggregationList = apiConfigureFieldList.stream()
                // 聚合字段: avg()  sum()  max()  min()  count()
                // 如维度金额求和:  sum(`money`)
                .filter(e -> e.getFieldType().equals(VALUE))
                .map(e -> dimensionClient.getAggregation(e.getFieldId()).getData()
                        + "(" + escapeStr[0] + e.getFieldName() + escapeStr[1] + ")")
                .collect(joining(","));

        String groupingList = apiConfigureFieldList.stream()
                // 分组字段: group by
                // 根据年,月,产维度金额分组: `date.year`,`date.month`,`dimension.money`
                .filter(e -> e.getFieldType().equals(COLUMN))
                .map(e -> escapeStr[0] + dimensionClient.getTableName(e.getFieldId(),e.getFieldType(),e.getFieldName())
                        + e.getFieldName() + escapeStr[1])
                .collect(joining(","));
        return this.splicingSql(queryFieldList, aggregationList, groupingList, conditionList, currentPage, pageSize, map, fieldList);
    }

    /**
     * 拼接sql
     *
     * @param aggregationList 聚合
     * @param groupingList    分组
     * @param conditionList   条件
     * @param currentPage     分页
     * @param pageSize
     * @param       表名
     * @return
     */
    public List<Map> splicingSql(String queryFieldList,
                                 String aggregationList,
                                 String groupingList,
                                 String conditionList,
                                 Integer currentPage, Integer pageSize,
                                 Map map,
                                 List<DataDoFieldDTO> fieldList) {
        // 最终SQL
        String splitSql = this.splitSql(queryFieldList, aggregationList, groupingList, conditionList, map,fieldList);

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
     * @param map             表名
     * @return
     */
    public String splitSql(String queryFieldList, String aggregationList, String groupingList, String conditionList, Map map,
                           List<DataDoFieldDTO> fieldList) {
        // 转义符
        String[] escapeStr = getEscapeStr();

        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        // select
        // SELECT `date.year`,`date.month`,`dimension.money`
        this.queryField(str, queryFieldList, aggregationList, groupingList);

        // from date  主表
        str.append(" FROM ").append(escapeStr[0] + map.get(fieldList.get(0).fieldId) + escapeStr[1]).append(" ");

        // 从表
        for (DataDoFieldDTO dataDoField : fieldList) {
            // TODO: 缺join
            str.append("join ").append(map.get(dataDoField.getFieldId())).append(" ");

            // TODO: 缺on
            // A.nian=B.nian
            str.append("on ").append(map.get(fieldList.get(0).fieldId + "=" + map.get(dataDoField.getFieldId()) + "." +  dataDoField.fieldName)).append(" ");
        }

        // where `date.year`=`2021` and `date.month`=`3`
        if (StringUtils.isNotBlank(conditionList)) {
//            str.append("WHERE 1 = 1 AND " + conditionList);
            str.append("WHERE " + conditionList + " ");
        }

        // group
        if (StringUtils.isNotBlank(groupingList)) {
            str.append("GROUP BY ");
            // group by data.year
            str.append(groupingList);
        }
//        // groupBy
//        this.aggregation(str, queryFieldList, aggregationList, groupingList);
//        this.noAggregation(str, queryFieldList, aggregationList, groupingList);
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

    /**
     * 根据数据源类型获取转义字符
     *
     * @return 转义字符
     */
    protected static String[] getEscapeStr() {
        String[] arr = new String[2];
        arr[0] = "`";
        arr[1] = "`";
        return arr;
    }
}
