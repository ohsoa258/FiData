package com.fisk.datamanagement.map;


import com.fisk.datamanagement.dto.metasynctime.MetaSyncDTO;
import com.fisk.datamanagement.entity.MetaSyncTimePO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MetaSyncTimeMap {


    MetaSyncTimeMap INSTANCES = Mappers.getMapper(MetaSyncTimeMap.class);

    MetaSyncDTO poToDto(MetaSyncTimePO po);

    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    MetaSyncTimePO dtoToPo(MetaSyncDTO dto);

    List<MetaSyncDTO> posToDtos(List<MetaSyncTimePO> pos);

    List<MetaSyncTimePO> dtosToPos(List<MetaSyncDTO> pos);


}
