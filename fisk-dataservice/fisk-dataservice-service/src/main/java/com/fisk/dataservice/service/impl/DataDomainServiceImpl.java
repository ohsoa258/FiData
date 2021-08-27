package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fisk.common.response.ResultEntity;
import com.fisk.dataservice.dto.TableDataDTO;
import com.fisk.dataservice.dto.DataDoFieldDTO;
import com.fisk.dataservice.mapper.ApiConfigureMapper;
import com.fisk.dataservice.service.DataDomainService;
import com.fisk.dataservice.service.ITableName;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.fisk.common.constants.AliasConstants.*;
import static com.fisk.dataservice.enums.DataDoFieldTypeEnum.*;
import static com.fisk.dataservice.enums.DataDoFieldTypeEnum.COLUMN;
import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.joining;

/**
 * @author WangYan
 * @date 2021/8/23 16:38
 */
@Service
public class DataDomainServiceImpl implements DataDomainService {

    @Resource
    private ITableName iTableName;

    @Resource
    private ApiConfigureMapper configureMapper;

    @Override
    public List<Map> query(List<DataDoFieldDTO> apiConfigureFieldList, Integer currentPage, Integer pageSize) {
        List<Map> maps = this.filterData(apiConfigureFieldList, currentPage, pageSize);
        return maps;
    }


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

        // 非维度数据
        List<TableDataDTO> noTableData = apiConfigureFieldList.stream()
                .filter(e -> e.getDimension() == 0)
                .map(e -> iTableName.getTableName(e.getFieldId(), e.getFieldType(), e.getFieldName(), e.dimension).getData())
                .collect(toList());

        // 所有维度的data
        List<TableDataDTO> existTableData = apiConfigureFieldList.stream()
                .filter(e -> e.getDimension() == 1)
                .map(e -> iTableName.getTableName(e.getFieldId(), e.getFieldType(), e.getFieldName(), e.dimension).getData())
                .collect(toList());
        // 全局sql
        StringBuilder wholeStr = new StringBuilder();
        // 维度
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");

        // select
        String queryField = existTableData.stream()
                .map(e -> e.getTableName() + "." + escapeStr[0] + e.getTableField() + escapeStr[1])
                .collect(joining(","));

        str.append(queryField);

        // 主表
        str.append(" FROM " + existTableData.get(0).tableName);
        existTableData.remove(0);

        str.append(" INNER JOIN ");

        // 从表
        String collect = existTableData.stream()
                .map(e -> e.tableName + " ON 1=1")
                .collect(joining(" INNER JOIN "));
        str.append(collect);
        // 只有维度的的情况
        System.out.println(str);

        // 存在非维度
        if (CollectionUtils.isNotEmpty(noTableData)) {
            // 所有维度的data
            List<TableDataDTO> tableDataList = apiConfigureFieldList.stream()
                    .filter(e -> e.getDimension() == 1)
                    .map(e -> iTableName.getTableName(e.getFieldId(), e.getFieldType(), e.getFieldName(), e.dimension).getData())
                    .collect(toList());

            String existTableField = tableDataList.stream().map(e -> escapeStr[0] + e.getTableField() + escapeStr[1])
                    .collect(joining("," + DIMENSION_ALL_ALIAS_NAME + "."));

            wholeStr.append("SELECT " + DIMENSION_ALL_ALIAS_NAME + "." + existTableField);
            wholeStr.append("FROM (" + str + ")" + " AS " + DIMENSION_ALL_ALIAS_NAME + " ");

            // 带有维度名称
            List<TableDataDTO> collect3 = noTableData.stream()
                    .filter(e -> e.getRelationId() != null)
                    .map(e -> {
                        e.setDimensionName(iTableName.getDimensionName(e.getRelationId()).data);
                        return e;
                    }).collect(toList());

            // LEFT JOIN
            AtomicInteger num = new AtomicInteger();
            String leftJoin = collect3.stream()
                    .filter(e -> e.getRelationId() != null)
                    .map(e -> "LEFT JOIN (SELECT * FROM " + e.getTableName() + ") AS " + NO_DIMENSION_ALIAS_NAME + num.incrementAndGet()
                            + " ON " + DIMENSION_ALL_ALIAS_NAME + "." + e.getDimensionName() + "_key"
                            + "=" + NO_DIMENSION_ALIAS_NAME + num + "." + e.getDimensionName() + "_key ")
                    .collect(joining(" "));

            String collect1 = collect3.stream()
                    .filter(e -> e.getRelationId() == null)
                    .map(e -> "LEFT JOIN (SELECT * FROM " + e.getTableName() + ") AS " + NO_DIMENSION_ALIAS_NAME + num.incrementAndGet()
                            + " ON " + "1 = 1 ")
                    .collect(joining(" "));

            int num1 = 0;
            for (TableDataDTO noTableDatum : collect3) {
                if (noTableDatum.getRelationId() != null){
                    noTableDatum.setAlias(NO_DIMENSION_ALIAS_NAME + num1);
                }
            }

            // 判断追加 LEFT JOIN 还是笛卡尔积
            if (StringUtils.isBlank(leftJoin)){
                wholeStr.append(collect1);
            }else {
                wholeStr.append(leftJoin);
            }

            // WHERE
            wholeStr.append("WHERE");
            String collect2 = collect3.stream()
                    .map(e -> e.alias + "." + e.tableField + " IS NOT NULL")
                    .collect(joining(" AND "));
            wholeStr.append(collect2);
        }

        System.err.println(wholeStr);


        // 所有列的表名field集合
        List<TableDataDTO> tableDataList = apiConfigureFieldList.stream()
                .map(e -> iTableName.getTableName(e.getFieldId(), e.getFieldType(), e.getFieldName(), e.dimension).getData())
                .collect(toList());


        ResultEntity<TableDataDTO> resultEntity3 = iTableName.getTableName(10, WHERE, "year", 1);
        ResultEntity<TableDataDTO> resultEntity1 = iTableName.getTableName(10, COLUMN, "year", 1);
        ResultEntity<TableDataDTO> resultEntity2 = iTableName.getTableName(116, VALUE, "money", 1);

        String queryFieldList = apiConfigureFieldList.stream()
                // 查询字段: select列
                // 日期.年,日期.月,维度.金额
                // `date.year`,`date.month`,`dimension.money`
                .filter(e -> e.getFieldType().equals(COLUMN))
                .map(e -> iTableName.getTableName(e.getFieldId(), e.getFieldType(), e.getFieldName(), e.dimension).getData().tableName
                        + "." + escapeStr[0] + e.getFieldName()
                        + escapeStr[1])
                .collect(joining(","));

        String conditionList = apiConfigureFieldList.stream()
                // 权限控制字段: where条件
                // `date.year`=`2021` and `date.month`=`3`
                .filter(e -> e.getFieldType().equals(WHERE))
                .map(e -> iTableName.getTableName(e.getFieldId(), e.getFieldType(), e.getFieldName(), e.dimension).getData().tableName
                        + "." + escapeStr[0] + e.getFieldName() + escapeStr[1] + e.getWhere() + "'" + e.getWhereValue() + "'")
                .collect(joining("AND "));

        String aggregationList = apiConfigureFieldList.stream()
                // 聚合字段: avg()  sum()  max()  min()  count()
                // 如维度金额求和:  sum(`money`)
                .filter(e -> e.getFieldType().equals(VALUE))
                .map(e -> iTableName.getAggregation(e.getFieldId()).getData()
                        + "(" + iTableName.getTableName(e.getFieldId(), e.getFieldType(), e.getFieldName(), e.dimension).getData().tableName +
                        "." + escapeStr[0] + e.getFieldName() + escapeStr[1] + ")")
                .collect(joining(","));

        String groupingList = apiConfigureFieldList.stream()
                // 分组字段: group by
                // 根据年,月,产维度金额分组: `date.year`,`date.month`,`dimension.money`
                .filter(e -> e.getFieldType().equals(COLUMN))
                .map(e -> iTableName.getTableName(e.getFieldId(), e.getFieldType(), e.getFieldName(), e.dimension).getData().tableName
                        + "." + escapeStr[0] + e.getFieldName() + escapeStr[1])
                .collect(joining(","));
        return this.splicingSql(queryFieldList, aggregationList, groupingList, conditionList, currentPage, pageSize, tableDataList);
    }

    /**
     * 拼接sql
     *
     * @param aggregationList 聚合
     * @param groupingList    分组
     * @param conditionList   条件
     * @param currentPage     分页
     * @param pageSize
     * @param
     * @return
     */
    public List<Map> splicingSql(String queryFieldList,
                                 String aggregationList,
                                 String groupingList,
                                 String conditionList,
                                 Integer currentPage, Integer pageSize,
                                 List<TableDataDTO> tableDataList) {
        // 最终SQL
        String splitSql = this.splitSql(queryFieldList, aggregationList, groupingList, conditionList, tableDataList);
        System.err.println(splitSql);

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
     * @param
     * @return
     */
    public String splitSql(String queryFieldList, String aggregationList, String groupingList, String conditionList,
                           List<TableDataDTO> tableDataList) {
        // 转义符
        String[] escapeStr = getEscapeStr();

        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        // select
        // SELECT `date.year`,`date.month`,`dimension.money`
        this.queryField(str, queryFieldList, aggregationList, groupingList);

        // from date  主表
        str.append(" FROM ").append(escapeStr[0] + tableDataList.get(0).tableName + escapeStr[1]).append(" ");

        // TODO: 缺join
        str.append("join ").append(tableDataList.get(1).tableName).append(" ");
        str.append("on ");

        TableDataDTO tableData = new TableDataDTO();
        for (TableDataDTO dto : tableDataList) {
            if (dto.getType().equals(VALUE)) {
                tableData.setId(dto.getId());
                tableData.setTableField(dto.getTableField());
                tableData.setTableName(dto.getTableName());
            }
        }

        String collect = tableDataList.stream()
                .filter(e -> e.getType().equals(COLUMN))
                .map(e -> tableData.tableName + "." + escapeStr[0] + e.tableField + escapeStr[1] + "=" +
                        e.tableName + "." + escapeStr[0] + e.tableField + escapeStr[1])
                .collect(joining(" AND "));

        // A.nian=B.nian
        str.append(collect);

        // where `date.year`=`2021` and `date.month`=`3`
        if (StringUtils.isNotBlank(conditionList)) {
            str.append("WHERE " + conditionList + " ");
        }

        if (StringUtils.isNotBlank(groupingList)) {
            str.append("GROUP BY ");
            // group by data.year
            str.append(groupingList);
        }
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

        // 避免分组的和查询的语句相同
        if (StringUtils.isNotBlank(groupingList) && !queryFieldList.equals(groupingList)) {
            str.append(groupingList);
        }
        if (!queryFieldList.equals(groupingList) && StringUtils.isNotBlank(groupingList) && StringUtils.isNotBlank(aggregationList)) {
            str.append(",");
        }
        if (StringUtils.isNotBlank(aggregationList)) {
            str.append(aggregationList);
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
