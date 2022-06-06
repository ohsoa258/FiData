package com.fisk.dataaccess.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * api请求参数
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-04-26 11:07:14
 */
@Data
public class ApiParameterDTO {

    @ApiModelProperty(value = "主键", required = true)
    public long id;

    @ApiModelProperty(value = "左边非实时api的id", required = true)
    public long apiId;

    @ApiModelProperty(value = "Headers or Body", required = true)
    @NotNull
    public String requestType;

    @ApiModelProperty(value = "form-data or raw")
    public String requestMethod;

    @ApiModelProperty(value = "请求参数key or Headers的key", required = true)
    public String parameterKey;

    @ApiModelProperty(value = "请求参数value or Headers的value", required = true)
    public String parameterValue;
}
