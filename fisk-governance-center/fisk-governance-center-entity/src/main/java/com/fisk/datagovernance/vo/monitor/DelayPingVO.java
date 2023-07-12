package com.fisk.datagovernance.vo.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-07-10
 * @Description:
 */
@Data
public class DelayPingVO {

    @ApiModelProperty(value = "时间轴")
    private String timestamp;

    @ApiModelProperty(value = "总个数")
    private Integer totalCount;

    @ApiModelProperty(value = "成功个数")
    private Integer successTotal;

    @ApiModelProperty(value = "失败个数")
    private Integer failureTotal;
}
