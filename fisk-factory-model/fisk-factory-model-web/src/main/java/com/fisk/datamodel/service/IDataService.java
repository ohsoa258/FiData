package com.fisk.datamodel.service;

import com.fisk.chartvisual.dto.IsDimensionDTO;
import com.fisk.datamodel.dto.atomicindicator.DimensionTimePeriodDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IDataService {

    /**
     * 判断维度与事实、维度与维度是否存在关联
     * @param dto
     * @return
     */
    boolean isExistAssociate(IsDimensionDTO dto);

    /**
     * 根据派生指标id获取该业务域下日期维度
     * @param indicatorsId
     * @return
     */
    DimensionTimePeriodDTO getDimensionDate(int indicatorsId);

    /**
     * 根据时间维度表名获取表下所有字段
     * @param tableName
     * @return
     */
    List<String> getDimensionFieldNameList(String tableName);
}
