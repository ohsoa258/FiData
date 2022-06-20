package com.fisk.task.dto.dispatchlog;

import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class PipelJobLogVO extends BasePO {
    @ApiModelProperty(value = "管道批次号")
    public String pipelTraceId;
    @ApiModelProperty(value = "job批次号")
    public String jobTraceId;
    @ApiModelProperty(value = "调度的管道id")
    public String pipelId;
    @ApiModelProperty(value = "调度的管道名称")
    public String pipelName;
    @ApiModelProperty(value = "组件id")
    public String componentId;
    @ApiModelProperty(value = "组件名称")
    public String componentName;
    @ApiModelProperty(value = "日志内容")
    public String msg;
    @ApiModelProperty(value = "日志类型")
    public int type;
    @ApiModelProperty(value = "日志类型名称")
    public int typeName;
}
