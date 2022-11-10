package com.fisk.datamanagement.map;

import com.fisk.common.service.metadata.dto.metadata.MetaDataColumnAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDbAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataTableAttributeDTO;
import com.fisk.datamanagement.dto.entity.EntityAttributesDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MetaDataMap {

    MetaDataMap INSTANCES = Mappers.getMapper(MetaDataMap.class);

    /**
     * instanceDto==>Attribute
     *
     * @param dto
     * @return
     */
    EntityAttributesDTO instanceDtoToAttribute(MetaDataInstanceAttributeDTO dto);

    /**
     * dbDto==>Attribute
     *
     * @param dto
     * @return
     */
    EntityAttributesDTO dbDtoToAttribute(MetaDataDbAttributeDTO dto);

    /**
     * tableDto==>Attribute
     *
     * @param dto
     * @return
     */
    EntityAttributesDTO tableDtoToAttribute(MetaDataTableAttributeDTO dto);

    /**
     * fieldDto==>Attribute
     *
     * @param dto
     * @return
     */
    EntityAttributesDTO fieldDtoToAttribute(MetaDataColumnAttributeDTO dto);

}
