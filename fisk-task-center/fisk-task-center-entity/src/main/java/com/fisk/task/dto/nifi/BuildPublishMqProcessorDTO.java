package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildPublishMqProcessorDTO extends BaseProcessorDTO{

    @ApiModelProperty(value = "交换")
    public String exchange;
    @ApiModelProperty(value = "路由")
    public String route;

    @ApiModelProperty(value = "主机")
    public String host;

    @ApiModelProperty(value = "端口")
    public String port;

    @ApiModelProperty(value = "虚拟主机")
    public String vhost;

    @ApiModelProperty(value = "用户")
    public String user;

    @ApiModelProperty(value = "密码")
    public String pwd;

}
