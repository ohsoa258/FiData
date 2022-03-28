package com.fisk.dataservice.dto.app;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 应用API内置参数查询 DTO
 * @date 2022/1/6 14:51
 */
public class AppApiParmQueryDTO {
    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    @NotNull()
    public Integer appId;

    /**
     * API id
     */
    @ApiModelProperty(value = "apiId")
    @NotNull()
    public Integer apiId;
}
