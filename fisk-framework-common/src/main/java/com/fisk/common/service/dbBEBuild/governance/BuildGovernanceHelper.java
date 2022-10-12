package com.fisk.common.service.dbBEBuild.governance;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.governance.impl.BuildGovernancePgCommandImpl;
import com.fisk.common.service.dbBEBuild.governance.impl.BuildGovernanceSqlServerCommandImpl;

/**
 * @author dick
 */
public class BuildGovernanceHelper {

    public static IBuildGovernanceSqlCommand getDBCommand(DataSourceTypeEnum type) {
        switch (type) {
            case SQLSERVER:
                return new BuildGovernanceSqlServerCommandImpl();
            case POSTGRESQL:
                return new BuildGovernancePgCommandImpl();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}
