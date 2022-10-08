package com.fisk.datagovernance.map.dataquality;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗 API清洗
 * @date 2022/10/8 16:55
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessFilterApiMap {
    BusinessFilterApiMap INSTANCES = Mappers.getMapper(BusinessFilterApiMap.class);
}
