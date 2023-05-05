package com.fisk.task.dto.dispatchlog;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cfk
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DispatchExceptionHandlingDTO {
    /**
     * 管道批次id
     */
    @ApiModelProperty(value = "管道批次id")
    public String pipelTraceId;
    /**
     * job批次号
     */
    @ApiModelProperty(value = "job批次号")
    public String pipelJobTraceId;

    /**
     * task批次号
     */
    @ApiModelProperty(value = "task批次号")
    public String pipelTaskTraceId;

    /**
     * stage批次号
     */
    @ApiModelProperty(value = "stage批次号")
    public String pipelStageTraceId;

    /**
     * 报错日志
     */
    @ApiModelProperty(value = "报错日志")
    public String comment;

    /**
     * 管道名称
     */
    @ApiModelProperty(value = "管道名称")
    public String pipleName;

    /**
     * job名称
     */
    @ApiModelProperty(value = "job名称")
    public String jobName;


}
