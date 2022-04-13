package com.fisk.task.utils;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.task.utils.buildSql.IBuildSqlCommand;
import com.fisk.task.utils.buildSql.impl.BuildPgCommandImpl;
import com.fisk.task.utils.buildSql.impl.BuildSqlServerCommandImpl;

/**
 * @author WangYan
 * @date 2022/4/13 18:25
 * 工厂类
 */
public class IBuildFactoryHelper {

    public static IBuildSqlCommand getSqlBuilder(DataSourceTypeEnum type){
        switch (type){
            case SQLSERVER:
                return new BuildSqlServerCommandImpl();
            case PG:
                return new BuildPgCommandImpl();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}
