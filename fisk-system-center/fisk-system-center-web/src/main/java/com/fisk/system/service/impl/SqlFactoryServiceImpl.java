package com.fisk.system.service.impl;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.sqlparser.SqlParserUtils;
import com.fisk.common.service.sqlparser.model.TableMetaDataObject;
import com.fisk.system.service.SqlFactoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-27 13:51
 * @description
 */
@Service
public class SqlFactoryServiceImpl implements SqlFactoryService {
    /**
     * SQL语句校验方法
     * @param sql
     * @param dbType
     * @return
     */
    @Override
    public List<TableMetaDataObject> sqlCheck(String sql, String dbType) {
        boolean flag=("mysql").equals(dbType)?true:false||("oracle").equals(dbType)?true:false||("postgresql").equals(dbType)?true:false||("sqlserver").equals(dbType)?true:false;
        if(StringUtils.isEmpty(sql)||StringUtils.isEmpty(dbType))
            throw new  FkException(ResultEnum.SQL_PARAMETER_NOTNULL);
         else if (!flag)
            throw new  FkException(ResultEnum.SQL_Not_SUPPORTED_YET_DBTYPE);

        return SqlParserUtils.sqlDriveConversion(null,dbType,sql);
    }
}
