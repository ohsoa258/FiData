package com.fisk.dataservice.map;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 * @version 1.0
 * @description 应用白名单配置
 * @date 2023/6/7 9:40
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AppWhiteListMap {
    AppWhiteListMap INSTANCES = Mappers.getMapper(AppWhiteListMap.class);
}
