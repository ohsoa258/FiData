package com.fisk.mdm.dto.mathingrules;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JinXingWang
 */
@Data
public class MatchingRulesFiledDto {
    /**
     * 属性ID
     */
    @ApiModelProperty(value = "属性ID")
    public Integer attributeId;
    /**
     * 权重
     */
    @ApiModelProperty(value = "权重")
    public Integer weight;
}
