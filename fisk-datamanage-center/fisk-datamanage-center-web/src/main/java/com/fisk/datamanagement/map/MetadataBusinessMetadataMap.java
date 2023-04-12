package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.metadatabusinessmetadatamap.MetadataBusinessMetadataMapDTO;
import com.fisk.datamanagement.entity.MetadataBusinessMetadataMapPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MetadataBusinessMetadataMap {

    MetadataBusinessMetadataMap INSTANCES = Mappers.getMapper(MetadataBusinessMetadataMap.class);

    /**
     * dtoList==>PoList
     *
     * @param dtoList
     * @return
     */
    List<MetadataBusinessMetadataMapPO> dtoListToPoList(List<MetadataBusinessMetadataMapDTO> dtoList);

}
