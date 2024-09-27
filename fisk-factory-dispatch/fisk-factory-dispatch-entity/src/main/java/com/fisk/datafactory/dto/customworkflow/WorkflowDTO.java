package com.fisk.datafactory.dto.customworkflow;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-09-20
 * @Description:
 */
@Data
public class WorkflowDTO {
    @ApiModelProperty(value = "管道id")
    public Integer id;
    @ApiModelProperty(value = "表id")
    public Integer tableId;
    @ApiModelProperty(value = "管道名称")
    public String workflowName;
}
