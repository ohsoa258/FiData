package com.fisk.datamodel.dto.dimension;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DimensionDateAttributeDTO {
    @ApiModelProperty(value = "业务区域Id")
    public int businessAreaId;

    @ApiModelProperty(value = "维度Id")
    public long dimensionId;
    @ApiModelProperty(value = "维度属性Id")
    public long dimensionAttributeId;
}
