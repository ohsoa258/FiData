package com.fisk.chartvisual.util.dbhelper.buildsql;

import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.dto.ColumnDetails;
import com.fisk.chartvisual.dto.SlicerQueryObject;
import com.fisk.common.enums.chartvisual.ColumnTypeEnum;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gy
 */
public abstract class BaseBuildSqlCommand implements IBuildSqlCommand {

    /**
     * 根据数据源类型，查询参数对象动态创建sql语句(图表数据)
     * @param query 查询参数对象
     * @param type 数据源类型
     * @return 查询语句
     */
    protected String baseBuildQueryData(ChartQueryObject query, DataSourceTypeEnum type) {
        List<ColumnDetails> values = query.columnDetails.stream().filter(e -> e.columnType == ColumnTypeEnum.VALUE).collect(Collectors.toList());
        List<ColumnDetails> names = query.columnDetails.stream().filter(e -> e.columnType == ColumnTypeEnum.NAME).collect(Collectors.toList());

        if (values.size() == 0 || names.size() == 0) {
            throw new FkException(ResultEnum.VISUAL_PARAMTER_ERROR);
        }
        ColumnDetails queryColumns = names.get(names.size() - 1);
        String[] arr = getEscapeStr(type);

        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        switch (query.interactiveType) {
            case DRILL:
            case DEFAULT:
                str.append(arr[0]).append(queryColumns.columnName).append(arr[1]).append(" as ").append(arr[0]).append(queryColumns.columnLabel).append(arr[1]);
                break;
            case LINKAGE:
                str.append(names.stream().map(e -> arr[0] + e.columnName + arr[1] + " as " + arr[0] + e.columnLabel + arr[1]).collect(Collectors.joining(",")));
                break;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
        values.forEach(e -> {
            str.append(",").append(e.aggregationType.getName()).append("(").append(arr[0]).append(e.columnName).append(arr[1]).append(") as ").append(arr[0]).append(e.columnLabel).append(arr[1]);
        });
        str.append("FROM ").append(query.tableName).append(" ");
        if (query.queryFilters != null) {
            str.append("WHERE 1 = 1 ");
            query.queryFilters.forEach(e -> {
                str.append("AND ").append(arr[0]).append(e.columnName).append(arr[1]).append(" = '").append(e.value).append("' ");
            });
        }
        str.append("GROUP BY ");
        str.append(names.stream().map(e -> arr[0] + e.columnName + arr[1]).collect(Collectors.joining(",")));

        return str.toString();
    }

    /**
     * 根据数据源类型，查询参数对象动态创建sql语句(切片器数据)
     * @param query 查询参数对象
     * @param type 数据源类型
     * @return 查询语句
     */
    protected String baseBuildQuerySlicer(SlicerQueryObject query, DataSourceTypeEnum type) {
        String[] arr = getEscapeStr(type);
        StringBuilder str = new StringBuilder();
        str.append("SELECT ").append(arr[0]).append(query.columnName).append(arr[1]).append(" FROM ").append(query.tableName);
        if (query.queryFilters != null) {
            str.append(" WHERE 1 = 1 ");
            query.queryFilters.forEach(e -> {
                str.append("AND ").append(arr[0]).append(e.columnName).append(arr[1]).append(" = '").append(e.value).append("' ");
            });
        }
        str.append(" GROUP BY ");
        str.append(arr[0]).append(query.columnName).append(arr[1]);
        return str.toString();
    }

    /**
     * 根据数据源类型获取转义字符
     * @param type 数据源类型
     * @return 转义字符
     */
    private String[] getEscapeStr(DataSourceTypeEnum type) {
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
}
