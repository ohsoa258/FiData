package com.fisk.datagovernance.map.datasecurity;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IntelligentDiscovery_LogsMap {
    IntelligentDiscovery_LogsMap INSTANCES = Mappers.getMapper(IntelligentDiscovery_LogsMap.class);
}
