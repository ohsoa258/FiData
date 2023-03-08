package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.lineagemaprelation.LineageMapRelationDTO;
import com.fisk.datamanagement.entity.LineageMapRelationPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LineageMapRelationMap {

    LineageMapRelationMap INSTANCES = Mappers.getMapper(LineageMapRelationMap.class);

    /**
     * dtoList==>PoList
     *
     * @param dtoList
     * @return
     */
    List<LineageMapRelationPO> dtoListToPoList(List<LineageMapRelationDTO> dtoList);

}
