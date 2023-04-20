package com.fisk.mdm.dto.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-04-07
 * @Description: 审核
 */
@Data
@NoArgsConstructor
public class BatchApprovalDTO {

    @ApiModelProperty(value = "工单ID列表")
    private List<Integer> processApplyIds;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "1通过/2拒绝")
    private Integer flag;

    @ApiModelProperty(value = "true管理员/false不是管理员")
    private Boolean adminMark;
}
