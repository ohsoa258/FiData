package com.fisk.system.map;

import com.fisk.system.dto.SystemVersionDTO;
import com.fisk.system.entity.SystemVersionPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SystemVersionMap {
    SystemVersionMap INSTANCES = Mappers.getMapper(SystemVersionMap.class);

    /**
     * po => dto
     *
     * @param po
     * @return
     */
    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "version", source = "version"),
            @Mapping(target = "publishTime",ignore = true)
    })
    SystemVersionDTO poToDto(SystemVersionPO po);
}
