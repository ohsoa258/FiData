package com.fisk.dataservice.map;

import com.fisk.dataservice.entity.DimensionPO;
import com.fisk.dataservice.entity.HierarchyPO;
import com.fisk.dataservice.entity.MeasurePO;
import com.fisk.dataservice.vo.datasource.DimensionVO;
import com.fisk.dataservice.vo.datasource.HierarchyVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * SSAS映射
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SSASMap {
    SSASMap INSTANCES = Mappers.getMapper(SSASMap.class);


    /**
     * 层级PO转VO
     * @param po 层级
     * @return 层级vo
     */
    List<HierarchyVO> hierarchiesPoToVo(List<HierarchyPO> po);


    /**
     * 度量PO转 层级 VO
     * @param po 度量
     * @return VO 层级vo
     */
    List<HierarchyVO> measurePoToVo(List<MeasurePO> po);

    /**
     * 维度 PO 转VO
     * @param po 维度
     * @return 维度vo
     */
    List<DimensionVO> dimensionPoToVo(List<DimensionPO> po);

}
