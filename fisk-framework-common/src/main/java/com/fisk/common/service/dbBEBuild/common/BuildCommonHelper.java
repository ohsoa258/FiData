package com.fisk.common.service.dbBEBuild.common;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.common.impl.BuildCommonMySqlCommand;
import com.fisk.common.service.dbBEBuild.common.impl.BuildCommonOracleCommand;
import com.fisk.common.service.dbBEBuild.common.impl.BuildCommonPgSqlCommand;
import com.fisk.common.service.dbBEBuild.common.impl.BuildCommonSqlServerCommand;

/**
 * @author JianWenYang
 */
public class BuildCommonHelper {

    public static IBuildCommonSqlCommand getCommand(DataSourceTypeEnum dataSourceTypeEnum) {
        switch (dataSourceTypeEnum) {
            case MYSQL:
                return new BuildCommonMySqlCommand();
            case POSTGRESQL:
                return new BuildCommonPgSqlCommand();
            case SQLSERVER:
                return new BuildCommonSqlServerCommand();
            case ORACLE:
                return new BuildCommonOracleCommand();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

}
