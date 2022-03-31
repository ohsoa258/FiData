package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

import java.util.Date;

/**
 * @author cfk
 */
@Data
@TableName("tb_pipeline_table_log")
public class PipelineTableLogPO extends BasePO {
    public int componentId;
    public int tableId;
    public int tableType;
    public int state;
    public String comment;
    public Date startTime;
    public Date endTime;
    public int counts;

}
