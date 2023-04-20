package com.fisk.mdm.map;

import com.fisk.mdm.dto.access.SyncModeDTO;
import com.fisk.mdm.entity.SyncModePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author wangjian
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
