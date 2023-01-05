package com.fisk.system.map;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 * @version 1.0
 * @description LicenseMap
 * @date 2022/11/10 15:48
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LicenseMap {

    LicenseMap INSTANCES = Mappers.getMapper(LicenseMap.class);

}
