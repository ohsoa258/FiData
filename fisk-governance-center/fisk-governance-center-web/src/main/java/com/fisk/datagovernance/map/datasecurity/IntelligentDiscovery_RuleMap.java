package com.fisk.datagovernance.map.datasecurity;

import com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery.IntelligentDiscovery_RuleDTO;
import com.fisk.datagovernance.entity.datasecurity.IntelligentDiscovery_RulePO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;


@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IntelligentDiscovery_RuleMap {
    IntelligentDiscovery_RuleMap INSTANCES = Mappers.getMapper(IntelligentDiscovery_RuleMap.class);
    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    IntelligentDiscovery_RulePO dtoToPo(IntelligentDiscovery_RuleDTO dto);
}