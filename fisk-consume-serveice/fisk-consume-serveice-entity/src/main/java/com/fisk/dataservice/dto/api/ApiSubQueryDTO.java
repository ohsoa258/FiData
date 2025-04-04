package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ApiSubQueryDTO {
    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    public int appId;

    /**
     * 应用类型：1本地应用、2代理应用
     */
    @ApiModelProperty(value = "应用类型：1本地应用、2代理应用")
    public int appType;

    /**
     * 搜索条件
     */
    @ApiModelProperty(value = "搜索条件")
    public String keyword;

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码")
    public int current;

    /**
     * 页数
     */
    @ApiModelProperty(value = "页数")
    public int size;
}
