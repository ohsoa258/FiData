package com.fisk.task.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class DwLogResultDTO {

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
    private Date startTime;

    /**
     * 同步开始时间
     */
    @ApiModelProperty(value = "同步开始时间")
    private Date endTime;

    /**
     * 数据量
     */
    @ApiModelProperty(value = "数据量")
    private Integer dataRows;


}
