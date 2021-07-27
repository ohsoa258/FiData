package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.ProjectDimensionAttributeDTO;
import com.fisk.datamodel.dto.ProjectDimensionMetaDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IProjectDimensionAttribute {

    /**
     * 获取接入表以及表字段
     * @return 查询数据
     */
    List<ProjectDimensionMetaDTO> getProjectDimensionMeta();

    /**
     * 获取维度表以及字段
     * @return 查询数据
     */
    List<ProjectDimensionMetaDTO> getProjectDimensionTable();

    /**
     * 添加维度字段
     * @param dto
     * @return
     */
    ResultEnum addProjectDimensionAttribute(List<ProjectDimensionAttributeDTO> dto);



}
