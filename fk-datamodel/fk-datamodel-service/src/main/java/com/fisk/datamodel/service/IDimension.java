package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.*;
import com.fisk.datamodel.dto.dimension.DimensionDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAssociationDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionSourceDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IDimension {

    /**
     * 获取维度相关数据域列表以及数据域下维度表
     * @return
     */
    List<DimensionSourceDTO> getDimensionList();

    /**
     * 添加维度表
     * @param dto
     * @return
     */
    ResultEnum addDimension(DimensionDTO dto);

    /**
     * 获取维度表详情
     * @param id
     * @return
     */
    DimensionAssociationDTO getDimension(int id);

    /**
     * 修改维度表
     * @param dto
     * @return
     */
    ResultEnum updateDimension(DimensionDTO dto);

    /**
     * 删除维度表
     * @param id
     * @return
     */
    ResultEnum deleteDimension(int id);

    /**
     * 获取维度表列表
     * @param dto
     * @return
     */
    IPage<DimensionDTO> getDimensionList(QueryDTO dto);

    /**
     * 发布维度
     * @param id
     * @return
     */
    ResultEnum dimensionPublish(int id);

    /**
     * 维度是否发布成功
     * @param id
     * @param isSuccess
     */
    void updatePublishStatus(int id,boolean isSuccess);

}
