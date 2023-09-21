package com.fisk.datagovernance.dto.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-09-15
 * @Description:
 */
@Data
public class ServerMonitorQueryDTO {
    @ApiModelProperty(value = "ip")
    private String ip;
    @ApiModelProperty(value = "查询范围时间")
    private Integer number;
    @ApiModelProperty(value = "查询范围时间单位")
    private Integer type;
    @ApiModelProperty(value = "服务类型")
    private Integer serverType;
    @ApiModelProperty(value = "状态")
    private Integer status;
    @ApiModelProperty(value = "搜索值")
    private String searchKey;
}
