package com.fisk.chartvisual.util.dscon;

import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * sqlserver
 * @author gy
 */
@Slf4j
public class UseSqlServerDataBase extends AbstractUseDataBase {
    public UseSqlServerDataBase() {
        super(DataSourceTypeEnum.SQLSERVER);
    }

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
}
