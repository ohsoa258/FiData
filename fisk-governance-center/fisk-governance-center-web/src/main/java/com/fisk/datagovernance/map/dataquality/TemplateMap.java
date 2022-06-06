package com.fisk.datagovernance.map.dataquality;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 * @version 1.0
 * @description 模板map
 * @date 2022/3/23 12:30
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TemplateMap {
    TemplateMap INSTANCES = Mappers.getMapper(TemplateMap.class);
}
