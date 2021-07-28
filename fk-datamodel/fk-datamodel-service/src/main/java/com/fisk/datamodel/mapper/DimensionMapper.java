package com.fisk.datamodel.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datamodel.dto.DimensionAssociationDTO;
import com.fisk.datamodel.entity.DimensionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author JianWenYang
 */
@Mapper
public interface DimensionMapper extends FKBaseMapper<DimensionPO> {
    /**
     * 获取维度表关联业务域、数据域
     * @param id
     * @return
     */
    DimensionAssociationDTO getDimensionAssociation(@Param("id") int id);

    /**
     * 获取维度表详情
     * @param id
     * @return
     */
    DimensionAssociationDTO getDimension(@Param("id") int id);
}
