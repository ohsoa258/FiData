package com.fisk.datagovernance.entity.monitor;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

@TableName("tb_server_monitor")
@Data
public class ServerMonitorPO  extends BasePO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "服务名称")
    private String serverName;

    @ApiModelProperty(value = "服务端口")
    private Integer serverPort;

    @ApiModelProperty(value = "服务ip")
    private String serverIp;

    @ApiModelProperty(value = "服务状态1:运行0:停止")
    private Integer status;

}
