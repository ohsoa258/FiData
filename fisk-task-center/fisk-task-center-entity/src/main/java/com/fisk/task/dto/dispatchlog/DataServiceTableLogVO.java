package com.fisk.task.dto.dispatchlog;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 表服务日志
 * @date 2023/1/10 14:55
 */
@Data
public class DataServiceTableLogVO
{
    /**
     * 表ID
     */
    @ApiModelProperty(value = "表ID")
    public int tableId;

    /**
     *  批次ID
     */
    @ApiModelProperty(value = "批次ID")
    public String taskTraceId;

    /**
     * 同步开始时间
     */
    @ApiModelProperty(value = "同步开始时间")
    public String startTime;

    /**
     * 同步结束时间
     */
    @ApiModelProperty(value = "同步结束时间")
    public String endTime;

    /**
     * 同步日志
     */
    @ApiModelProperty(value = "同步日志")
    public String msg;

    /**
     * 表别名
     */
    @ApiModelProperty(value = "表别名")
    public String tableDisplayName;
}
