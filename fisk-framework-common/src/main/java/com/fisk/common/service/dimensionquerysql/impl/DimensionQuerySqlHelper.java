package com.fisk.common.service.dimensionquerysql.impl;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dimensionquerysql.IBuildDimensionQuerySql;

/**
 * @author lsj
 */
public class DimensionQuerySqlHelper {

    public static IBuildDimensionQuerySql getDimensionQuerySqlHelperByConType(DataSourceTypeEnum sourceType) {
        switch (sourceType) {
            case SQLSERVER:
                return new DimensionQuerySqlSqlServerImpl();
            case POSTGRESQL:
                return new DimensionQuerySqlPgSqlImpl();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

}
