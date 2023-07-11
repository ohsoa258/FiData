package com.fisk.datagovernance.vo.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-07-10
 * @Description:
 */
@Data
public class SystemMonitorVO {

    @ApiModelProperty(value = "运行时间")
    private String upTime;

    @ApiModelProperty(value = "cpu核心数")
    private Integer cpuCores;

    @ApiModelProperty(value = "内存大小")
    private Integer rawTotal;

    @ApiModelProperty(value = "缓存大小")
    private Integer swapTotal;

    @ApiModelProperty(value = "cpu使用率")
    private Double cpuBusy;

    @ApiModelProperty(value = "内存使用大小")
    private Double rawUsed;

    @ApiModelProperty(value = "缓存使用大小")
    private Double swapUsed;

}
