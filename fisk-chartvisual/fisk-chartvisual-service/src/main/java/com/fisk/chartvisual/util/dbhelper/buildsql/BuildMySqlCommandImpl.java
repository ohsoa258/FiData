package com.fisk.chartvisual.util.dbhelper.buildsql;

import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.dto.ColumnDetails;
import com.fisk.chartvisual.dto.SlicerQueryObject;
import com.fisk.common.enums.chartvisual.ColumnTypeEnum;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;

/**
 * @author gy
 */
public class BuildMySqlCommandImpl extends BaseBuildSqlCommand {

    private final DataSourceTypeEnum dsType = DataSourceTypeEnum.MYSQL;

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
    public String buildQueryData(ChartQueryObject query, boolean aggregation) {
        String sql = baseBuildQueryData(query, dsType, aggregation);
        //拼装分页信息
        if (query.pagination != null && query.pagination.enablePage && !aggregation) {
            //如果没有开启排序，默认使用第一个聚合值排序
            return paginationSql(sql, query);
        }
        return sql;
    }

    @Override
    public String buildQueryAggregation(ChartQueryObject query) {
        return bseBuildQueryAggregation(query, DataSourceTypeEnum.MYSQL);
    }

    @Override
    public String buildQuerySlicer(SlicerQueryObject query) {
        String sql = baseBuildQuerySlicer(query, dsType);
        if (query.pagination != null) {
            return paginationSql(sql, query.pagination.pageNum, query.pagination.pageSize);
        }
        return sql;
    }

    /**
     * sql追加分页
     *
     * @param sql   查询语句
     * @param query 查询对象
     * @return 带分页的sql
     */
    private String paginationSql(String sql, ChartQueryObject query) {
        if (!query.pagination.enableOrder) {
            ColumnDetails valueDetails = query.columnDetails.stream().filter(e -> e.columnType == ColumnTypeEnum.VALUE).findFirst().orElse(null);
            if (valueDetails == null) {
                throw new FkException(ResultEnum.PARAMTER_ERROR);
            }
            sql += " ORDER BY " + getColumn(valueDetails.columnLabel, getEscapeStr(DataSourceTypeEnum.MYSQL)) + " " + query.pagination.ascType.getName();
        }
        sql += " LIMIT " + (query.pagination.pageNum - 1) * query.pagination.pageSize + "," + query.pagination.pageSize;
        return sql;
    }

    /**
     * sql追加分页
     *
     * @param sql      查询语句
     * @param pageNum  页码
     * @param pageSize 分页条数
     * @return 带分页的sql
     */
    private String paginationSql(String sql, int pageNum, int pageSize) {
        sql += " LIMIT " + (pageNum - 1) * pageSize + "," + pageSize;
        return sql;
    }

}
