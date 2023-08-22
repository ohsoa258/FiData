package com.fisk.datamanagement.dto.metadataentityexporttemplate;

import lombok.Data;

import java.util.List;

/**
 * @author JinXingWang
 */
@Data
public class MetadataExportTemplateAttributeDto {
    /**
     *
     */
    private  Integer attributeId;

    /**
     *
     */
    private String attributeName;

    private Integer attributePid;

    /**
     *
     */
    private Boolean checked;
    public List<MetadataExportTemplateAttributeDto> children;
}
