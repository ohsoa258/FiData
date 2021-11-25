package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.dimensionattribute.*;
import com.fisk.datamodel.entity.DimensionAttributePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DimensionAttributeMap {
    DimensionAttributeMap INSTANCES= Mappers.getMapper(DimensionAttributeMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    DimensionAttributePO dtoToPo(DimensionAttributeDTO dto);

    /**
     * updateDto==>po
     * @param dto
     * @return
     */
    DimensionAttributePO updateDtoToPo(DimensionAttributeUpdateDTO dto);

    /**
     * po==>MetaDto
     * @param po
     * @return
     */
    ModelAttributeMetaDataDTO poToMetaDto(DimensionAttributePO po);

    /**
     * po==>dto
     * @param po
     * @return
     */
    List<DimensionAttributeDataDTO> poToDto(List<DimensionAttributePO> po);

    /**
     * po==>NameListDTO
     * @param po
     * @return
     */
    List<DimensionAttributeAssociationDTO> poToNameListDTO(List<DimensionAttributePO> po);

    /**
     *dtoList==>PoList
     * @param dto
     * @return
     */
    List<DimensionAttributePO> dtoListToPoList(List<DimensionAttributeDTO> dto);

    /**
     * poList==>dtoList
     * @param dto
     * @return
     */
    List<DimensionAttributeDTO> poListToDtoList(List<DimensionAttributePO> dto);

    /**
     * po==>DetailDto
     * @param po
     * @return
     */
    DimensionAttributeUpdateDTO poToDetailDto(DimensionAttributePO po);

}
