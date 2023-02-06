package com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IntelligentDiscovery_RuleExtInfo_UserVO {
    /**
     * 用户Id
     */
    @ApiModelProperty(value = "用户Id")
    public Long id;

    /**
     * 用户邮箱
     */
    @ApiModelProperty(value = "用户邮箱")
    public String email;

    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号")
    public String userAccount;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    public String username;

    /**
     * 是否有效
     */
    @ApiModelProperty(value = "是否有效")
    public boolean valid;
}
