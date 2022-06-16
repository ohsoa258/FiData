package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author cfk
 */
@Data
@TableName("tb_pipel_job_log")
public class PipelJobLogPO extends BasePO {
    public String pipelTraceId;
    public String jobTraceId;
    public String pipelId;
    public String componentId;
    public String msg;
    public int type;

}
