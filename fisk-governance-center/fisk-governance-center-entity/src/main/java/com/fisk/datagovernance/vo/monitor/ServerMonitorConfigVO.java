package com.fisk.datagovernance.vo.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-07-26
 * @Description:
 */
@Data
public class ServerMonitorConfigVO {

    @ApiModelProperty(value = "id")
    private long id;

    @ApiModelProperty(value = "服务名称")
    private String serverName;

    @ApiModelProperty(value = "服务端口")
    private Integer serverPort;

    @ApiModelProperty(value = "服务ip")
    private String serverIp;

    @ApiModelProperty(value = "0:系统1:其他")
    private Integer serverTypeId;

    @ApiModelProperty(value = "0:系统1:其他")
    private String serverType;
}
