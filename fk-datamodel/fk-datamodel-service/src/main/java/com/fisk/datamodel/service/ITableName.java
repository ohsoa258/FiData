package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEntity;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;

/**
 * @author Lock
 */
public interface ITableName {
    /**
     * 查询表名
     * @param id id
     * @param type type
     * @return tableName
     */
    ResultEntity<String> getTableName(Integer id, DataDoFieldTypeEnum type);
}
