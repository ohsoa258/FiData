package com.fisk.datamanagement.dto.metadataglossarymap;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MetaDataGlossaryMapDTO {

    @ApiModelProperty(value = "元数据实体ID")
    public Integer metadataEntityId;

    @ApiModelProperty(value = "术语ID")
    public Integer glossaryId;

    @ApiModelProperty(value = "术语名称")
    public String glossaryName;

}
