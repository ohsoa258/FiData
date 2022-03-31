package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.syncmode.SyncTableBusinessDTO;
import com.fisk.datamodel.entity.TableBusinessPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableBusinessMap {
    TableBusinessMap INSTANCES = Mappers.getMapper(TableBusinessMap.class);

    /**
     * po==>Dto
     * @param po
     * @return
     */
    SyncTableBusinessDTO poToDto(TableBusinessPO po);

    /**
     * dto==>Po
     * @param dto
     * @return
     */
    TableBusinessPO dtoToPo(SyncTableBusinessDTO dto);

}
