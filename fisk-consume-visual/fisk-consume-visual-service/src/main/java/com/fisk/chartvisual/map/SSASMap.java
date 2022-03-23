package com.fisk.chartvisual.map;

import com.fisk.chartvisual.entity.DimensionPO;
import com.fisk.chartvisual.entity.HierarchyPO;
import com.fisk.chartvisual.entity.MeasurePO;
import com.fisk.chartvisual.vo.DataDomainVO;
import com.fisk.chartvisual.vo.DimensionVO;
import com.fisk.chartvisual.vo.HierarchyVO;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * SSAS映射
 * @author JinXingWang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SSASMap {
    SSASMap INSTANCES = Mappers.getMapper(SSASMap.class);


    /**
     * 层级PO转VO
     * @param po 层级
     * @return 层级vo
     */
    List<DataDomainVO> hierarchiesPoToVo(List<HierarchyPO> po);


    /**
     * 度量PO转 层级 VO
     * @param po 度量
     * @return VO 层级vo
     */
    List<DataDomainVO> measurePoToVo(List<MeasurePO> po);

    /**
     * 维度 PO 转VO
     * @param po 维度
     * @return 维度vo
     */
    List<DimensionVO> dimensionPoToVo(List<DimensionPO> po);

}
