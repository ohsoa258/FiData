package com.fisk.mdm.map;

import com.fisk.mdm.dto.stgbatch.StgBatchDTO;
import com.fisk.mdm.entity.StgBatchPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StgBatchMap {

    StgBatchMap INSTANCES = Mappers.getMapper(StgBatchMap.class);

    /**
     * dto=>Po
     * @param dto
     * @return
     */
    StgBatchPO dtoToPo(StgBatchDTO dto);

}
