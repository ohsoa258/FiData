package com.fisk.mdm.utils.mdmBEBuild;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.utils.mdmBEBuild.impl.BuildPgCommandImpl;
import com.fisk.mdm.utils.mdmBEBuild.impl.BuildSqlServerCommandImpl;

/**
 * @author WangYan
 * @date 2022/4/13 18:25
 * 工厂类
 */
public class BuildFactoryHelper {

    public static IBuildSqlCommand getDBCommand(DataSourceTypeEnum type){
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
