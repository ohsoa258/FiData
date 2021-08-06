package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeUpdateDTO;
import com.fisk.datamodel.entity.DimensionAttributePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

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
    DimensionAttributeMetaDataDTO poToMetaDto(DimensionAttributePO po);

}
