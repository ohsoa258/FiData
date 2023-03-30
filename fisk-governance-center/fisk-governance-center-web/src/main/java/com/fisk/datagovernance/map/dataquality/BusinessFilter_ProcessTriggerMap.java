package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.businessfilter.process.BusinessFilter_ProcessTriggerDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessTriggerPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessTriggerVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessFilter_ProcessTriggerMap {
    BusinessFilter_ProcessTriggerMap INSTANCES = Mappers.getMapper(BusinessFilter_ProcessTriggerMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    List<BusinessFilter_ProcessTriggerPO> dtoListToPoList(List<BusinessFilter_ProcessTriggerDTO> dto);

    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    List<BusinessFilter_ProcessTriggerVO> poListToVoList(List<BusinessFilter_ProcessTriggerPO> po);
}
