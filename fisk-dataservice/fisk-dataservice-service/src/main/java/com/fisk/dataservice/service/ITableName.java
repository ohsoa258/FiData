package com.fisk.dataservice.service;

import com.fisk.common.response.ResultEntity;
import com.fisk.dataservice.dto.InputParameterDTO;
import com.fisk.dataservice.dto.OutParameterDTO;
import com.fisk.dataservice.dto.TableDataDTO;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;

import java.sql.ResultSet;
import java.util.List;

/**
 * @author Lock
 */
public interface ITableName {
    /**
     * 查询表名
     * @param id
     * @param type
     * @param field
     * @param isDimension
     * @return
     */
    ResultEntity<TableDataDTO> getTableName(Integer id, DataDoFieldTypeEnum type, String field,int isDimension);

    /**
     * 查询聚合条件
     * @param id id
     * @return count()
     */
    ResultEntity<String> getAggregation(Integer id);

    /**
     * 查询关联维度的表名
     * @param relationId
     * @return
     */
    ResultEntity<String> getDimensionName(Integer relationId);

    /**
     * 查询是否存在关联关系
     * @param dto
     * @return
     */
    List<OutParameterDTO> getRelationShip(InputParameterDTO dto);
}
