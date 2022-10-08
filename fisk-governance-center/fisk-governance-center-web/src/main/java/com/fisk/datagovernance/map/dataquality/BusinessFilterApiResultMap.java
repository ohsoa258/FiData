package com.fisk.datagovernance.map.dataquality;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗 API清洗结果
 * @date 2022/10/8 16:56
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessFilterApiResultMap {
    BusinessFilterApiResultMap INSTANCES = Mappers.getMapper(BusinessFilterApiResultMap.class);
}
