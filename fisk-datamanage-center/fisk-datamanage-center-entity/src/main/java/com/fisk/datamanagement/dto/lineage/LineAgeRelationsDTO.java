package com.fisk.datamanagement.dto.lineage;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class LineAgeRelationsDTO {

    @ApiModelProperty(value = "fromEntityId")
    public String fromEntityId;

    @ApiModelProperty(value = "toEntityId")
    public String toEntityId;

    @ApiModelProperty(value = "relationshipId")
    public String relationshipId;

}
