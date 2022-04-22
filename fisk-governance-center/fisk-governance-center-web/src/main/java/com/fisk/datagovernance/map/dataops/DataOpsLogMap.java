package com.fisk.datagovernance.map.dataops;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataOpsLogMap {

    DataOpsLogMap INSTANCES = Mappers.getMapper(DataOpsLogMap.class);

}
