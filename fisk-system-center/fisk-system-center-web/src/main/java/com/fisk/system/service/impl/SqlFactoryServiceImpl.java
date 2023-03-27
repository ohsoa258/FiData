package com.fisk.system.service.impl;

import com.fisk.common.service.sqlparser.SqlParserUtils;
import com.fisk.common.service.sqlparser.model.TableMetaDataObject;
import com.fisk.system.service.SqlFactoryService;
import org.springframework.stereotype.Service;

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

    @Override
    public List<TableMetaDataObject> sqlCheck(String sql, String dbType) {
        return SqlParserUtils.sqlDriveConversion(null,sql, dbType);
    }
}
