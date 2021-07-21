package com.fisk.datamodel.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datamodel.dto.ProjectDimensionAssociationDTO;
import com.fisk.datamodel.entity.ProjectDimensionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author JianWenYang
 */
@Mapper
public interface ProjectDimensionMapper extends FKBaseMapper<ProjectDimensionPO> {
    /**
     * 获取维度表关联业务域、数据域
     * @param id
     * @return
     */
    ProjectDimensionAssociationDTO getDimensionAssociation(@Param("id") int id);

    /**
     * 获取维度表详情
     * @param id
     * @return
     */
    ProjectDimensionAssociationDTO getDimension(@Param("id") int id);
}
