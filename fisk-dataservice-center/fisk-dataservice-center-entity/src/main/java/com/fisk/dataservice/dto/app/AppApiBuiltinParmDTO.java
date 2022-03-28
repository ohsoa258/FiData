package com.fisk.dataservice.dto.app;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 应用API内置参数 DTO
 * @date 2022/1/6 14:51
 */
public class AppApiBuiltinParmDTO {
    /**
     * API Id
     */
    @ApiModelProperty(value = "apiId")
    public int apiId;

    /**
     * 应用Id
     */
    @ApiModelProperty(value = "应用id")
    public int appId;

    /**
     * parmId
     */
    @ApiModelProperty(value = "parmId")
    public int parmId;

    /**
     * 是否是内置参数 1是、0否
     */
    @ApiModelProperty(value = "是否是内置参数 1是、0否")
    public int parmIsbuiltin;

    /**
     * 参数描述
     */
    @ApiModelProperty(value = "参数描述")
    @Length(min = 0, max = 255, message = "长度最多255")
    public String parmDesc;

    /**
     * 参数值
     */
    @ApiModelProperty(value = "参数值")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String parmValue;
}
