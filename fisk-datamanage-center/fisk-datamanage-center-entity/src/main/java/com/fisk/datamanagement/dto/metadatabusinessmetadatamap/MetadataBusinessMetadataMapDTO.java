package com.fisk.datamanagement.dto.metadatabusinessmetadatamap;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MetadataBusinessMetadataMapDTO {

    @ApiModelProperty(value = "业务数据源ID")
    public Integer businessMetadataId;

    @ApiModelProperty(value = "数据源实体ID")
    public Integer metadataEntityId;

    @ApiModelProperty(value = "值")
    public String value;

}
