package com.fisk.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author gy
 */
@Data
public class MQBaseDTO {
    @ApiModelProperty(value = "用户id")
    public Long userId;
    @ApiModelProperty(value = "发送时间")
    public LocalDateTime sendTime;
    @ApiModelProperty(value = "日志id")
    public Long logId;
    @ApiModelProperty(value = "跟踪id")
    public String traceId;
    @ApiModelProperty(value = "跨度id")
    public String spanId;
    /**
     * 报错信息
     */
    @ApiModelProperty(value = "报错信息")
    public String msg;
}
