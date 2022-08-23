package com.fisk.dataaccess.dto.apioutputparameter;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 * @date 2022-08-18 14:11
 */
@Data
public class ApiOutputParameterDTO {

    @ApiModelProperty(value = "默认不填")
    public Long dataTargetId;

    @ApiModelProperty(value = "参数类型：header 或 body")
    public String parameterType;

    @ApiModelProperty(value = "参数key")
    public String queryParamsKey;

    @ApiModelProperty(value = "参数value")
    public String queryParamsValue;

    @ApiModelProperty(value = "参数描述")
    public String queryParamsDescribe;

    @ApiModelProperty(value = "参数方法：form-data 或 raw ")
    public String requestMethod;

}
