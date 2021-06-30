package com.fisk.chartvisual.util.dbhelper.buildsql;

import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.dto.SlicerQueryObject;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;

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
    public String buildQueryData(ChartQueryObject query) {
        return baseBuildQueryData(query, dsType);
    }

    @Override
    public String buildQuerySlicer(SlicerQueryObject query) {
        return baseBuildQuerySlicer(query, dsType);
    }
}
