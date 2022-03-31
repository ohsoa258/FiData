package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.syncmode.SyncModeDTO;
import com.fisk.datamodel.entity.SyncModePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SyncModeMap {
    SyncModeMap INSTANCES = Mappers.getMapper(SyncModeMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    SyncModePO dtoToPo(SyncModeDTO dto);

    /**
     * po==>dto
     * @param po
     * @return
     */
    SyncModeDTO poToDto(SyncModePO po);

}
