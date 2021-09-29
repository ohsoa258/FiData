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
     * 获取维度表以及字段
     * @return 查询数据
     */
    List<DimensionMetaDTO> getProjectDimensionTable();

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
    ResultEntity<Integer> deleteDimensionAttribute(List<Integer> ids);

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

    /**
     * 根据维度id获取维度字段详情
     * @param id
     * @return
     */
    ModelMetaDataDTO getDimensionMetaData(int id);

    /**
     * 根据业务域发布
     * @param businessAreaId
     * @return
     */
    List<ModelMetaDataDTO> getDimensionMetaDataList(int businessAreaId);

    /**
     * 根据维度id获取维度下相关字段
     * @param id
     * @return
     */
    List<DimensionAttributeAssociationDTO> getDimensionAttributeData(int id);

    /**
     * 根据维度id获取维度字段所有来源id
     * @param id
     * @return
     */
    List<FieldNameDTO> getDimensionAttributeSourceId(int id);

}
