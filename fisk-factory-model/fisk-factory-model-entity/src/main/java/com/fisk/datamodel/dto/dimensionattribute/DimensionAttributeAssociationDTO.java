package com.fisk.datamodel.dto.dimensionattribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DimensionAttributeAssociationDTO {

    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "维度字段英文名")
    public String dimensionFieldEnName;
}
