package com.fisk.common.service.dbBEBuild.dataservice;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.dataservice.impl.BuildDataServiceMysqlCommandImpl;
import com.fisk.common.service.dbBEBuild.dataservice.impl.BuildDataServicePgCommandImpl;
import com.fisk.common.service.dbBEBuild.dataservice.impl.BuildDataServiceSqlServerCommandImpl;

/**
 * @author dick
 */
public class BuildDataServiceHelper {

    public static IBuildDataServiceSqlCommand getDBCommand(DataSourceTypeEnum type) {
        switch (type) {
            case SQLSERVER:
                return new BuildDataServiceSqlServerCommandImpl();
            case POSTGRESQL:
                return new BuildDataServicePgCommandImpl();
            case MYSQL:
                return new BuildDataServiceMysqlCommandImpl();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}
