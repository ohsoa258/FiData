package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.standards.StandardsDTO;
import com.fisk.datamanagement.dto.standards.StandardsExportDTO;
import com.fisk.datamanagement.entity.StandardsPO;
import com.fisk.datamanagement.utils.TypeConversionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-11-20
 * @Description:
 */
@Mapper(uses = { TypeConversionUtils.class } ,nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StandardsMap {
    StandardsMap INSTANCES = Mappers.getMapper(StandardsMap.class);

    @Mapping(source = "valueRangeType.value",target = "valueRangeType")
    StandardsPO dtoToPo(StandardsDTO dto);

    @Mapping(source = "valueRangeType",target = "valueRangeType")
    StandardsDTO poToDTO(StandardsPO dto);

    List<StandardsDTO> poListToDTOList(List<StandardsPO> dto);

    List<StandardsExportDTO> poListToExportDTOList(List<StandardsPO> dto);
}
