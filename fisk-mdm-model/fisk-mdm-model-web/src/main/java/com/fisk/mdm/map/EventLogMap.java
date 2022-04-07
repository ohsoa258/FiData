package com.fisk.mdm.map;

import com.fisk.mdm.dto.eventlog.EventLogDTO;
import com.fisk.mdm.entity.EventLogPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author WangYan
 * @date 2022/4/6 16:40
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventLogMap {

    EventLogMap INSTANCES = Mappers.getMapper(EventLogMap.class);

    /**
     * dto => po
     * @param dto
     * @return
     */
    EventLogPO dtoToPo(EventLogDTO dto);
}
