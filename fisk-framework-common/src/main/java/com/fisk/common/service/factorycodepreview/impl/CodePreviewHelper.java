package com.fisk.common.service.factorycodepreview.impl;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.factorycodepreview.IBuildFactoryCodePreview;

/**
 * @author lsj
 */
public class CodePreviewHelper {

    public static IBuildFactoryCodePreview getSqlHelperByConType(DataSourceTypeEnum sourceType) {
        switch (sourceType) {
            case SQLSERVER:
                return new FactoryCodePreviewSqlServerImpl();
            case POSTGRESQL:
                return new FactoryCodePreviewPgSqlImpl();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

}
