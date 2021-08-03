package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.factattribute.FactAttributeDTO;
import com.fisk.datamodel.entity.FactAttributePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FactAttributeMap {
    FactAttributeMap INSTANCES = Mappers.getMapper(FactAttributeMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    FactAttributePO dtoToPo(FactAttributeDTO dto);

}
