package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.app.AppApiSubDTO;
import com.fisk.dataservice.entity.AppServiceConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AppApiMap {

    AppApiMap INSTANCES = Mappers.getMapper(AppApiMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    AppServiceConfigPO dtoToPo(AppApiSubDTO dto);
}
