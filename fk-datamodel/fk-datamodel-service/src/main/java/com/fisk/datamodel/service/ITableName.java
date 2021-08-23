package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.dto.table.TableData;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;

/**
 * @author Lock
 */
public interface ITableName {
    /**
     * 查询表名
     * @param id id
     * @param type type
     * @param field field
     * @return tableName
     */
    ResultEntity<TableData> getTableName(Integer id, DataDoFieldTypeEnum type,String field);
}
