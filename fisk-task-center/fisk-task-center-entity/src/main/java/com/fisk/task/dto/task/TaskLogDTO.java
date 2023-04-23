package com.fisk.task.dto.task;

import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.task.enums.TaskStatusEnum;
import lombok.Data;

@Data
public class TaskLogDTO extends BasePO{
    public String taskName;
    public String taskQueue;
    public TaskStatusEnum taskStatus;
    public boolean taskSendOk;
    public String taskData;
    public String traceId;
    public String msg;
}
