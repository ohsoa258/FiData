package com.fisk.dataservice.dto.app;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 应用订阅API查询 DTO
 * @date 2022/1/6 14:51
 */
public class AppApiSubQueryDTO {
    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    @NotNull()
    public Integer appId;

    /**
     * 关键字
     */
    @ApiModelProperty(value = "关键字")
    public String keyword;
}
