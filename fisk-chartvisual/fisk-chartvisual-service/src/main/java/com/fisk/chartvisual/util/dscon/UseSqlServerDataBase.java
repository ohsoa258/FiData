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
        str.append("SELECT ");
        str.append("a.name as tableName, ");
        str.append("b.name as columnName ");
        str.append("FROM ");
        str.append("sysobjects a ");
        str.append("INNER JOIN syscolumns b ON a.id = b.id ");
        str.append("WHERE ");
        str.append("a.xtype= 'u' ");
        return str.toString();
    }
}
