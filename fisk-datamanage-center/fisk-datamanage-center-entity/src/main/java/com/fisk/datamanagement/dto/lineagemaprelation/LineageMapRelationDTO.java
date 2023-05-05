package com.fisk.datamanagement.dto.lineagemaprelation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class LineageMapRelationDTO {

    @ApiModelProperty(value = "Id")
    public Integer id;

    @ApiModelProperty(value = "元数据")
    public Integer metadataEntityId;

    @ApiModelProperty(value = "fromEntityId")
    public Integer fromEntityId;

    @ApiModelProperty(value = "toEntityId")
    public Integer toEntityId;

    @ApiModelProperty(value = "流程类别")
    public Integer processType;

}
