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
public class ServerTableVO {

    @ApiModelProperty(value = "服务名称")
    private String serverName;

    @ApiModelProperty(value = "服务url")
    private String serverUrl;

    @ApiModelProperty(value = "服务端口")
    private Integer port;

    @ApiModelProperty(value = "服务状态")
    private Integer status;

    @ApiModelProperty(value = "时间")
    private String date;

    @ApiModelProperty(value = "时移ping")
    private List<DelayPingVO> delayPingVO;
}
