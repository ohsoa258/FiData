package com.fisk.datafactory.dto.customworkflowdetail;

import lombok.Data;

/**
 * @author cfk
 */
@Data
public class QueryJobHierarchyDTO {
    /**
     * 管道TraceId
     */
    public String pipelTraceId;
    /**
     * 管道自增id
     */
    public Long nifiCustomWorkflowId;
}
