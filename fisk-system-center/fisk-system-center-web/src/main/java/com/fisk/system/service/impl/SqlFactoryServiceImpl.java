package com.fisk.system.service.impl;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.sqlparser.SqlParserUtils;
import com.fisk.common.service.sqlparser.model.TableMetaDataObject;
import com.fisk.system.dto.SqlCheckDTO;
import com.fisk.system.service.SqlFactoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
     *
     * @param sqlCheckDTO
     * @return
     */
    @Override
    public List<TableMetaDataObject> sqlCheck(SqlCheckDTO sqlCheckDTO) {
        String dbType = sqlCheckDTO.dbType;
        String sql = sqlCheckDTO.sql;
        boolean flag = ("mysql").equals(dbType) || (("oracle").equals(dbType) || (("postgresql").equals(dbType) || (("sqlserver").equals(dbType))));
        if (StringUtils.isEmpty(sql) || StringUtils.isEmpty(dbType))
            throw new FkException(ResultEnum.SQL_PARAMETER_NOTNULL);
        else if (!flag)
            throw new FkException(ResultEnum.SQL_Not_SUPPORTED_YET_DBTYPE);

        return SqlParserUtils.sqlDriveConversion(null, dbType, sql);
    }
}
