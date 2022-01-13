package com.fisk.dataservice.utils;

import com.fisk.dataservice.utils.buildsql.BuildMySqlCommandImpl;
import com.fisk.dataservice.utils.buildsql.BuildSqlServerCommandImpl;
import com.fisk.dataservice.utils.buildsql.IBuildSqlCommand;
import com.fisk.common.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;

/**
 * @author dick
 */
public class DbHelperFactory {

    public static AbstractDbHelper getDbHelper(DataSourceTypeEnum type){
        switch (type){
            case SQLSERVER:
                return new SqlServerHelper();
            case MYSQL:
                return new MySqlHelper();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

    public static IBuildSqlCommand getSqlBuilder(DataSourceTypeEnum type){
        switch (type){
            case SQLSERVER:
                return new BuildSqlServerCommandImpl();
            case MYSQL:
                return new BuildMySqlCommandImpl();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}
