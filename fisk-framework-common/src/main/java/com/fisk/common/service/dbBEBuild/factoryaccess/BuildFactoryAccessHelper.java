package com.fisk.common.service.dbBEBuild.factoryaccess;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.factoryaccess.impl.BuildAccessMySqlCommandImpl;
import com.fisk.common.service.dbBEBuild.factoryaccess.impl.BuildAccessOracleCommandImpl;
import com.fisk.common.service.dbBEBuild.factoryaccess.impl.BuildAccessPgCommandImpl;
import com.fisk.common.service.dbBEBuild.factoryaccess.impl.BuildAccessSqlServerCommandImpl;

/**
 * @author JianWenYang
 */
public class BuildFactoryAccessHelper {

    public static IBuildAccessSqlCommand getDBCommand(DataSourceTypeEnum type) {
        switch (type) {
            case SQLSERVER:
                return new BuildAccessSqlServerCommandImpl();
            case POSTGRESQL:
                return new BuildAccessPgCommandImpl();
            case ORACLE:
                return new BuildAccessOracleCommandImpl();
            case MYSQL:
                return new BuildAccessMySqlCommandImpl();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}
