package com.fisk.dataservice.dto.apiservice;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Map;

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
    public Map<String, Object> parmList;

    /**
     * API标识
     */
    @ApiModelProperty(value = "API标识")
    @NotNull()
    public String apiCode;
}
