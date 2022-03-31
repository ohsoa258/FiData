package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 查询api信息，含订阅信息
 * @date 2022/1/19 19:01
 */
public class ApiSubQueryDTO {
    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    public int appId;

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
