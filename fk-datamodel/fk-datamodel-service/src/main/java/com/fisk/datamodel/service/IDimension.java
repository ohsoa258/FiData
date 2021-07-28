package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.*;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IDimension {

    /**
     * 获取维度相关数据域列表以及数据域下维度表
     * @return
     */
    List<ProjectDimensionSourceDTO> getDimensionList();

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
    ProjectDimensionAssociationDTO getDimension(int id);

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
     * 获取维度表关联业务域以及数据域
     * @param id
     * @return
     */
    ProjectDimensionAssociationDTO getRegionDetail(int id);

    /**
     * 获取维度表列表
     * @param dto
     * @return
     */
    IPage<DimensionDTO> getDimension(QueryDTO dto);

}
