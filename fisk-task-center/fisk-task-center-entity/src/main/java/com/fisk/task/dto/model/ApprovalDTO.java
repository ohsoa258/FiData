package com.fisk.task.dto.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: wangjian
 * @Date: 2023-04-07
 * @Description: 审核
 */
@Data
@NoArgsConstructor
public class ApprovalDTO {
    @ApiModelProperty(value = "工单ID")
    private Integer processApplyId;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "1通过/2拒绝")
    private Integer flag;
}
