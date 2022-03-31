package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.label.LabelDTO;
import com.fisk.datamanagement.entity.LabelPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LabelMap {
    LabelMap INSTANCES = Mappers.getMapper(LabelMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    LabelPO dtoToPo(LabelDTO dto);

    /**
     * po==>dto
     * @param po
     * @return
     */
    LabelDTO poToDto(LabelPO po);

}
