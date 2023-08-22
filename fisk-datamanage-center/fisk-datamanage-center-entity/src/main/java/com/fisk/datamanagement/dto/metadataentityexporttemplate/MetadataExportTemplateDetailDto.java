package com.fisk.datamanagement.dto.metadataentityexporttemplate;

import lombok.Data;

import java.util.List;

/**
 * @author JinXingWang
 */
@Data
public class MetadataExportTemplateDetailDto {
    private String name;

    private List<MetadataExportTemplateAttributeDto> attribute;
}
