package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.standards.StandardsBeCitedDTO;
import com.fisk.datamanagement.entity.StandardsBeCitedPO;
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
public interface StandardsBeCitedMap {

    StandardsBeCitedMap INSTANCES = Mappers.getMapper(StandardsBeCitedMap.class);

    StandardsBeCitedPO dtoToPo(StandardsBeCitedDTO dto);

    StandardsBeCitedDTO poToDTO(StandardsBeCitedPO dto);

    List<StandardsBeCitedDTO> poListToDTOList(List<StandardsBeCitedPO> dto);

    List<StandardsBeCitedPO> dtoListToPoList(List<StandardsBeCitedDTO> dto);
}
