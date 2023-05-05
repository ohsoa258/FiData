package com.fisk.datafactory.dto.components;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author cfk
 */
public class TableUsageDTO {
    /**
     * 管道id
     */
    @ApiModelProperty(value = "管道id")
    public long pipelId;
    /**
     * 管道名称
     */
    @ApiModelProperty(value = "管道名称")
    public String pipelName;
    /**
     * 组id
     */
    @ApiModelProperty(value = "组id")
    public long jobId;
    /**
     * 组名称
     */
    @ApiModelProperty(value = "组名称")
    public String jobName;
    /**
     * 任务id
     */
    @ApiModelProperty(value = "任务id")
    public long taskId;
    /**
     * 任务名称
     */
    @ApiModelProperty(value = "任务名称")
    public String taskName;
    /**
     * 表所在组第几个
     */
    @ApiModelProperty(value = "表所在组第几个")
    public long tableOrder;


}
