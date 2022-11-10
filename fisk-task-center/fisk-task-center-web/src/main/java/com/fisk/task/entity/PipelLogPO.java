package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author cfk
 */
@Data
@TableName("tb_pipel_log")
public class PipelLogPO extends BasePO {
    public String pipelId;
    public String pipelTraceId;
    public String msg;
    public int type;
}
