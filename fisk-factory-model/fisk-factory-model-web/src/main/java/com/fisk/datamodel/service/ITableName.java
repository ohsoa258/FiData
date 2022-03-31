package com.fisk.datamodel.service;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.chartvisual.dto.chartVisual.IndicatorDTO;
import com.fisk.chartvisual.dto.chartVisual.IndicatorFeignDTO;
import com.fisk.chartvisual.dto.chartVisual.TableDataDTO;
import com.fisk.chartvisual.enums.DataDoFieldTypeEnum;

import java.util.List;

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
    ResultEntity<TableDataDTO> getTableName(Integer id, DataDoFieldTypeEnum type, String field);

    /**
     * 查询聚合条件
     * @param id id
     * @return count()
     */
    ResultEntity<String> getAggregation(Integer id);

    /**
     * 查询指标字段的关系
     * @param dto
     * @return
     */
    ResultEntity<List<IndicatorDTO>> getIndicatorsLogic(IndicatorFeignDTO dto);
}
