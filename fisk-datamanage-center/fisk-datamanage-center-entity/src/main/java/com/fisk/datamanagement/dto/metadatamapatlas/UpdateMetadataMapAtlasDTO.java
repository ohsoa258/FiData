package com.fisk.datamanagement.dto.metadatamapatlas;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class UpdateMetadataMapAtlasDTO {

    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "数据类型")
    public int dataType;

    @ApiModelProperty(value = "表类型")
    public int tableType;

}
