package com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IntelligentDiscovery_KeyWordRuleVO {
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
