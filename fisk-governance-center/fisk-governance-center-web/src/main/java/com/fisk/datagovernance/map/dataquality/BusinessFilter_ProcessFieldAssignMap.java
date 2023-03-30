package com.fisk.datagovernance.map.dataquality;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessFilter_ProcessFieldAssignMap {
    BusinessFilter_ProcessFieldAssignMap INSTANCES = Mappers.getMapper(BusinessFilter_ProcessFieldAssignMap.class);
}
