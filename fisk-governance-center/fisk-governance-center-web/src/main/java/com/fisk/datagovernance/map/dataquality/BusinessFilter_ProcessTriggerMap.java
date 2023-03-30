package com.fisk.datagovernance.map.dataquality;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessFilter_ProcessTriggerMap {
    BusinessFilter_ProcessTriggerMap INSTANCES = Mappers.getMapper(BusinessFilter_ProcessTriggerMap.class);
}
