package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleDTO;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleEditDTO;
import com.fisk.datagovernance.entity.dataquality.LifecyclePO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LifecycleMap {

    LifecycleMap INSTANCES = Mappers.getMapper(LifecycleMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "tableState.value", target = "tableState"),
            @Mapping(source = "ruleState.value", target = "ruleState")
    })
    LifecyclePO dtoToPo(LifecycleDTO dto);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "tableState.value", target = "tableState"),
            @Mapping(source = "ruleState.value", target = "ruleState")
    })
    LifecyclePO dtoToPo_Edit(LifecycleEditDTO dto);
}