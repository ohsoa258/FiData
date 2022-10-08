package com.fisk.task.listener.postgre.datainput.impl;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.task.listener.postgre.datainput.IbuildTable;

/**
 * @author cfk
 * 工厂类
 */
public class BuildFactoryHelper {

    public static IbuildTable getDBCommand(DataSourceTypeEnum type) {
        switch (type) {
            case SQLSERVER:
                return new BuildSqlServerTableImpl();
            case POSTGRESQL:
                return new BuildPgTableImpl();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}
