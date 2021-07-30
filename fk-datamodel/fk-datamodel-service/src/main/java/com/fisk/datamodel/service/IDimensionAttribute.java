package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.dimensionattribute.*;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IDimensionAttribute {

    /**
     * 获取接入表以及表字段
     * @return 查询数据
     */
    List<DimensionMetaDTO> getProjectDimensionMeta();

    /**
     * 获取维度表以及字段
     * @return 查询数据
     */
    List<DimensionAttributeAssociationDTO> getProjectDimensionTable();

    /**
     * 添加维度字段
     * @param dimensionId
     * @param dto
     * @return 添加结果
     */
    ResultEnum addDimensionAttribute(int dimensionId,List<DimensionAttributeDTO> dto);

    /**
     * 批量删除维度字段
     * @param ids
     * @return 删除结果
     */
    ResultEnum deleteDimensionAttribute(List<Integer> ids);

    /**
     * 获取维度字段表数据
     * @param dimensionId
     * @return
     */
    List<DimensionAttributeListDTO> getDimensionAttributeList(int dimensionId);

    /**
     * 维度字段数据更改
     * @param dto
     * @return
     */
    ResultEnum updateDimensionAttribute(DimensionAttributeUpdateDTO dto);

}
