package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.task.enums.TaskStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_task_log")
public class TaskLogPO extends BasePO {
    public String taskName;
    public String taskExchange;
    public String taskQueue;
    public TaskStatusEnum taskStatus;
    public boolean taskSendOk;
    public String taskData;
}
