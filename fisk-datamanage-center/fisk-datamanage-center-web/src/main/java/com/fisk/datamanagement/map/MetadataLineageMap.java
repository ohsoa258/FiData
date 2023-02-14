package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.metadatalineagemap.MetadataLineageMapDTO;
import com.fisk.datamanagement.entity.MetadataLineageMapPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MetadataLineageMap {

    MetadataLineageMap INSTANCES = Mappers.getMapper(MetadataLineageMap.class);

    /**
     * dto==>Po
     *
     * @param dto
     * @return
     */
    MetadataLineageMapPO dtoToPo(MetadataLineageMapDTO dto);

}
