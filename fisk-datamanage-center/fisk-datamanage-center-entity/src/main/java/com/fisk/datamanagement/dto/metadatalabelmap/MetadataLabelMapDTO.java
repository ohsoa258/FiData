package com.fisk.datamanagement.dto.metadatalabelmap;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MetadataLabelMapDTO {

    @ApiModelProperty(value = "元数据实体id")
    public Integer metadataEntityId;

    @ApiModelProperty(value = "标签ID")
    public Integer labelId;

}
