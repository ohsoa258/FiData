package com.fisk.datamodel.map;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FactAttributeMap {
    FactAttributeMap INSTANCES = Mappers.getMapper(FactAttributeMap.class);



}
