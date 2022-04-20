package com.fisk.mdm.map;

import com.fisk.mdm.dto.modelVersion.ModelVersionDTO;
import com.fisk.mdm.entity.ModelVersionPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author chenYa
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ModelVersionMap {
    ModelVersionMap INSTANCES = Mappers.getMapper(ModelVersionMap.class);

    /**
     * dto => po
     * @param dto
     * @return
     */
    ModelVersionPO dtoToPo(ModelVersionDTO dto);
}
