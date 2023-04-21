package com.fisk.datamanagement.dto.metadataclassificationmap;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MetadataClassificationMapDTO {

    @ApiModelProperty(value = "元数据实体ID")
    public Integer metadataEntityId;

    @ApiModelProperty(value = "业务分类ID")
    public Integer businessClassificationId;

}
