package com.fisk.system.map;

import com.fisk.system.dto.ServiceRegistryDTO;
import com.fisk.system.entity.ServiceRegistryPO;
import org.mapstruct.Mapper;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ServiceRegistryMap {
    ServiceRegistryMap INSTANCES = Mappers.getMapper(ServiceRegistryMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    ServiceRegistryPO dtoToPo(ServiceRegistryDTO dto);

    /**
     po => dto
     */
    ServiceRegistryDTO poToDto(ServiceRegistryPO po);

}
