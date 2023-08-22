package com.fisk.datamanagement.map;

import com.fisk.common.server.metadata.TemplateAttributeDto;
import com.fisk.datamanagement.dto.metadataentityexporttemplate.MetadataExportTemplateAttributeDto;
import com.fisk.datamanagement.entity.MetadataEntityExportTemplateAttributePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JinXingWang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MetadataEntityExportTemplateAttributeMap {
    MetadataEntityExportTemplateAttributeMap INSTANCES = Mappers.getMapper(MetadataEntityExportTemplateAttributeMap.class);
    /**
     *  po==>dto
     * @param PO
     * @return
     */
    MetadataExportTemplateAttributeDto poToDto(MetadataEntityExportTemplateAttributePO PO);
    /**
     *  poList==>dtoList
     * @param PO
     * @return
     */
    List<MetadataExportTemplateAttributeDto> poToDtoList(List<MetadataEntityExportTemplateAttributePO> PO);

    /**
     * dto ==>po
     * @param dto
     * @return
     */
    MetadataEntityExportTemplateAttributePO dtoToPo(MetadataExportTemplateAttributeDto  dto);
    /**
     * dto ==>po
     * @param dto
     * @return
     */
    List<MetadataEntityExportTemplateAttributePO> dtoToPoList(List<MetadataExportTemplateAttributeDto>  dto);


    /**
     * dto ==>po
     * @param dto
     * @return
     */
    TemplateAttributeDto dtoToDto(MetadataExportTemplateAttributeDto  dto);
    /**
     * dto ==>po
     * @param dto
     * @return
     */
    List<TemplateAttributeDto> dtoToDtoList(List<MetadataExportTemplateAttributeDto>  dto);

}
