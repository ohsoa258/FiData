package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.notice.ComponentNotificationDTO;
import com.fisk.datagovernance.entity.dataquality.ComponentNotificationPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ComponentNotificationMap {

    ComponentNotificationMap INSTANCES = Mappers.getMapper(ComponentNotificationMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    List<ComponentNotificationPO> listDtoToPo(List<ComponentNotificationDTO> dto);
}