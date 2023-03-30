package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.businessfilter.process.BusinessFilter_ProcessFieldAssignDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessFieldAssignPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessFieldAssignVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessFilter_ProcessFieldAssignMap {
    BusinessFilter_ProcessFieldAssignMap INSTANCES = Mappers.getMapper(BusinessFilter_ProcessFieldAssignMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    List<BusinessFilter_ProcessFieldAssignPO> dtoListToPoList(List<BusinessFilter_ProcessFieldAssignDTO> dto);

    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    List<BusinessFilter_ProcessFieldAssignVO> poListToVoList(List<BusinessFilter_ProcessFieldAssignPO> po);
}
