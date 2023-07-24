package com.fisk.datagovernance.entity.monitor;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wangjian
 * @date 2023-07-14 16:21:58
 */
@TableName("tb_server_monitor_config")
@Data
public class ServerMonitorConfigPO extends BasePO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "服务名称")
    private String serverName;

    @ApiModelProperty(value = "服务端口")
    private Integer serverPort;

    @ApiModelProperty(value = "服务ip")
    private String serverIp;

}
