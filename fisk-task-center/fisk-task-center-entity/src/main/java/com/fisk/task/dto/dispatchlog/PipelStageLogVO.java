package com.fisk.task.dto.dispatchlog;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class PipelStageLogVO {
    @ApiModelProperty(value = "阶段批次号")
    public String stateTraceId;
    @ApiModelProperty(value = "日志内容")
    public String msg;
    @ApiModelProperty(value = "日志类别")
    public Integer type;
    @ApiModelProperty(value = "日志类别名称")
    public Integer typeName;
}
