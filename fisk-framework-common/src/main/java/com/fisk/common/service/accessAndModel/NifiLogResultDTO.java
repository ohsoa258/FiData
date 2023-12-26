package com.fisk.common.service.accessAndModel;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NifiLogResultDTO {

    /**
     * 表名
     */
    @ApiModelProperty(value = "表名")
    private String tableName;

    /**
     * 触发时间
     */
    @ApiModelProperty(value = "触发时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime triggerTime;

    /**
     * 触发类型
     * 0手动 1管道  -1正在同步
     */
    @ApiModelProperty(value = "触发类型")
    private Integer triggerType;

    /**
     * 同步状态；0代表进行中，1代表已完成
     */
    @ApiModelProperty(value = "同步状态；0代表进行中，1代表已完成")
    private Integer state;

    /**
     * 同步结果；0代表正在同步，1代表同步成功，2代表同步失败
     */
    @ApiModelProperty(value = "同步结果；0代表正在同步，1代表同步成功，2代表同步失败")
    private Integer result;

    /**
     * 同步开始时间
     */
    @ApiModelProperty(value = "同步开始时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 同步结束时间
     */
    @ApiModelProperty(value = "同步结束时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 同步结束时间
     */
    @ApiModelProperty(value = "同步持续时间")
    private String duration;

    /**
     * 数据量
     */
    @ApiModelProperty(value = "数据量")
    private Integer dataRows;

    /**
     * 报错信息
     */
    @ApiModelProperty(value = "报错信息")
    private String ErrorMsg;



}
