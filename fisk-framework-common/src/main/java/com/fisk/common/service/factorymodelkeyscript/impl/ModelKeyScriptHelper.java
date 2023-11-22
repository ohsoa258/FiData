package com.fisk.common.service.factorymodelkeyscript.impl;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.factorycodepreview.IBuildFactoryCodePreview;
import com.fisk.common.service.factorycodepreview.impl.FactoryCodePreviewPgSqlImpl;
import com.fisk.common.service.factorycodepreview.impl.FactoryCodePreviewSqlServerImpl;
import com.fisk.common.service.factorymodelkeyscript.IBuildFactoryModelKeyScript;

/**
 * @author lsj
 */
public class ModelKeyScriptHelper {

    public static IBuildFactoryModelKeyScript getKeyScriptHelperByConType(DataSourceTypeEnum sourceType) {
        switch (sourceType) {
            case SQLSERVER:
                return new FactoryModelKeyScriptSqlServerImpl();
            case POSTGRESQL:
                return new FactoryModelKeyScriptPgSqlImpl();
            case DORIS:
                return new FactoryModelKeyScriptDorisSqlImpl();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

}
