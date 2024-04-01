package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.classification.FacttreeListDTO;
import com.fisk.datamanagement.entity.FactTreePOs;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @Author: wangjian
 * @Date: 2024-03-29
 * @Description:
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FactTreeMap {
    FactTreeMap INSTANCES = Mappers.getMapper(FactTreeMap.class);

    FacttreeListDTO poToDto(FactTreePOs po);
}
