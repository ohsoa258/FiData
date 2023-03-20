package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.metadataentity.MetadataEntityDTO;
import com.fisk.datamanagement.entity.MetadataEntityPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MetadataEntityMap {

    MetadataEntityMap INSTANCES = Mappers.getMapper(MetadataEntityMap.class);

    /**
     * po===>dto
     * @param metadataEntityPO
     * @return
     */
    MetadataEntityDTO toDto(MetadataEntityPO metadataEntityPO);

    /**
     * poList===>dtoList
     * @param metadataEntityPOs
     * @return
     */
    List<MetadataEntityDTO> toDtos(List<MetadataEntityPO> metadataEntityPOs);

}
