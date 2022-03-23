package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsAddDTO;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsDTO;
import com.fisk.datamodel.entity.DerivedIndicatorsPO;
import com.fisk.datamodel.entity.IndicatorsPO;
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
    IndicatorsPO dtoToPo(DerivedIndicatorsDTO dto);

    /**
     * po==>dto
     * @param po
     * @return
     */
    DerivedIndicatorsDTO poToDto(IndicatorsPO po);

    /**
     * po==>po
     * @param dto
     * @return
     */
    DerivedIndicatorsAddDTO poToPo(DerivedIndicatorsDTO dto);

}
