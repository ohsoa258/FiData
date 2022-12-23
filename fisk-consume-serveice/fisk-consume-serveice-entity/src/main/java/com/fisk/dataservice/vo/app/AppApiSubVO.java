package com.fisk.dataservice.vo.app;

import io.swagger.annotations.ApiModelProperty;


/**
 * @author dick
 * @version v1.0
 * @description 应用订阅API VO
 * @date 2022/1/10 17:51
 */
public class AppApiSubVO {
    /**
     * Id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    public int appId;

    /**
     * 服务id
     */
    @ApiModelProperty(value = "API id")
    public int serviceId;

    /**
     * API 状态 1启用、0禁用
     */
    @ApiModelProperty(value = "apiState")
    public int apiState;

    /**
     * 服务名称名称
     */
    @ApiModelProperty(value = "服务名称名称")
    public String ServiceName;

    /**
     * api标识
     */
    @ApiModelProperty(value = "api标识")
    public String apiCode;

    /**
     * 服务描述
     */
    @ApiModelProperty(value = "服务描述")
    public String ServiceDesc;

    @ApiModelProperty(value = "类型：1api服务 2表服务 3 文件服务")
    public Integer type;
}
