package com.fisk.common.service.dbBEBuild.factoryaccess;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.factoryaccess.impl.*;

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
            case OPENEDGE:
                return new BuildAccessOpenEdgeCommandImpl();
            case SAPBW:
                return new BuildAccessSapBwCommandImpl();
            case DORIS:
            case MONGODB:
                return new BuildAccessDorisCommandImpl();
            case DM8:
                return new BuildAccessDM8CommandImpl();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}
