package com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IntelligentDiscovery_UserDTO {
    /**
     * 主键ID
     */
    @ApiModelProperty(value = "主键ID")
    public int id;

    /**
     * tb_Intelligentdiscovery_rule表主键ID
     */
    @ApiModelProperty(value = "tb_Intelligentdiscovery_rule表主键ID")
    public int ruleId;

    /**
     * 接收人名称
     */
    @ApiModelProperty(value = "接收人名称")
    public String recipientName;

    /**
     * 接收人邮箱
     */
    @ApiModelProperty(value = "接收人邮箱")
    public String recipientEmail;
}
