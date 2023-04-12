package com.fisk.task.dto.dispatchlog;

import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author cfk
 */
@Data
public class PipelTaskMergeLogVO extends BasePO {
    @ApiModelProperty(value = "job批次号")
    public String jobTraceId;
    @ApiModelProperty(value = "task批次号")
    public String taskTraceId;
    @ApiModelProperty(value = "绑定表的任务的id")
    public String taskId;
    @ApiModelProperty(value = "绑定表的任务的名称")
    public String taskName;
    @ApiModelProperty(value = "绑定表id")
    public String tableId;
    @ApiModelProperty(value = "绑定表名称")
    public String tableName;
    @ApiModelProperty(value = "内容")
    public String msg;
    @ApiModelProperty(value = "task开始时间")
    public Date startTime;
    @ApiModelProperty(value = "task结束时间")
    public Date endTime;
    @ApiModelProperty(value = "持续时间/分钟")
    public String duration;
}
