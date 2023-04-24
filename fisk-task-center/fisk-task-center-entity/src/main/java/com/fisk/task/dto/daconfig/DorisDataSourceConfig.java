package com.fisk.task.dto.daconfig;

import io.swagger.annotations.ApiModelProperty;

/**
 * Author:DennyHui
 * CreateTime: 2021/6/30 17:40
 * Description:
 */
public class DorisDataSourceConfig {
    @ApiModelProperty(value = "路径")
    public String url;

    @ApiModelProperty(value = "用户名")
    public String username;

    @ApiModelProperty(value = "密码")
    public String password;

    @ApiModelProperty(value = "驱动类名")
    public String driver_class_name;
}
