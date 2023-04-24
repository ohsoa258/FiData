package com.fisk.datafactory.dto.customworkflowdetail;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class ForbiddenTaskDTO {
    /**
     * true启用.false禁用
     */
    @ApiModelProperty(value = "true启用.false禁用")
    public Boolean forbidden;
    /**
     * 任务或jobid
     */
    @ApiModelProperty(value = "任务或jobid")
    public long taskId;
}
