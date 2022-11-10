package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author cfk
 */
@Data
@TableName("tb_pipel_stage_log")
public class PipelStageLogPO extends BasePO {
    public String stateTraceId;
    public String taskTraceId;
    public String msg;
    public Integer type;

}
