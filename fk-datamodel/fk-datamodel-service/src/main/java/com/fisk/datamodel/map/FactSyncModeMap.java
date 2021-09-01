package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.factsyncmode.FactSyncModeDTO;
import com.fisk.datamodel.dto.factsyncmode.FactSyncModePushDTO;
import com.fisk.datamodel.entity.FactSyncModePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FactSyncModeMap {
    FactSyncModeMap INSTANCES = Mappers.getMapper(FactSyncModeMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    FactSyncModePO dtoToPo(FactSyncModeDTO dto);

    /**
     * po==>dto
     * @param po
     * @return
     */
    FactSyncModeDTO poToDto(FactSyncModePO po);

    /**
     * po==>pushDto
     * @param po
     * @return
     */
    FactSyncModePushDTO poToPushDto(FactSyncModePO po);

}
