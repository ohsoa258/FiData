package com.fisk.dataservice.vo.logs;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 表服务日志详情VO
 * @date 2023/1/10 17:14
 */
@Data
public class TableServiceLogDetailVO {
    /**
     * 表ID
     */
    @ApiModelProperty(value = "表ID")
    public Long tableId;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableDisplayName;

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
}
