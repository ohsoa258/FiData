package com.fisk.common.service.dbBEBuild.datamodel;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.datamodel.impl.BuildDataModelOracleCommandImpl;
import com.fisk.common.service.dbBEBuild.datamodel.impl.BuildDataModelPgCommandImpl;
import com.fisk.common.service.dbBEBuild.datamodel.impl.BuildDataModelSqlServerCommandImpl;

/**
 * @author JianWenYang
 */
public class BuildDataModelHelper {

    public static IBuildDataModelSqlCommand getDBCommand(DataSourceTypeEnum type) {
        switch (type) {
            case SQLSERVER:
                return new BuildDataModelSqlServerCommandImpl();
            case POSTGRESQL:
                return new BuildDataModelPgCommandImpl();
            case ORACLE:
                return new BuildDataModelOracleCommandImpl();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

}
