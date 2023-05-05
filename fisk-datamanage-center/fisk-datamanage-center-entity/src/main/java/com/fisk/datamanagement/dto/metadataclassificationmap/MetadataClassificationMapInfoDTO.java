package com.fisk.datamanagement.dto.metadataclassificationmap;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MetadataClassificationMapInfoDTO extends MetadataClassificationMapDTO {

    @ApiModelProperty(value = "名称")
    public String name;

}
