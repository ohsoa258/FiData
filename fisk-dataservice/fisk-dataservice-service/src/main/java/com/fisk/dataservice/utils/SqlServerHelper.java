package com.fisk.dataservice.utils;

import com.fisk.common.enums.dataservice.DataSourceTypeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * sqlserver
 * @author dick
 */
@Slf4j
public class SqlServerHelper extends AbstractDbHelper {
    public SqlServerHelper() {
        super(DataSourceTypeEnum.SQLSERVER);
    }

}
