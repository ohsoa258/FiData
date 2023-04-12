package com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IntelligentDiscovery_RuleExtInfo_EmailServiceVO {
    /**
     * id
     */
    @ApiModelProperty(value = "标识")
    public int id;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    public String name;
}
