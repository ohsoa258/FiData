package com.fisk.dataservice.service;

import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.dto.table.TableDataDTO;
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
    ResultEntity<TableDataDTO> getTableName(Integer id, DataDoFieldTypeEnum type, String field,int isDimension);

    /**
     * 查询聚合条件
     * @param id id
     * @return count()
     */
    ResultEntity<String> getAggregation(Integer id);
}
