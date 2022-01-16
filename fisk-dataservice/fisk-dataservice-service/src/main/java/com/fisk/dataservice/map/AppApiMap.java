package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.app.AppApiSubDTO;
import com.fisk.dataservice.entity.AppApiPO;
import org.mapstruct.*;
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
    AppApiPO dtoToPo(AppApiSubDTO dto);
}
