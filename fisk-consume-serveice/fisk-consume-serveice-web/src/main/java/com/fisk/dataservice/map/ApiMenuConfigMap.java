package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.api.ApiMenuDTO;
import com.fisk.dataservice.dto.api.FilterConditionConfigDTO;
import com.fisk.dataservice.entity.ApiMenuConfigPO;
import com.fisk.dataservice.entity.FilterConditionConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-01-26
 * @Description:
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApiMenuConfigMap {
    ApiMenuConfigMap INSTANCES = Mappers.getMapper(ApiMenuConfigMap.class);

    ApiMenuConfigPO dtoToPo(ApiMenuDTO dto);
}
