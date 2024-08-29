package com.fisk.task.dto.accessmdm;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: wangjian
 * @Date: 2024-08-29
 * @Description:
 */
@Data
public class LogResultDTO {
    @ApiModelProperty(value = "日志提交id")
    private String subRunId;
    /**
     * 同步状态
     * 状态；0代表正在同步，1代表同步成功，2代表同步失败
     */
    @ApiModelProperty(value = "同步状态:状态；0代表正在同步，1代表同步成功，2代表同步失败")
    private Integer state;

    /**
     * 报错信息
     */
    @ApiModelProperty(value = "报错信息")
    private String ErrorMsg;

    /**
     * 同步开始时间
     */
    @ApiModelProperty(value = "同步开始时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 同步开始时间
     */
    @ApiModelProperty(value = "同步开始时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 数据量
     */
    @ApiModelProperty(value = "数据量")
    private Integer dataRows;
}
