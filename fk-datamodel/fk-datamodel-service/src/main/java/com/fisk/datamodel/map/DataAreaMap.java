package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.DataAreaDTO;
import com.fisk.datamodel.entity.DataAreaPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author Lock
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataAreaMap {

    DataAreaMap INSTANCES = Mappers.getMapper(DataAreaMap.class);

    /**
     * dto => po
     *
     * @param po source
     * @return target
     */
    DataAreaPO dtoToPo(DataAreaDTO po);

    /**
     * po => dto
     *
     * @param po po
     * @return dto
     */
    DataAreaDTO poToDto(DataAreaPO po);
}
