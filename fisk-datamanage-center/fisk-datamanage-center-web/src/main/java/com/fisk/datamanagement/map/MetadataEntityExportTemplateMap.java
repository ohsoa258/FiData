package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.metadataentityexporttemplate.EditMetadataExportTemplateDto;
import com.fisk.datamanagement.dto.metadataentityexporttemplate.MetadataExportTemplateDetailDto;
import com.fisk.datamanagement.dto.metadataentityexporttemplate.MetadataExportTemplateDto;
import com.fisk.datamanagement.entity.MetadataEntityExportTemplatePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JinXingWang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MetadataEntityExportTemplateMap {
    MetadataEntityExportTemplateMap INSTANCES = Mappers.getMapper(MetadataEntityExportTemplateMap.class);
    /**
     *  po==>dto
     * @param PO
     * @return
     */
    MetadataExportTemplateDetailDto poToDetailDto(MetadataEntityExportTemplatePO PO);

    /**
     *  po==>dto
     * @param PO
     * @return
     */
    List<MetadataExportTemplateDetailDto> poToDetailDtoList(List<MetadataEntityExportTemplatePO> PO);


    /**
     * dto ==>po
     * @param dto
     * @return
     */
    MetadataEntityExportTemplatePO  dtoToPo(MetadataExportTemplateDetailDto dto);

    /**
     * dto ==>po
     * @param dto
     * @return
     */
    MetadataEntityExportTemplatePO  editDtoToPo(EditMetadataExportTemplateDto dto);


    /**
     *  po==>dto
     * @param PO
     * @return
     */
    MetadataExportTemplateDto poToDto(MetadataEntityExportTemplatePO PO);

    /**
     *  po==>dto
     * @param PO
     * @return
     */
    List<MetadataExportTemplateDto> poToDtoList(List<MetadataEntityExportTemplatePO> PO);


}
