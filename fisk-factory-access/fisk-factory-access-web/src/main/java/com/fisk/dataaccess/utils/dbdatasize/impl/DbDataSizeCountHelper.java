package com.fisk.dataaccess.utils.dbdatasize.impl;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.utils.dbdatasize.IBuildFactoryDbDataSizeCount;

/**
 * @author lsj
 */
public class DbDataSizeCountHelper {

    public static IBuildFactoryDbDataSizeCount getDbDataSizeCountHelperByConType(DataSourceTypeEnum sourceType) {
        switch (sourceType) {
            case SQLSERVER:
                return new DbDataSizeCountSqlServerImpl();
            case POSTGRESQL:
                return new DbDataSizeCountPgSqlImpl();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

}
