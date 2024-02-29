package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.label.GlobalSearchDto;
import com.fisk.datamanagement.dto.metadataentity.MetadataEntityDTO;
import com.fisk.datamanagement.dto.search.EntitiesDTO;
import com.fisk.datamanagement.entity.MetadataEntityPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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


    @Mapping(source = "displayText",target = "name")
    @Mapping(source = "guid",target = "id")
    @Mapping(target = "type",constant = "META_DATA")
    GlobalSearchDto entitiesDtoToTermDto(EntitiesDTO dto);

    List<GlobalSearchDto> entitiesDtoToTermDtoList(List<EntitiesDTO> dtos);
}
