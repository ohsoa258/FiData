package com.fisk.system.service;

import com.fisk.common.service.sqlparser.model.TableMetaDataObject;
import com.fisk.system.dto.SqlCheckDTO;

import java.util.List;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-27 13:51
 * @description
 */

public interface SqlFactoryService {
    /**
     * SQL语句校验
     *
     * @param sqlCheckDTO
     * @return
     */
    List<TableMetaDataObject> sqlCheck(SqlCheckDTO sqlCheckDTO);
}
