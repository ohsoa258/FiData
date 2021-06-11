package com.fisk.chartvisual.util.dbhelper.buildsql;

import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.dto.ColumnDetails;
import com.fisk.chartvisual.dto.SlicerQueryObject;
import com.fisk.common.enums.chartvisual.ColumnTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gy
 */
public class BuildSqlServerCommandImpl implements IBuildSQLCommand {

    @Override
    public String buildDataDomainQuery(String dbName) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT DISTINCT ");
        str.append("objects.name as tableName, ");
        str.append("props.value as tableDetails, ");
        str.append("col.columnName, ");
        str.append("col.columnDetails ");
        str.append("FROM ");
        str.append("syscolumns columns ");
        str.append("LEFT JOIN systypes types ON columns.xusertype= types.xusertype ");
        str.append("INNER JOIN sysobjects objects ON columns.id= objects.id  ");
        str.append("AND objects.xtype= 'U'  ");
        str.append("AND objects.name<> 'dtproperties' ");
        str.append("LEFT JOIN syscomments comments ON columns.cdefault= comments.id ");
        str.append("LEFT JOIN sys.extended_properties pro ON columns.id= pro.major_id  ");
        str.append("AND columns.colid= pro.minor_id ");
        str.append("LEFT JOIN sys.extended_properties props ON objects.id= props.major_id  ");
        str.append("AND props.minor_id= 0 ");
        str.append("LEFT JOIN ( ");
        str.append("SELECT ");
        str.append("tables.name AS table_name, ");
        str.append("columns.name AS columnName, ");
        str.append("props.value AS columnDetails  ");
        str.append("FROM ");
        str.append("sys.tables tables ");
        str.append("INNER JOIN sys.columns columns ON columns.object_id = tables.object_id ");
        str.append("LEFT JOIN sys.extended_properties props ON props.major_id = columns.object_id ");
        str.append("AND props.minor_id = columns.column_id  ");
        str.append(") col ON col.table_name = objects.name ");
        return str.toString();
    }

    @Override
    public String buildQueryData(ChartQueryObject query) {
        List<ColumnDetails> values = query.columnDetails.stream().filter(e -> e.columnType == ColumnTypeEnum.VALUE).collect(Collectors.toList());
        List<ColumnDetails> names = query.columnDetails.stream().filter(e -> e.columnType == ColumnTypeEnum.NAME).collect(Collectors.toList());
        if (values.size() == 0 || names.size() == 0) {
            throw new FkException(ResultEnum.VISUAL_PARAMTER_ERROR);
        }

        ColumnDetails queryColumns = names.get(names.size() - 1);

        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        switch (query.interactiveType) {
            case DRILL:
            case DEFAULT:
                str.append("[").append(queryColumns.columnName).append("]").append(" as [").append(queryColumns.columnLabel).append("] ");
                break;
            case LINKAGE:
                str.append(names.stream().map(e -> "[" + e.columnName + "] as [" + e.columnLabel + "]").collect(Collectors.joining(",")));
                break;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
        values.forEach(e -> {
            str.append(",").append(e.aggregationType.getName()).append("([").append(e.columnName).append("]) as ").append("[").append(e.columnLabel).append("] ");
        });
        str.append("FROM ").append(query.tableName).append(" ");
        if (query.queryFilters != null) {
            str.append("WHERE 1 = 1 ");
            query.queryFilters.forEach(e -> {
                str.append("AND [").append(e.columnName).append("] = '").append(e.value).append("' ");
            });
        }
        str.append("GROUP BY ");
        str.append(names.stream().map(e -> "[" + e.columnName + "]").collect(Collectors.joining(",")));

        return str.toString();
    }

    @Override
    public String buildQuerySlicer(SlicerQueryObject query) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT [").append(query.columnName).append("] FROM ").append(query.tableName);
        if (query.queryFilters != null) {
            str.append(" WHERE 1 = 1 ");
            query.queryFilters.forEach(e -> {
                str.append("AND [").append(e.columnName).append("] = '").append(e.value).append("' ");
            });
        }
        str.append(" GROUP BY ");
        str.append("[").append(query.columnName).append("]");
        return str.toString();
    }
}
