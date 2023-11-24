package com.fisk.common.service.factorymdmdomainscript.impl;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.factorymdmdomainscript.IBuildMdmScript;

/**
 * @author wangjian
 */
public class MdmScriptHelper {

    public static IBuildMdmScript getDomainScriptHelperByConType(DataSourceTypeEnum sourceType) {
        switch (sourceType) {
            case SQLSERVER:
                return new FactoryMdmDomainScriptPgSqlImpl();
            case POSTGRESQL:
                return new FactoryMdmDomainScriptPgSqlImpl();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

}
