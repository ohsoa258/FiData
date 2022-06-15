package com.fisk.datamanagement.dto.businessmetadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessMetaDataAttributeDefsDTO {

    public String name;

    public String typeName;
    @JsonProperty(value = "isOptional")
    public boolean isOptional;

    public String cardinality;

    public int valuesMinCount;

    public int valuesMaxCount;
    @JsonProperty(value = "isUnique")
    public boolean isUnique;
    @JsonProperty(value = "isIndexable")
    public boolean isIndexable;

    public BusinessMetaDataOptionsDTO options;

    public String searchWeight;

    public boolean multiValueSelect;

}
