package com.fisk.dataaccess.utils.keepnumberfactory.impl;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.utils.keepnumberfactory.IBuildKeepNumber;

/**
 * @author lsj
 */
public class BuildKeepNumberSqlHelper {

    public static IBuildKeepNumber getKeepNumberSqlHelperByConType(com.fisk.common.core.enums.dataservice.DataSourceTypeEnum conType) {
        switch (conType) {
            case SQLSERVER:
                return new KeepNumberSqlServerImpl();
            case POSTGRESQL:
                return new KeepNumberPgImpl();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

}
