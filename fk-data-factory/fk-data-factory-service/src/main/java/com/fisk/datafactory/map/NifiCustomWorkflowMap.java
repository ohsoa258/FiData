package com.fisk.datafactory.map;

import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.datafactory.entity.NifiCustomWorkflowPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author Lock
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NifiCustomWorkflowMap {

    NifiCustomWorkflowMap INSTANCES = Mappers.getMapper(NifiCustomWorkflowMap.class);

    /**
     * dto => po
     * @param dto source
     * @return target
     */
    NifiCustomWorkflowPO dtoToPo(NifiCustomWorkflowDTO dto);

    /**
     * po => dto
     * @param po source
     * @return target
     */
    NifiCustomWorkflowDTO poToDto(NifiCustomWorkflowPO po);
}
