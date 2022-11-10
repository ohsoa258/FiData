package com.fisk.datamodel.mapper.dimension;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeListDTO;
import com.fisk.datamodel.entity.dimension.DimensionAttributePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper
public interface DimensionAttributeMapper extends FKBaseMapper<DimensionAttributePO> {
    /**
     * 获取维度字段表数据
     * @param dimensionId
     * @return
     */
    List<DimensionAttributeListDTO> getDimensionAttributeList(@Param("dimensionId") int dimensionId);
}
