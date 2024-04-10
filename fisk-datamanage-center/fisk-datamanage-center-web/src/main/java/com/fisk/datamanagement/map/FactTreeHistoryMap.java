package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.classification.FacttreelistHistoryDTO;
import com.fisk.datamanagement.entity.FactTreePOs;
import com.fisk.datamanagement.entity.FacttreelistHistoryPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @Author: wangjian
 * @Date: 2024-03-29
 * @Description:
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FactTreeHistoryMap {
    FactTreeHistoryMap INSTANCES = Mappers.getMapper(FactTreeHistoryMap.class);

    FacttreelistHistoryDTO poToDto(FacttreelistHistoryPO po);
}
