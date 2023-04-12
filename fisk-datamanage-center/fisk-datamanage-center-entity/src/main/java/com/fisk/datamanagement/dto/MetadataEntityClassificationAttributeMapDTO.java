package com.fisk.datamanagement.dto;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MetadataEntityClassificationAttributeMapDTO {

    public Integer attributeTypeId;

    public Integer metadataEntityId;

    public String value;

    public String name;

    public String classificationName;

}
