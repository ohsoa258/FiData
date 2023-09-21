package com.fisk.datagovernance.dto.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-07-26
 * @Description:
 */
@Data
public class ServerMonitorConfigDTO {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "服务名称")
    private String serverName;

    @ApiModelProperty(value = "服务端口")
    private Integer serverPort;

    @ApiModelProperty(value = "服务类型0:系统1:其他")
    private Integer serverType;

    @ApiModelProperty(value = "服务ip")
    private String serverIp;
}
