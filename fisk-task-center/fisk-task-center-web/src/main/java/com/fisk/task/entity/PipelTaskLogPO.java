package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author cfk
 */
@Data
@TableName("tb_pipel_task_log")
public class PipelTaskLogPO extends BasePO {
    public String jobTraceId;
    public String taskTraceId;
    public String taskId;
    public String msg;
    public int type;

}
