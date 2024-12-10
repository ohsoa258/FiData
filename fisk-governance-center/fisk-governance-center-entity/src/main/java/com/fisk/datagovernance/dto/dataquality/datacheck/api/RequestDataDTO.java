package com.fisk.datagovernance.dto.dataquality.datacheck.api;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 参数dto
 * @date 2022/1/18 10:10
 */
public class RequestDataDTO
{
    /**
     * 参数key
     */
    @ApiModelProperty(value = "参数key")
    @NotNull()
    public String parmKey;

    /**
     * 参数value
     */
    @ApiModelProperty(value = "参数value")
    public String parmValue;
}
