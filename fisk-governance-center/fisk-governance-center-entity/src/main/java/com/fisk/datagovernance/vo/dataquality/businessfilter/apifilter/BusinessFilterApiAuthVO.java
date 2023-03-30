package com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BusinessFilterApiAuthVO {
    /**
     * api授权时间
     */
    @ApiModelProperty(value = "api授权时间")
    public LocalDateTime apiAuthExpirTime;

    /**
     * api授权有效时间，分钟
     */
    @ApiModelProperty(value = "api授权有效时间，分钟")
    public int apiAuthExpirMinute;

    /**
     * api授权票据
     */
    @ApiModelProperty(value = "api授权票据")
    public String apiAuthTicket;
}
