package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.FieldNameDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.*;
import com.squareup.okhttp.internal.Internal;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IDimensionAttribute {

    /**
     * 添加维度字段
     * @param dto
     * @return 添加结果
     */
    ResultEnum addOrUpdateDimensionAttribute(DimensionAttributeAddDTO dto);

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
    DimensionAttributeListDTO getDimensionAttributeList(int dimensionId);

    /**
     * 维度字段数据更改
     * @param dto
     * @return
     */
    ResultEnum updateDimensionAttribute(DimensionAttributeUpdateDTO dto);

    /**
     * 根据维度id获取维度字段详情
     * @param id
     * @return
     */
    ModelMetaDataDTO getDimensionMetaData(int id);

    /**
     * 根据业务域发布
     * @param factIds
     * @return
     */
    List<ModelMetaDataDTO> getDimensionMetaDataList(List<Integer> factIds);

    /**
     * 根据维度id获取维度下相关字段
     * @param id
     * @return
     */
    List<DimensionAttributeAssociationDTO> getDimensionAttributeData(int id);

    /**
     * 根据维度字段id,获取字段详情信息
     * @param id
     * @return
     */
    DimensionAttributeUpdateDTO getDimensionAttribute(int id);

}
