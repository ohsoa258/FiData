package com.fisk.datamodel.dto.dimension;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DimensionTabDTO {
    @ApiModelProperty(value = "Id")
    public long id;

    @ApiModelProperty(value = "维度中文名")
    public String dimensionCnName;
}
