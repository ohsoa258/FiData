package com.fisk.dataservice.map;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableServiceMap {

    TableServiceMap INSTANCES = Mappers.getMapper(TableServiceMap.class);

}
