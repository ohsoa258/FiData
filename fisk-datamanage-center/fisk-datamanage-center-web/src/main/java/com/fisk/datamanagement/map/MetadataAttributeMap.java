package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.metadataattribute.MetadataAttributeDTO;
import com.fisk.datamanagement.entity.MetadataAttributePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MetadataAttributeMap {

    MetadataAttributeMap INSTANCES = Mappers.getMapper(MetadataAttributeMap.class);

    /**
     * dtoList==>PoList
     *
     * @param dtoList
     * @return
     */
    List<MetadataAttributePO> dtoListToPoList(List<MetadataAttributeDTO> dtoList);

}
