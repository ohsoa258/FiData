package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.tablesyncmode.ApiTableSyncModeDTO;
import com.fisk.dataservice.entity.TableSyncModePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableSyncModeMap {

    TableSyncModeMap INSTANCES = Mappers.getMapper(TableSyncModeMap.class);

    /**
     * dto==>Po
     *
     * @param dto
     * @return
     */
    TableSyncModePO dtoToPo(ApiTableSyncModeDTO dto);


}
