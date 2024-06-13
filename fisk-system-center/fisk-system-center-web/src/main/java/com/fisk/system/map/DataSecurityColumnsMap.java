package com.fisk.system.map;

import com.fisk.system.dto.datasecurity.DataSecurityColumnsDTO;
import com.fisk.system.entity.DataSecurityColumnsPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataSecurityColumnsMap {

    DataSecurityColumnsMap INSTANCES = Mappers.getMapper(DataSecurityColumnsMap.class);

    @Mapping(target = "readableFieldIds", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    DataSecurityColumnsPO dtoToPo(DataSecurityColumnsDTO dto);

    List<DataSecurityColumnsPO> dtosToPos(List<DataSecurityColumnsDTO> dtos);

    @Mapping(target = "readableFieldMap", ignore = true)
    @Mapping(target = "appName", ignore = true)
    @Mapping(target = "readableFieldIds", ignore = true)
    @Mapping(target = "tblName", ignore = true)
    @Mapping(target = "tblDisName", ignore = true)
    DataSecurityColumnsDTO poToDto(DataSecurityColumnsPO po);

    List<DataSecurityColumnsDTO> posToDtos(List<DataSecurityColumnsPO> pos);

}
