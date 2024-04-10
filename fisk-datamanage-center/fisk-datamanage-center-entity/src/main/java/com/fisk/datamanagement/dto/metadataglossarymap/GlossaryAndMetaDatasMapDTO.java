package com.fisk.datamanagement.dto.metadataglossarymap;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GlossaryAndMetaDatasMapDTO implements Serializable {

    /**
     * 术语ID
     */
    @ApiModelProperty(value = "术语ID")
    public Integer glossaryId;

    /**
     * 元数据实体ID + Type集合
     */
    @ApiModelProperty(value = "元数据实体ID + Type集合")
    public List<MetadataEntitySimpleDTO> metadataEntityIds;

}
