package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.BusinessAreaDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author Lock
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessAreaMap {

    BusinessAreaMap INSTANCES = Mappers.getMapper(BusinessAreaMap.class);

    /**
     * dto => po
     *
     * @param po source
     * @return target
     */
    BusinessAreaPO dtoToPo(BusinessAreaDTO po);

    /**
     * po => dto
     *
     * @param po po
     * @return dto
     */
    BusinessAreaDTO poToDto(BusinessAreaPO po);
}
