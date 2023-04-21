package com.fisk.datamanagement.dto.metadatamapatlas;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 * @date 2022-06-20 14:45
 */
@Data
public class MetaDataMapTableDTO {

    @ApiModelProperty(value = "atlasGuid")
    public String atlasGuid;

    @ApiModelProperty(value = "表id")
    public Integer tableId;

}
