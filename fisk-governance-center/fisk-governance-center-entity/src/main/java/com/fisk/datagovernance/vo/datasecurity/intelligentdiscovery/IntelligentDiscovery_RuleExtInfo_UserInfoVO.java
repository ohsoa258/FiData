package com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IntelligentDiscovery_RuleExtInfo_UserInfoVO {
    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID")
    public Long id;

    /**
     * 用户名称
     */
    @ApiModelProperty(value = "用户名称")
    public String username;
}
