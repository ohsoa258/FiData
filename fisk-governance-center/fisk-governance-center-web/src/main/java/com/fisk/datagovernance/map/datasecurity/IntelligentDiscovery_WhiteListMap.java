package com.fisk.datagovernance.map.datasecurity;

import com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery.IntelligentDiscovery_WhiteListDTO;
import com.fisk.datagovernance.entity.datasecurity.IntelligentDiscovery_WhiteListPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IntelligentDiscovery_WhiteListMap {
    IntelligentDiscovery_WhiteListMap INSTANCES = Mappers.getMapper(IntelligentDiscovery_WhiteListMap.class);
    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    IntelligentDiscovery_WhiteListPO dtoToPo(IntelligentDiscovery_WhiteListDTO dto);
}