package com.fisk.datagovernance.entity.monitor;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("tb_server_monitor1")
@Data
public class ServerMonitorPO implements Serializable {

    @ApiModelProperty(value = "服务名称")
    private String serverName;

    @ApiModelProperty(value = "服务端口")
    private Integer serverPort;

    @ApiModelProperty(value = "服务ip")
    private String serverIp;

    @ApiModelProperty(value = "服务状态1:运行0:停止")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    public LocalDateTime createTime;

    @TableField(value = "create_user", fill = FieldFill.INSERT)
    public String createUser;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    public LocalDateTime updateTime;

    @TableField(value = "update_user", fill = FieldFill.UPDATE)
    public String updateUser;
    @TableLogic
    public int delFlag;

}
