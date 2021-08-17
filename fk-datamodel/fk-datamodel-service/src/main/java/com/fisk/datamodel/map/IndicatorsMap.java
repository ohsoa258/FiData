package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorsDTO;
import com.fisk.datamodel.entity.IndicatorsPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IndicatorsMap {
    IndicatorsMap INSTANCES = Mappers.getMapper(IndicatorsMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    IndicatorsPO dtoToPo(AtomicIndicatorsDTO dto);

}
