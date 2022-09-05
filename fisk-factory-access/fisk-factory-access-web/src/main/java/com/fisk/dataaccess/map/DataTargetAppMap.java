package com.fisk.dataaccess.map;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataTargetAppMap {

    DataTargetAppMap INSTANCES = Mappers.getMapper(DataTargetAppMap.class);

}
