package com.fisk.chartvisual.util.dbhelper;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * sqlserver
 * @author gy
 */
@Slf4j
public class SqlServerHelper extends AbstractDbHelper {
    public SqlServerHelper() {
        super(DataSourceTypeEnum.SQLSERVER);
    }

}
