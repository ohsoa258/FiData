package com.fisk.datamanagement.map;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MetadataClassificationMap {

    MetadataClassificationMap INSTANCES = Mappers.getMapper(MetadataClassificationMap.class);

}
