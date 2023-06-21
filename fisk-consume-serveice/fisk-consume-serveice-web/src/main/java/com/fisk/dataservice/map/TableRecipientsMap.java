package com.fisk.dataservice.map;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableRecipientsMap {
    TableRecipientsMap INSTANCES = Mappers.getMapper(TableRecipientsMap.class);
}
