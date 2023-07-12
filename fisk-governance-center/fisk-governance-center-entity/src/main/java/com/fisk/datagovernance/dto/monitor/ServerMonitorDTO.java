package com.fisk.datagovernance.dto.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ServerMonitorDTO {

    @ApiModelProperty(value = "服务名称")
    private String serverName;

    @ApiModelProperty(value = "服务端口")
    private Integer serverPort;

    @ApiModelProperty(value = "服务ip")
    private String serverIp;

    @ApiModelProperty(value = "服务状态1:运行2:停止")
    private Integer status;

}
