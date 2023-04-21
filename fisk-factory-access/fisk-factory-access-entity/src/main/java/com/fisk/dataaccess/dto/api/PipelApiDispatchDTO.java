package com.fisk.dataaccess.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: cfk
 * CreateTime: 2022/06/09 11:05
 * Description:
 */
@Data
public class PipelApiDispatchDTO {
    /**
     * task_id任务id
     */
    @ApiModelProperty(value = "task_id任务id")
    public String workflowId;
    @ApiModelProperty(value = "appId")
    public long appId;
    @ApiModelProperty(value = "apiId")
    public long apiId;
    /**
     * 管道id
     */
    @ApiModelProperty(value = "管道id")
    public long pipelineId;

}