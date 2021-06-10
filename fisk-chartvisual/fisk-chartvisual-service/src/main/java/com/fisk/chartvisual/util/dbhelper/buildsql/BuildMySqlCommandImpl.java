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
public class BuildMySqlCommandImpl implements IBuildSQLCommand {

    @Override
    public String buildDataDomainQuery(String dbName) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append("T.TABLE_NAME AS 'tableName', ");
        str.append("T.TABLE_COMMENT AS 'tableDetails', ");
        str.append("C.COLUMN_NAME AS 'columnName', ");
        str.append("C.COLUMN_COMMENT AS 'columnDetails'  ");
        str.append("FROM ");
        str.append("information_schema.`TABLES` T ");
        str.append("LEFT JOIN information_schema.`COLUMNS` C ON T.TABLE_NAME = C.TABLE_NAME  ");
        str.append("AND T.TABLE_SCHEMA = C.TABLE_SCHEMA  ");
        str.append("WHERE ");
        str.append("T.TABLE_SCHEMA = '").append(dbName).append("' ");
        str.append("ORDER BY ");
        str.append("C.TABLE_NAME,C.ORDINAL_POSITION; ");
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
                str.append("`").append(queryColumns.columnName).append("`").append(" as `").append(queryColumns.columnLabel).append("` ");
                break;
            case LINKAGE:
                str.append(names.stream().map(e -> "`" + e.columnName + "` as `" + e.columnLabel + "`").collect(Collectors.joining(",")));
                break;
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
        values.forEach(e -> {
            str.append(",").append(e.aggregationType.getName()).append("(`").append(e.columnName).append("`) as ").append("`").append(e.columnLabel).append("` ");
        });
        str.append("FROM ").append(query.tableName).append(" ");
        if (query.queryFilters != null) {
            str.append("WHERE 1 = 1 ");
            query.queryFilters.forEach(e -> {
                str.append("AND `").append(e.columnName).append("` = '").append(e.value).append("' ");
            });
        }
        str.append("GROUP BY ");
        str.append(names.stream().map(e -> "`" + e.columnName + "`").collect(Collectors.joining(",")));

        return str.toString();
    }

    @Override
    public String buildQuerySlicer(SlicerQueryObject query) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT `").append(query.columnName).append("` FROM ").append(query.tableName);
        if (query.queryFilters != null) {
            str.append(" WHERE 1 = 1 ");
            query.queryFilters.forEach(e -> {
                str.append("AND `").append(e.columnName).append("` = '").append(e.value).append("' ");
            });
        }
        str.append(" GROUP BY ");
        str.append("`").append(query.columnName).append("`");
        return str.toString();
    }
}
