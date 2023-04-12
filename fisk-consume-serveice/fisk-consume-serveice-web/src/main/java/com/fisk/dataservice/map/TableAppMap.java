package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.tableservice.TableAppDTO;
import com.fisk.dataservice.entity.TableAppPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableAppMap {
    TableAppMap INSTANCES = Mappers.getMapper(TableAppMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    TableAppPO dtoToPo(TableAppDTO dto);
}
