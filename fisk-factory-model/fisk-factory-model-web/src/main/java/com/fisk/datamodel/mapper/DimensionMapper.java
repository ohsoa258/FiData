package com.fisk.datamodel.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamodel.dto.dimension.DimensionTabDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAssociationDTO;
import com.fisk.datamodel.entity.DimensionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    /**
     * 获取维度id集合
     * @return
     */
    @Select("SELECT id,dimension_cn_name FROM tb_dimension where del_flag=1 ORDER BY id DESC;")
    List<DimensionTabDTO> getDimensionTabList();
}
