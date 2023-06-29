package com.fisk.dataservice.vo.app;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 应用API绑定VO
 * @date 2023/6/19 16:11
 */
@Data
public class AppApiBindVO {
    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    public int appId;

    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称")
    public String appName;

    /**
     * apiId
     */
    @ApiModelProperty(value = "apiId")
    public int apiId;

    /**
     * api名称
     */
    @ApiModelProperty(value = "api名称")
    public String apiName;

    /**
     * api标识
     */
    @ApiModelProperty(value = "api标识")
    public String apiCode;
}
