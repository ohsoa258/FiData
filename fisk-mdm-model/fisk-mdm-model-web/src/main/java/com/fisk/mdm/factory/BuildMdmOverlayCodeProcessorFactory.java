package com.fisk.mdm.factory;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.factory.impl.MysqlBuildMdmOverlayCodeProcessor;
import com.fisk.mdm.factory.impl.OracleBuildMdmOverlayCodeProcessor;
import com.fisk.mdm.factory.impl.PGBuildMdmOverlayCodeProcessor;
import com.fisk.mdm.factory.impl.SqlServerBuildMdmOverlayCodeProcessor;

/**
 * @Author: wangjian
 * @Date: 2023-12-26
 * @Description:
 */
public class BuildMdmOverlayCodeProcessorFactory {
    public static BuildMdmOverlayCodeProcessor getDBProcessor(DataSourceTypeEnum type) {
        switch (type) {
            case SQLSERVER:
                return new SqlServerBuildMdmOverlayCodeProcessor();
            case POSTGRESQL:
                return new PGBuildMdmOverlayCodeProcessor();
            case MYSQL:
                return new MysqlBuildMdmOverlayCodeProcessor();
            case ORACLE:
                return new OracleBuildMdmOverlayCodeProcessor();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}
