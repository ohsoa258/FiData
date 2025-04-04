package com.fisk.dataservice.dto.api.doc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description API请求参数
 * @date 2022/2/3 14:09
 */
@Data
public class ApiRequestDTO {
    /**
     * 参数名称
     */
    @ApiModelProperty(value = "参数名称")
    public String  parmName;

    /**
     * 是否必填
     */
    @ApiModelProperty(value = "是否必填")
    public String  isRequired;

    /**
     * 参数类型
     */
    @ApiModelProperty(value = "参数类型")
    public String  parmType;

    /**
     * 参数描述
     */
    @ApiModelProperty(value = "参数描述")
    public String  parmDesc;

    /**
     * 行样式
     */
    @ApiModelProperty(value = "行样式")
    public String  trStyle;
}
