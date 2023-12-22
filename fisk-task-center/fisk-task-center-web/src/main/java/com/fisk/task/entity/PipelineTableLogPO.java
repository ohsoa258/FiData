package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author cfk
 */
@Data
@TableName("tb_pipeline_table_log")
public class PipelineTableLogPO extends BasePO {
    public Integer componentId;
    public Integer tableId;
    public Integer tableType;
    public Integer state;
    public String comment;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime endTime;
    public int counts;
    public Integer appId;
    public Integer dispatchType;

}
