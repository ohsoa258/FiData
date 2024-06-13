package com.fisk.system.map;

import com.fisk.system.dto.datasecurity.DataSecurityTablesDTO;
import com.fisk.system.entity.DataSecurityTablesPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataSecurityMap {

    DataSecurityMap INSTANCES = Mappers.getMapper(DataSecurityMap.class);

    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    DataSecurityTablesPO dtoToPo(DataSecurityTablesDTO dto);

    List<DataSecurityTablesPO> dtosToPos(List<DataSecurityTablesDTO> dtos);

    @Mapping(target = "appName", ignore = true)
    @Mapping(target = "tblName", ignore = true)
    @Mapping(target = "tblDisName", ignore = true)
    DataSecurityTablesDTO poToDto(DataSecurityTablesPO po);

    List<DataSecurityTablesDTO> posToDtos(List<DataSecurityTablesPO> pos);

}
