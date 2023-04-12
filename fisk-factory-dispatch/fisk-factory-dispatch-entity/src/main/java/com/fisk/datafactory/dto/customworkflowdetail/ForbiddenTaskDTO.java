package com.fisk.datafactory.dto.customworkflowdetail;

import lombok.Data;

/**
 * @author cfk
 */
@Data
public class ForbiddenTaskDTO {
    /**
     * true启用.false禁用
     */
    public Boolean forbidden;
    /**
     * 任务或jobid
     */
    public long taskId;
}
