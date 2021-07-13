package com.fisk.system.map;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleServiceAssignmentMap {
    RoleServiceAssignmentMap INSTANCES = Mappers.getMapper(RoleServiceAssignmentMap.class);
}
