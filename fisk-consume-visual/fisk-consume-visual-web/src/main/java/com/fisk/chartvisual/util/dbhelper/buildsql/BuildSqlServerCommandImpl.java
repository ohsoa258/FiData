package com.fisk.chartvisual.util.dbhelper.buildsql;

import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.dto.SlicerQueryObject;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;

/**
 * @author gy
 */
public class BuildSqlServerCommandImpl extends BaseBuildSqlCommand {

    private final DataSourceTypeEnum dsType = DataSourceTypeEnum.SQLSERVER;

    @Override
    public String buildDataDomainQuery(String dbName) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT DISTINCT ");
        str.append("objects.name as tableName, ");
        str.append("cast(props.value as nvarchar(50)) as tableDetails, ");
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
    public String buildQueryData(ChartQueryObject query, boolean aggregation) {
        String sql = baseBuildQueryData(query, dsType, aggregation);
        if (query.pagination != null && query.pagination.enablePage && !aggregation) {
            return paginationSql(sql, query);
        }
        return sql;
    }

    @Override
    public String buildQueryAggregation(ChartQueryObject query) {
        return bseBuildQueryAggregation(query, DataSourceTypeEnum.SQLSERVER);
    }

    @Override
    public String buildQuerySlicer(SlicerQueryObject query) {
        String sql = baseBuildQuerySlicer(query, dsType);
        if (query.pagination != null) {
            return paginationSql(sql, query.columnName, query.pagination.pageNum, query.pagination.pageSize);
        }
        return sql;
    }

    @Override
    public String buildQueryAllTables(String databaseName) {
        StringBuilder str = new StringBuilder();
        str.append("select name AS tableName from sys.tables");
        return str.toString();
    }

    @Override
    public String buildQueryFiled(String databaseName, String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("select name from syscolumns where id=object_id('").append(tableName).append("')");
        return str.toString();
    }

    @Override
    public String getData(String tableName, Integer total,String filed) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT * FROM ").append(tableName);
        str.append(" ORDER BY ").append(filed);
        str.append(" DESC OFFSET 0 ROWS FETCH NEXT ").append(total);
        str.append(" ROWS ONLY");
        return str.toString();
    }

    @Override
    public String buildQueryFiledInfo(String tableName) {
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append("field=a.name, ");
        str.append("type=b.name, ");
        str.append("fieldInfo=isnull(g.[value],'') ");
        str.append("FROM syscolumns a ");
        str.append("left join systypes b on a.xusertype=b.xusertype ");
        str.append("inner join sysobjects d on a.id=d.id and d.xtype in('U','V') and  d.name<>'dtproperties' ");
        str.append("left join syscomments e on a.cdefault=e.id ");
        str.append("left join sys.extended_properties g on a.id=g.major_id and a.colid=g.minor_id ");
        str.append("left join sys.extended_properties f on d.id=f.major_id and f.minor_id=0 ");
        str.append("where d.name='").append(tableName).append("' ");
        str.append("order by a.id,a.colorder");
        return str.toString();
    }

    /**
     * sql追加分页
     *
     * @param sql   查询语句
     * @param query 查询对象
     * @return 带分页的sql
     */
    private String paginationSql(String sql, ChartQueryObject query) {
        return "SELECT TOP " + query.pagination.pageSize + " * FROM (" + sql + ") AS tab WHERE RowNumber > " + (query.pagination.pageNum - 1) * query.pagination.pageSize;
    }

    /**
     * sql追加分页
     *
     * @param sql        查询语句
     * @param columnName 字段名
     * @param pageNum    页码
     * @param pageSize   条数
     * @return 带分页的sql
     */
    private String paginationSql(String sql, String columnName, int pageNum, int pageSize) {
        return "SELECT TOP " + pageSize + " " + columnName + " FROM (" + sql + ") AS tab WHERE RowNumber > " + (pageNum - 1) * pageSize;
    }
}
