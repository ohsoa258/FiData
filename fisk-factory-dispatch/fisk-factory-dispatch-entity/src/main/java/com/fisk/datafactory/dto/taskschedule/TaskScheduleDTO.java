package com.fisk.datafactory.dto.taskschedule;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TaskScheduleDTO {
    @ApiModelProperty(value = "id")
    public int id;
    /**
     * 子节点id
     */
    @ApiModelProperty(value = "子节点id")
    public int jobId;
    /**
     * 父节点id
     */
    @ApiModelProperty(value = "父节点id")
    public int jobPid;
    /**
     * 同步方式
     */
    @ApiModelProperty(value = "同步方式")
    public String syncMode;
    /**
     * 表达式
     */
    @ApiModelProperty(value = "表达式")
    public String expression;
    /**
     * 日志
     */
    @ApiModelProperty(value = "日志")
    public String msg;
    /**
     * tree标识
     */
    @ApiModelProperty(value = "tree标识")
    public int flag;
    /**
     * 0:实时  1:非实时
     */
    @ApiModelProperty(value = "0:实时  1:非实时")
    public int appType;
}
