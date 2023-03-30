package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessAssemblyPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessAssemblyVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessFilter_ProcessAssemblyMap {
    BusinessFilter_ProcessAssemblyMap INSTANCES = Mappers.getMapper(BusinessFilter_ProcessAssemblyMap.class);

    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    List<BusinessFilter_ProcessAssemblyVO> poListToVoList(List<BusinessFilter_ProcessAssemblyPO> po);
}
