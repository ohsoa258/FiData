package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.standards.StandardsDTO;
import com.fisk.datamanagement.entity.StandardsPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-11-20
 * @Description:
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StandardsMap {
    StandardsMap INSTANCES = Mappers.getMapper(StandardsMap.class);

    StandardsPO dtoToPo(StandardsDTO dto);

    StandardsDTO poToDTO(StandardsPO dto);

    List<StandardsDTO> poListToDTOList(List<StandardsPO> dto);
}
