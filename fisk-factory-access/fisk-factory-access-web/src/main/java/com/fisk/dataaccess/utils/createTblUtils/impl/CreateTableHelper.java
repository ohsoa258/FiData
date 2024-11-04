package com.fisk.dataaccess.utils.createTblUtils.impl;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.utils.createTblUtils.IBuildCreateTableFactory;

public class CreateTableHelper {

    public static IBuildCreateTableFactory getCreateTableHelperByConType(DataSourceTypeEnum sourceType) {
        switch (sourceType) {
            case SQLSERVER:
                return new FactoryCreateTableSqlserverImpl();
            case POSTGRESQL:
                return new FactoryCreateTablePgImpl();
            case MYSQL:
                return new FactoryCreateTableMysqlImpl();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

}
