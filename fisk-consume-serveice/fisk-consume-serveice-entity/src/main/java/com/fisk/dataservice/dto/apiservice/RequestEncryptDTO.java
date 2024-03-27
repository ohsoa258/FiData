package com.fisk.dataservice.dto.apiservice;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 参数dto
 * @date 2022/1/18 10:10
 */
public class RequestEncryptDTO
{
    /**
     * API标识
     */
    @ApiModelProperty(value = "API标识")
    @NotNull()
    public String apiCode;
}
