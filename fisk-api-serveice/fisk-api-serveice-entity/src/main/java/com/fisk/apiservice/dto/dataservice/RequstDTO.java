package com.fisk.apiservice.dto.dataservice;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.HashMap;

/**
 * @author dick
 * @version v1.0
 * @description 数据请求DTO
 * @date 2022/1/18 10:03
 */
public class RequstDTO {
    /**
     * 请求参数
     */
    @ApiModelProperty(value = "请求参数")
    public HashMap<String, Object> parmList;

    /**
     * API标识
     */
    @ApiModelProperty(value = "API标识")
    @NotNull()
    public String apiCode;

    /**
     * 当前页，起始页为第一页
     */
    @ApiModelProperty(value = "当前页")
    public Integer current;

    /**
     * 每页大小,最大500
     */
    @ApiModelProperty(value = "size")
    public Integer size;
}
