package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.ProjectDimensionAssociationDTO;
import com.fisk.datamodel.dto.ProjectDimensionDTO;
import com.fisk.datamodel.dto.ProjectDimensionSourceDTO;
import com.fisk.datamodel.dto.ProjectInfoDropDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IProjectDimension {

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
    ResultEnum addDimension(ProjectDimensionDTO dto);

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
    ResultEnum updateDimension(ProjectDimensionDTO dto);

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
     * 根据数据域id获取项目列表
     * @param dataId
     * @return
     */
    List<ProjectInfoDropDTO> getProjectDropList(int dataId);

}
