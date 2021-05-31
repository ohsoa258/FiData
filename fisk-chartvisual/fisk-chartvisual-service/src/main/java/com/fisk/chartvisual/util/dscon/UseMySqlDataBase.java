package com.fisk.chartvisual.util.dscon;

import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.dto.ColumnDetails;
import com.fisk.common.enums.chartvisual.ColumnTypeEnum;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * mysql
 *
 * @author gy
 */
@Slf4j
public class UseMySqlDataBase extends AbstractUseDataBase {
    public UseMySqlDataBase() {
        super(DataSourceTypeEnum.MYSQL);
    }

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
        ColumnDetails names = query.columnDetails.stream().filter(e -> e.columnType == ColumnTypeEnum.NAME).findFirst().orElse(null);
        if (values.size() == 0 || names == null) {
            throw new FkException(ResultEnum.VISUAL_PARAMTER_ERROR);
        }

        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        str.append(names.columnName).append(" as name ");
        values.forEach(e -> {
            str.append(",").append(e.aggregationType.getName()).append("(").append(e.columnName).append(") as value ");
        });
        str.append("FROM ").append(query.tableName).append(" ");
        str.append("GROUP BY ");
        str.append(names.columnName);
        return str.toString();
    }
}
