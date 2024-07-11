package com.fisk.datamanagement.dto.metadataglossarymap;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class GlossaryMapDelDTO implements Serializable {

    /**
     * 术语ID
     */
    @ApiModelProperty(value = "术语ID")
    public Integer glossaryId;

    @ApiModelProperty(value = "元数据实体限定名称")
    public String metadataQualifiedName;

}
