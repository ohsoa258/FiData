package com.fisk.chartvisual.util.dbhelper;

import com.fisk.chartvisual.util.dbhelper.buildsql.BuildMySqlCommandImpl;
import com.fisk.chartvisual.util.dbhelper.buildsql.BuildSqlServerCommandImpl;
import com.fisk.chartvisual.util.dbhelper.buildsql.IBuildSqlCommand;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;

/**
 * @author gy
 */
public class DbHelperFactory {

    public static AbstractDbHelper getDbHelper(DataSourceTypeEnum type){
        switch (type){
            case SQLSERVER:
            case SQLSERVER_WINDOWS:
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
            case SQLSERVER_WINDOWS:
                return new BuildSqlServerCommandImpl();
            case MYSQL:
                return new BuildMySqlCommandImpl();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}
