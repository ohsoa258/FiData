package com.fisk.task.dto.daconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class AssociatedConditionDTO {

    @ApiModelProperty(value = "Id")
    public String id;

    @ApiModelProperty(value = "关联维度名称")
    public String associateDimensionName;
    @ApiModelProperty(value = "关联性")
    public String relevancy;
}
