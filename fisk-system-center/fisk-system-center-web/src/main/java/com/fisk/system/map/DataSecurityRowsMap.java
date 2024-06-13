package com.fisk.system.map;

import com.fisk.system.dto.datasecurity.DataSecurityRowsDTO;
import com.fisk.system.entity.DataSecurityRowsPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataSecurityRowsMap {

    DataSecurityRowsMap INSTANCES = Mappers.getMapper(DataSecurityRowsMap.class);

    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    DataSecurityRowsPO dtoToPo(DataSecurityRowsDTO dto);

    List<DataSecurityRowsPO> dtosToPos(List<DataSecurityRowsDTO> dtos);

    @Mapping(target = "appName", ignore = true)
    @Mapping(target = "tblName", ignore = true)
    @Mapping(target = "tblDisName", ignore = true)
    DataSecurityRowsDTO poToDto(DataSecurityRowsPO po);

    List<DataSecurityRowsDTO> posToDtos(List<DataSecurityRowsPO> pos);

}
