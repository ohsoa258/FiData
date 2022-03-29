package com.fisk.dataservice.dto.app;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 应用API内置参数 DTO
 * @date 2022/1/6 14:51
 */
public class AppApiBuiltinParmEditDTO {
    /**
     * API Id
     */
    @ApiModelProperty(value = "apiId")
    @NotNull()
    public Integer apiId;

    /**
     * 应用Id
     */
    @ApiModelProperty(value = "应用id")
    @NotNull()
    public Integer appId;

    /**
     * 内置参数列表
     */
    @ApiModelProperty(value = "内置参数列表")
    @NotNull()
    public List<AppApiBuiltinParmDTO> parmList;
}
