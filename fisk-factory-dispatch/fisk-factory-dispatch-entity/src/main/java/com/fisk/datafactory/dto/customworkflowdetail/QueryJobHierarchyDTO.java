package com.fisk.datafactory.dto.customworkflowdetail;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class QueryJobHierarchyDTO {
    /**
     * 管道TraceId
     */
    @ApiModelProperty(value = "管道TraceId")
    public String pipelTraceId;
    /**
     * 管道自增id
     */
    @ApiModelProperty(value = "管道自增id")
    public Long nifiCustomWorkflowId;
}
