package com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IntelligentDiscovery_KeyWordRuleDTO {
    /**
     * 运算符
     */
    @ApiModelProperty(value = "运算符")
    public String operator;

    /**
     * 值
     */
    @ApiModelProperty(value = "值")
    public String value;
}
