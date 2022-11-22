package com.fisk.task.dto.dispatchlog;

import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author cfk
 */
@Data
public class PipelJobMergeLogVO extends BasePO {

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
    @ApiModelProperty(value = "job开始时间")
    public Date startTime;
    @ApiModelProperty(value = "job结束时间")
    public Date endTime;
    @ApiModelProperty(value = "持续时间/分钟")
    public Long duration;
    @ApiModelProperty(value = "job运行结果,成功/失败")
    public String result;

}
