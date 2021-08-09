package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsDTO;
import com.fisk.datamodel.entity.DerivedIndicatorsPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DerivedIndicatorsMap {
    DerivedIndicatorsMap INSTANCES = Mappers.getMapper(DerivedIndicatorsMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    DerivedIndicatorsPO dtoToPo(DerivedIndicatorsDTO dto);



}
