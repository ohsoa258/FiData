package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.derivedindicatorslimited.DerivedIndicatorsLimitedDTO;
import com.fisk.datamodel.entity.DerivedIndicatorsLimitedPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DerivedIndicatorsLimitedMap {
    DerivedIndicatorsLimitedMap INSTANCES = Mappers.getMapper(DerivedIndicatorsLimitedMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    DerivedIndicatorsLimitedPO dtoToPo(DerivedIndicatorsLimitedDTO dto);

    /**
     * po==>dto
     * @param po
     * @return
     */
    DerivedIndicatorsLimitedDTO poToDto(DerivedIndicatorsLimitedPO po);

}
