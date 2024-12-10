package com.fisk.datagovernance.dto.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version 1.0
 * @description 生成应用api文档
 * @date 2022/1/28 18:53
 */
public class CreateAppApiDocDTO
{
    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    @NotNull()
    public Integer appId;
}
