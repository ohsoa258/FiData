package com.fisk.datamodel.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datamodel.dto.DimensionAttributeListDTO;
import com.fisk.datamodel.entity.DimensionAttributePO;
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
