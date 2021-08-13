package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorDropListDTO;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorsDTO;
import com.fisk.datamodel.entity.AtomicIndicatorsPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

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

    /**
     * po==>dtoList
     * @param PO
     * @return
     */
    List<AtomicIndicatorDropListDTO> poToDtoList(List<AtomicIndicatorsPO> PO);


}
