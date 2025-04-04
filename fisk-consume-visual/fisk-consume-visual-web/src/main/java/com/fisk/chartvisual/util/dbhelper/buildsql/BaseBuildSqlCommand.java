package com.fisk.chartvisual.util.dbhelper.buildsql;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.chartvisual.dto.chartvisual.ChartQueryFilter;
import com.fisk.chartvisual.dto.chartvisual.ChartQueryObject;
import com.fisk.chartvisual.dto.chartvisual.ColumnDetails;
import com.fisk.chartvisual.dto.chartvisual.SlicerQueryObject;
import com.fisk.common.core.constants.SystemConstants;
import com.fisk.common.core.enums.chartvisual.ColumnTypeEnum;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.enums.chartvisual.TableOrderEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.fisk.chartvisual.enums.SsasChartFilterTypeEnum.*;
import static java.util.stream.Collectors.joining;

/**
 * @author gy
 */
public abstract class BaseBuildSqlCommand implements IBuildSqlCommand {

    /**
     * 根据数据源类型，查询参数对象动态创建sql语句(图表数据)
     *
     * @param query 查询参数对象
     * @param type  数据源类型
     * @return 查询语句
     */
    protected String baseBuildQueryData(ChartQueryObject query, DataSourceTypeEnum type, boolean aggregation) {
        List<ColumnDetails> values = query.columnDetails.stream().filter(e -> e.columnType == ColumnTypeEnum.VALUE).collect(Collectors.toList());
        List<ColumnDetails> names = query.columnDetails.stream().filter(e -> e.columnType == ColumnTypeEnum.NAME).collect(Collectors.toList());

        if (names.size() == 0) {
            throw new FkException(ResultEnum.VISUAL_PARAMTER_ERROR);
        }
        ColumnDetails queryColumns = names.get(names.size() - 1);
        String[] arr = getEscapeStr(type);

        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        //select
        if (aggregation) {
            //统计查询只需要查询聚合字段
            String columns = values.stream()
                    .map(e -> e.aggregationType.getName().replace(SystemConstants.BUILD_SQL_REPLACE_STR, getColumn(e.columnName, arr)) + " as " + getColumn(e.columnLabel, arr))
                    .collect(Collectors.joining(","));
            str.append(columns);
        } else {
            //非统计查询需要维度字段和聚合字段

            //维度字段
            switch (query.interactiveType) {
                case DRILL:
                case DEFAULT:
                    str.append(getColumn(queryColumns.columnName, arr)).append(" as ").append(getColumn(queryColumns.columnLabel, arr));
                    break;
                case LINKAGE:
                case TABLE:
                    str.append(names.stream().map(e -> getColumn(e.columnName, arr) + " as " + getColumn(e.columnLabel, arr)).collect(Collectors.joining(",")));
                    break;
                default:
                    throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
            }
            //聚合字段
            if (CollectionUtils.isNotEmpty(values)){
                values.forEach(e -> {
                    str.append(",").append(e.aggregationType.getName().replace(SystemConstants.BUILD_SQL_REPLACE_STR, getColumn(e.columnName, arr))).append(" as ").append(getColumn(e.columnLabel, arr));
                });
            }
        }
        if (query.pagination != null && query.pagination.enablePage && type == DataSourceTypeEnum.SQLSERVER && !aggregation) {
            String orderColumn = "";
            //根据排序字段类型拼接
            if (query.pagination.enableOrder) {
                switch (query.pagination.orderType) {
                    case NAME:
                        orderColumn = getColumn(query.pagination.orderColumn, arr);
                        break;
                    case VALUE:
                        orderColumn = query.pagination.aggregationType.getName().replace(SystemConstants.BUILD_SQL_REPLACE_STR, getColumn(query.pagination.orderColumn, arr));
                        break;
                    default:
                        throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
                }
            } else {
                //说明没有开启排序，默认取值字段的最后一个
                ColumnDetails valueDetails = values.get(values.size() - 1);
                orderColumn = valueDetails.aggregationType.getName().replace(SystemConstants.BUILD_SQL_REPLACE_STR, getColumn(valueDetails.columnName, arr));
            }
            str.append(rowNumber(orderColumn, query.pagination.ascType.getName()));
        }
        str.append(" FROM ").append(query.tableName).append(" ");
        //where
        if (query.queryFilters != null) {
            str.append("WHERE 1 = 1 ");
            str.append(queryFilter(query.queryFilters, arr,type));
        }
        //group
        str.append("GROUP BY ");
        str.append(names.stream().map(e -> getColumn(e.columnName, arr)).collect(Collectors.joining(",")));
        //order
        if (query.pagination != null && query.pagination.enableOrder && !aggregation) {
            switch (type) {
                case SQLSERVER:
                    //查询sqlserver时，如果开启了分页，那么就不需要排序了，row_number中已经排序了
                    if (!query.pagination.enablePage) {
                        str.append(" ORDER BY ").append(getColumn(query.pagination.orderColumn, arr)).append(" ").append(query.pagination.ascType.getName());
                    }
                    break;
                case MYSQL:
                    //mysql排序的时候，排序的字段名使用的是查询后的别名
                    String orderName = query.columnDetails.stream().filter(e -> e.columnName.equals(query.pagination.orderColumn)).map(ColumnDetails::getColumnLabel).findFirst().orElse(null);
                    str.append(" ORDER BY ").append(getColumn(orderName, arr)).append(" ").append(query.pagination.ascType.getName());
                    break;
                default:
                    throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
            }
        }
        return str.toString();
    }

    /**
     * 根据数据源类型，查询参数对象动态创建sql语句（统计查询）
     *
     * @param query 查询条件
     * @param type  数据源类型
     * @return sql
     */
    protected String bseBuildQueryAggregation(ChartQueryObject query, DataSourceTypeEnum type) {
        String[] arr = getEscapeStr(type);
        String columns = query.columnDetails.stream()
                .filter(e -> e.columnType == ColumnTypeEnum.VALUE)
                .map(e -> "SUM(" + getColumn(e.columnLabel, arr) + ")" + " as " + getColumn(e.columnLabel, arr))
                .collect(Collectors.joining(","));
        String sql = baseBuildQueryData(query, type, true);
        return "SELECT " + columns + " FROM (" + sql + ") AS tab";
    }

    /**
     * 根据数据源类型，查询参数对象动态创建sql语句(切片器数据)
     *
     * @param query 查询参数对象
     * @param type  数据源类型
     * @return 查询语句
     */
    protected String baseBuildQuerySlicer(SlicerQueryObject query, DataSourceTypeEnum type) {
        String ascType = TableOrderEnum.ASC.getName();
        String[] arr = getEscapeStr(type);
        String columnName = getColumn(query.columnName, arr);
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append(columnName);
        if (type == DataSourceTypeEnum.SQLSERVER) {
            str.append(rowNumber(columnName, ascType));
        }
        str.append(" FROM ").append(query.tableName);
        str.append(" WHERE 1 = 1 ");
        if (query.queryFilters != null) {
            str.append(queryFilter(query.queryFilters, arr,type));
        }
        if (StringUtils.isNotEmpty(query.likeValue)) {
            str.append("AND ").append(getColumn(query.columnName, arr)).append(" LIKE '%").append(query.likeValue).append("%'");
        }
        str.append(" GROUP BY ");
        str.append(columnName);
        //order sqlserver的在row_number中已经排序
        if (type == DataSourceTypeEnum.MYSQL) {
            str.append(" ORDER BY ").append(columnName).append(" ").append(ascType);
        }
        return str.toString();
    }

    /**
     * 根据数据源类型获取转义字符
     *
     * @param type 数据源类型
     * @return 转义字符
     */
    protected String[] getEscapeStr(DataSourceTypeEnum type) {
        String[] arr = new String[2];
        switch (type) {
            case MYSQL:
                arr[0] = "`";
                arr[1] = "`";
                break;
            case SQLSERVER:
                arr[0] = "[";
                arr[1] = "]";
                break;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
        return arr;
    }

    /**
     * 获取字段名（拼接转义字符）
     *
     * @param column    字段名
     * @param escapeStr 转义字符
     * @return 转义后的字段名 case： [name]
     */
    protected String getColumn(String column, String[] escapeStr) {
        if (column == null) {
            throw new FkException(ResultEnum.PARAMTER_ERROR);
        }
        return escapeStr[0] + column + escapeStr[1];
    }

    /**
     * 拼接row_number 字段
     *
     * @param orderColumn 排序字段
     * @param orderType   排序条件
     * @return sql
     */
    protected String rowNumber(String orderColumn, String orderType) {
        return ",ROW_NUMBER() OVER (ORDER BY " + orderColumn + " " + orderType + ") AS RowNumber";
    }

    /**
     * 拼接where条件
     *
     * @param filter    filter
     * @param escapeStr 转义字符
     * @return where
     */
    protected String queryFilter(List<ChartQueryFilter> filter, String[] escapeStr,DataSourceTypeEnum type) {
        StringBuilder str = new StringBuilder();
        filter.stream().filter(e -> e.getSsasChartFilterType() == FILTER).forEach(e -> {
            str.append("AND ");
            String name = getColumn(e.columnName, escapeStr);
            if (e.value.size() > 0) {
                String filterStr = e.value.stream().map(item -> "(" + name + " = '" + item + "')").collect(Collectors.joining(" OR "));
                str.append("(").append(filterStr).append(")");
            } else {
                str.append(name).append(" = '").append(e.value).append("' ");
            }
        });

        // 时间区间
        String slicerDateField = filter.stream()
                .filter(e -> e.getSsasChartFilterType() == SLICER)
                .map(e -> escapeStr[0] + e.getColumnName() + escapeStr[1] + " BETWEEN " + e.getStartTime() + " AND " + e.getEndTime() +" ")
                .collect(joining(" AND "));

        // 指定时间
        String serifedTime = this.serifedTime(filter, escapeStr, type);

        StringBuilder strTime = new StringBuilder();
        if (StringUtils.isNotBlank(slicerDateField)){
            strTime.append(slicerDateField);
        }
        if (StringUtils.isNotBlank(serifedTime)){
            strTime.append(serifedTime);
        }

        if (StringUtils.isNotBlank(str.toString()) && StringUtils.isNotBlank(strTime.toString())){
            this.additional(str,strTime);
        }else if (StringUtils.isNotBlank(strTime)){
            this.additional(str,strTime);
        }

        System.out.println(str);
        return str.toString();
    }

    /**
     * 追加where条件
     * @param str
     * @param strTime
     */
    public void additional(StringBuilder str,StringBuilder strTime){
        str.append(" AND ");
        str.append(strTime);
    }

    /**
     * 指定时间
     * @param filter
     * @param escapeStr
     * @param type
     * @return
     */
    public String serifedTime(List<ChartQueryFilter> filter, String[] escapeStr,DataSourceTypeEnum type){
        switch (type){
            case MYSQL:
                return filter.stream()
                        .filter(e -> e.getSsasChartFilterType() == APPOINT_SLICER)
                        .map(e -> escapeStr[0] + e.getColumnName() + escapeStr[1]
                                + " IN (" + JSON.toJSONString(e.getSpecifiedTime()).replace("[", " ").replace("]"," ") + ")")
                        .collect(joining(" AND "));
            case SQLSERVER:
                return filter.stream()
                        .filter(e -> e.getSsasChartFilterType() == APPOINT_SLICER)
                        .map(e -> escapeStr[0] + e.getColumnName() + escapeStr[1]
                                + " IN (" + JSON.toJSONString(Arrays.stream(e.getSpecifiedTime()).mapToInt(Integer::parseInt).toArray()).replace("[", " ").replace("]"," ") + ")")
                        .collect(joining(" AND "));
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}
