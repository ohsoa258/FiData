package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.standards.StandardsMenuDTO;
import com.fisk.datamanagement.entity.StandardsMenuPO;
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
public interface StandardsMenuMap {
    StandardsMenuMap INSTANCES = Mappers.getMapper(StandardsMenuMap.class);

    StandardsMenuPO dtoToPo(StandardsMenuDTO dto);

    List<StandardsMenuDTO> posToDtos(List<StandardsMenuPO> pos);
}
