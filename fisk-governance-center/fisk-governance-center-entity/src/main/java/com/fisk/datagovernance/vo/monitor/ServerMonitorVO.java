package com.fisk.datagovernance.vo.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-07-10
 * @Description:
 */
@Data
public class ServerMonitorVO {

    @ApiModelProperty(value = "总个数")
    private Integer totalCount;

    @ApiModelProperty(value = "成功个数")
    private Integer successTotal;

    @ApiModelProperty(value = "失败个数")
    private Integer failureTotal;

    @ApiModelProperty(value = "总时移ping")
    private List<DelayPingVO> delayPingVOList;
}
