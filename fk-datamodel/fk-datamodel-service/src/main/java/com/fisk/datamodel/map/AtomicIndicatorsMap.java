package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.atomicIndicators.AtomicIndicatorsDTO;
import com.fisk.datamodel.entity.AtomicIndicatorsPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AtomicIndicatorsMap {
    AtomicIndicatorsMap INSTANCES = Mappers.getMapper(AtomicIndicatorsMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    AtomicIndicatorsPO dtoToPo(AtomicIndicatorsDTO dto);


}
