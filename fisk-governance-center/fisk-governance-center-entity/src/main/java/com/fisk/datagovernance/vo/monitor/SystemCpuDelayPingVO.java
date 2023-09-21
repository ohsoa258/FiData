package com.fisk.datagovernance.vo.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: wangjian
 * @Date: 2023-09-15
 * @Description:
 */
@Data
public class SystemCpuDelayPingVO {
    @ApiModelProperty(value = "cpu百分比")
    private BigDecimal cpuBuys;

    @ApiModelProperty(value = "时间轴")
    private String timestamp;
}
